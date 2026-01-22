package org.example.apssolution.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginAccountRequest {
    @NotBlank
    private String accountId;
    @NotBlank
    private String pw;
}
