package org.example.apssolution.dto.response.account;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String accountId;
    private String accountName;
    private Role role;
}
