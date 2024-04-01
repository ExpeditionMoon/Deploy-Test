package goorm.tricount.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @NotNull
    private String loginId;
    @NotNull
    private String password;
}
