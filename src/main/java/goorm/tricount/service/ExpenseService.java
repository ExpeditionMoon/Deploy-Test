package goorm.tricount.service;

import goorm.tricount.dto.ExpenseRequest;
import goorm.tricount.dto.ExpenseResult;
import goorm.tricount.enums.TricountApiErrorCode;
import goorm.tricount.model.Expense;
import goorm.tricount.model.Member;
import goorm.tricount.model.Settlement;
import goorm.tricount.repository.ExpenseRepository;
import goorm.tricount.repository.MemberRepository;
import goorm.tricount.repository.SettlementRepository;
import goorm.tricount.util.TricountApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;

    // 지출 추가
    @Transactional
    public ExpenseResult addExpense(ExpenseRequest expenseRequest) {
        /* 예외 처리 */
        // 없는 회원 id로 요청을 보낸 경우
        Optional<Member> payer = memberRepository.findById(expenseRequest.getPayerMemberId());
        if (!payer.isPresent()) {
            throw new TricountApiException("회원 id를 찾을 수 없습니다!", TricountApiErrorCode.INVALID_INPUT_VALUE);
        }

        // 없는 정산 id로 요청을 보낸 경우
        Optional<Settlement> settlement = settlementRepository.findById(expenseRequest.getSettlementId());
        if (!settlement.isPresent()) {
            throw new TricountApiException("정산 id를 찾을 수 없습니다!", TricountApiErrorCode.INVALID_INPUT_VALUE);
        }


        // 지출 추가
        Expense expense = Expense.builder()
                .name(expenseRequest.getName())
                .settlementId(expenseRequest.getSettlementId())
                .payerMemberId(expenseRequest.getPayerMemberId())
                .amount(expenseRequest.getAmount())
                // 요청이 들어오면 그 값을, 없으면 현재 시간을 넣음
                .expenseDateTime(Objects.nonNull(expenseRequest.getExpenseDateTime()) ? expenseRequest.getExpenseDateTime() : LocalDateTime.now())
                .build();

        expenseRepository.save(expense);
        return new ExpenseResult(expense, payer.get());
    }

}
