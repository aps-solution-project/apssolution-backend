package org.example.apssolution.dto.response.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class GetAccountResponse {

    private String id;
    private String name;
    private String email;
    private Role role;
    private LocalDate workedAt;
    private LocalDate resignedAt;
    private String profileImageUrl;

    public static GetAccountResponse from(Account account) {
        return GetAccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .role(account.getRole())
                .workedAt(account.getWorkedAt())
                .resignedAt(account.getResignedAt())
                .profileImageUrl(account.getProfileImageUrl())
                .build();
    }
}
