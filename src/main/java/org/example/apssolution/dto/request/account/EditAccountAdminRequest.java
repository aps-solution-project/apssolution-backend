package org.example.apssolution.dto.request.account;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class EditAccountAdminRequest {
    private String name;
    @Email
    private String email;
    private Role role;
    private LocalDate workedAt;
}
