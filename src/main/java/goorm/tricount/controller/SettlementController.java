package goorm.tricount.controller;

import goorm.tricount.dto.BalanceResult;
import goorm.tricount.dto.ExpenseResult;
import goorm.tricount.dto.SettlementRequest;
import goorm.tricount.model.ApiResponse;
import goorm.tricount.model.Settlement;
import goorm.tricount.service.SettlementService;
import goorm.tricount.util.MemberContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settles")
public class SettlementController {
    private final SettlementService settlementService;

    // 새로운 정산 생성
    @PostMapping("/create")
    public ApiResponse<Settlement> createSettlement(@Valid @RequestBody SettlementRequest settlementRequest) {
        return new ApiResponse<Settlement>().ok(
                settlementService.createAndJoinSettlement(
                        settlementRequest.getSettlementName(), MemberContext.getCurrentMember()));
    }

    // 정산 참여
    @PostMapping("/{id}/join")
    public ApiResponse joinSettlement(@PathVariable("id") Long settlementId) {
        settlementService.joinSettlement(settlementId, MemberContext.getCurrentMember().getId());
        return new ApiResponse().ok();
    }

    // 정산 결과 조회
    @GetMapping("/{id}/balance")
    public ApiResponse<BalanceResult> getSettlementResult(@PathVariable("id") Long settlementId) {
        return new ApiResponse<BalanceResult>().ok(settlementService.getBalanceResult(settlementId));
    }

    // 특정 정산 조회
    @GetMapping("/{id}/expenses")
    public ApiResponse<ExpenseResult> findGetSettlement(@PathVariable("id") Long settlementId) {
        return new ApiResponse<ExpenseResult>().ok(settlementService.getSettlementExpenses(settlementId));
    }

}
