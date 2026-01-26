package org.example.apssolution.dto.request.account;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.enums.Role;

@Getter
@Setter
@Builder
public class CreateAccountRequest {
    @NotBlank
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    @NotEmpty
    @Email
    private String email;
}
