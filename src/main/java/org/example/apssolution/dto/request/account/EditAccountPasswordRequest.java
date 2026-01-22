package org.example.apssolution.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EditAccountPasswordRequest {
    @NotBlank
    private String oldPw;
    @NotBlank
    @Pattern(regexp = "(?=.*[a-z])(?=.*[0-9]).{4,}")
    private String newPw;
    @NotBlank
    private String newPwConfirm;
}
