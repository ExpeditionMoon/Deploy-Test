package goorm.tricount.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupRequest {
    @NotNull
    private String loginId;
    @NotNull
    private String password;
    @NotNull
    private String name;
}
