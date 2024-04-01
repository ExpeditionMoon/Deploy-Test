package goorm.tricount.service;

import goorm.tricount.dto.BalanceResult;
import goorm.tricount.dto.ExpenseResult;
import goorm.tricount.enums.TricountApiErrorCode;
import goorm.tricount.model.Member;
import goorm.tricount.model.Settlement;
import goorm.tricount.repository.ExpenseRepository;
import goorm.tricount.repository.SettlementRepository;
import goorm.tricount.util.TricountApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final ExpenseRepository expenseRepository;

    // 생성 및 다른 멤버와 조인
    @Transactional
    public Settlement createAndJoinSettlement(String settlementName, Member member) {
        // TODO 예외 처리

        // 새로운 정산 생성
        Settlement settlement = settlementRepository.create(settlementName);
        // 해당 사용자를 정산 참여자에 추가(요구사항) - 중간 테이블
        settlement.setParticipants(Collections.singletonList(member));
        settlementRepository.addParticipantToSettlement(settlement.getId(), member.getId());

        return settlement;
    }

    // 다른 멤버와 조인
    @Transactional
    public void joinSettlement(Long settlementId, Long memberId) {
        // TODO 예외 처리 - 없는 정산이나 유저에 대해서 요청을 보낸 경우

        settlementRepository.addParticipantToSettlement(settlementId, memberId);
    }

    /* 정산 결과 조회 */
    public List<BalanceResult> getBalanceResult(Long settlementId) {
        List<ExpenseResult> expensesWithMember = getExpensesWithMember(settlementId);
        return this.calculateBalanceResult(expensesWithMember);
    }

    private List<ExpenseResult> getExpensesWithMember(Long settlementId) {
        // 지출이 있는 멤버
        List<ExpenseResult> expensesWithMember = expenseRepository.findExpensesWithMemberBySettlementId(settlementId);
        System.out.println("expensesWithMember.toString() = " + expensesWithMember.toString());

        // 정산에 참여한 전체 멤버
        Optional<Settlement> settlementOptional = settlementRepository.findById(settlementId);
        if (!settlementOptional.isPresent()) {
            throw new TricountApiException("정산 id를 찾을 수 없습니다.", TricountApiErrorCode.INVALID_INPUT_VALUE);
        }
        Settlement settlement = settlementOptional.get();

        // 지출이 없는 멤버 계산
        List<Member> expenseMembers = expensesWithMember.stream()
                .map(ExpenseResult::getPayerMember)
                .distinct()
                .collect(Collectors.toList());

        List<Member> noExpenseMembers = settlement.getParticipants().stream()
                .filter(member -> !expenseMembers.contains(member))
                .collect(Collectors.toList());

        List<ExpenseResult> noExpenseMemberResult = createExpenseResultWithZero(settlement, noExpenseMembers);

        // 전체 멤버 = 지출이 있는 멤버 + 지출이 없는 멤버
        List<ExpenseResult> mergedList = new ArrayList<>(expensesWithMember);
        mergedList.addAll(noExpenseMemberResult);
        return mergedList;
    }

    private List<ExpenseResult> createExpenseResultWithZero(Settlement settlement, List<Member> members) {
        return members.stream()
                .map(member -> {
                    ExpenseResult expenseResult = new ExpenseResult();
                    expenseResult.setId(0L); // id값은 알 수 없으므로 0으로 초기화
                    expenseResult.setName(settlement.getName());
                    expenseResult.setSettlementId(settlement.getId());
                    expenseResult.setPayerMember(member);
                    expenseResult.setAmount(BigDecimal.ZERO); // amount만 0으로 초기화
                    expenseResult.setExpenseDateTime(LocalDateTime.now()); // 현재 날짜 및 시간으로 초기화
                    return expenseResult;
                })
                .collect(Collectors.toList());
    }

    private List<BalanceResult> calculateBalanceResult(List<ExpenseResult> expensesWithMember) {
        // 모든 지출 항목을 사용자별로 그룹화하고 각 사용자별 지출 총액을 계산함
        Map<Long, BigDecimal> userBalanceMap = expensesWithMember.stream()
                .collect(groupingBy(
                        expense -> expense.getPayerMember().getId(),
                        mapping(ExpenseResult::getAmount, reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        log.info("userBalanceMap = {}", userBalanceMap);

        // 사용자간의 송금 결과 계산
        List<BalanceResult> balanceResults = calculateBalance(userBalanceMap, expensesWithMember);
        return balanceResults;
    }

    private List<BalanceResult> calculateBalance(Map<Long, BigDecimal> userBalanceMap, List<ExpenseResult> expensesWithMember) {
        // 총합과 평균 구하기
        BigDecimal totalAmount = expensesWithMember.stream()
                .map(ExpenseResult::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalMember = expensesWithMember.stream()
                .map(ExpenseResult::getPayerMember)
                .distinct()
                .count();

        BigDecimal averageAmount = totalMember > 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                totalAmount.divide(BigDecimal.valueOf(totalMember), BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        // 정산 불가능 금액 = 총합 - 평균 * 모수
        BigDecimal impossibleAmount = totalAmount.subtract(averageAmount.multiply(BigDecimal.valueOf(totalMember)));

        // TODO 정산 불가능 금액 로직 처리 필요(일단 로그 남김)
        log.info("정산 불가능 금액 = {}", impossibleAmount);

        // 정산 필요 금액 = 총합 - 정산 불가능 금액
        BigDecimal possibleAmount = totalAmount.subtract(impossibleAmount);

        // 각 원소의 평균 - balance 값을 계산하여 userSettleBalanceMap에 저장
        Map<Long, BigDecimal> userSettleBalanceMap = new HashMap<>();
        userBalanceMap.forEach((userId, balance) -> userSettleBalanceMap.put(userId, balance.subtract(averageAmount)));

        // 정산 필요 금액
        BigDecimal shouldSettleAmount = userSettleBalanceMap.values().stream()
                .filter(balance -> balance.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        shouldSettleAmount = shouldSettleAmount.subtract(impossibleAmount);

        // 결과
        AtomicReference<BigDecimal> shouldSettleAmountRef = new AtomicReference<>(shouldSettleAmount);
        List<BalanceResult> result = new ArrayList<>();
        while (!shouldSettleAmountRef.get().equals(BigDecimal.ZERO)) {
            // 받아야하는 금액
            Map<Long, BigDecimal> receivers = new HashMap<>();
            userSettleBalanceMap.forEach((userId, balance) -> {
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    receivers.put(userId, balance);
                }
            });

            // 줘야하는 금액
            Map<Long, BigDecimal> senders = new HashMap<>();
            userSettleBalanceMap.forEach((userId, balance) -> {
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    senders.put(userId, balance);
                }
            });

            // 입금 받아야하는 멤버
            receivers.forEach((rUserId, rBalance) -> {
                senders.forEach((sUserId, sBalance) -> {
                    BigDecimal absBalance = sBalance.abs();

                    // 입금이 가능하면, 입금
                    if (rBalance.compareTo(absBalance) >= 0) {
                        String senderMemberName = expensesWithMember.stream()
                                .filter(expense -> expense.getPayerMember().getId().equals(sUserId))
                                .findFirst()
                                .map(expense -> expense.getPayerMember().getName()).orElse("알 수 없습니다.");

                        String receiverMemberName = expensesWithMember.stream()
                                .filter(expense -> expense.getPayerMember().getId().equals(rUserId))
                                .findFirst()
                                .map(expense -> expense.getPayerMember().getName()).orElse("알 수 없습니다.");

                        result.add(BalanceResult.builder()
                                .senderMemberId(sUserId)
                                .senderMemberName(senderMemberName)
                                .sendAmount(absBalance)
                                .receiverMemberId(rUserId)
                                .receiverMemberName(receiverMemberName)
                                .build()
                        );

                        // 업데이트
                        userBalanceMap.put(rUserId, rBalance.subtract(absBalance));
                        userBalanceMap.put(sUserId, sBalance.add(absBalance));
                        receivers.put(rUserId, rBalance.subtract(absBalance));
                        senders.put(sUserId, sBalance.add(absBalance));

                        shouldSettleAmountRef.set(shouldSettleAmountRef.get().subtract(absBalance));
                    }
                });
            });
        }

        // TODO 완료 조건 검사 - 별도 비즈니스 로직 처리 필요.
        if (!shouldSettleAmountRef.get().equals(BigDecimal.ZERO)) {
            log.error("정산 오류로 확인이 필요합니다.");
        }

        return result;
    }

    // 특정 정산 조회
    public List<ExpenseResult> getSettlementExpenses(Long settlementId) {
        return expenseRepository.findExpensesWithMemberBySettlementId(settlementId);
    }

    // TODO 특정 정산 삭제 기능 구현(정산 ID 전체 삭제 + 부분 멤버 정산 삭제)
    public void deleteSettlement(Long settlementId, Long memberId) {

    }
}
