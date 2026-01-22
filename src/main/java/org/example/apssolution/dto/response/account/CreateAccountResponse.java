package org.example.apssolution.dto.response.account;

import lombok.*;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountResponse {
    private boolean success;
    private String message;
    private String accountId;
    private String accountName;
    private String accountEmail;
    private Role role;
}
