package org.example.apssolution.dto.response.account;

import lombok.Builder;
import lombok.Getter;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Getter
@Builder
public class GetAccountDTO {
    private String accountId;
    private String accountName;
    private String accountEmail;
    private Role role;
    private LocalDate workedAt;
    private LocalDate resignedAt;
    private String profileImageUrl;
}
