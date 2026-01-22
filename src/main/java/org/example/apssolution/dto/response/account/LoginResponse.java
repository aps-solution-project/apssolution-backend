package org.example.apssolution.dto.response.account;

import lombok.*;
import org.example.apssolution.domain.entity.Account;

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
}
