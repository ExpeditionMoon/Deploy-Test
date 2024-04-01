package goorm.tricount.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResult {
    @NotNull
    private Long senderMemberId;
    @NotNull
    private String senderMemberName;
    @NotNull
    private BigDecimal sendAmount;
    @NotNull
    private Long receiverMemberId;
    @NotNull
    private String receiverMemberName;
}
