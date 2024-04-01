package goorm.tricount.dto;

import goorm.tricount.model.Member;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRequest {
    @NotNull
    private String name;
    @NotNull
    private Long settlementId;
    @NotNull
    private Long payerMemberId;
    @NotNull
    private BigDecimal amount;

    private LocalDateTime expenseDateTime;
}
