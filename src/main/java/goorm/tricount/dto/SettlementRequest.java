package goorm.tricount.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementRequest {
    @NotNull
    private String settlementName;
}
