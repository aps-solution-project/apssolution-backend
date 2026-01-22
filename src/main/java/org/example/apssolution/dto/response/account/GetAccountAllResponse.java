package org.example.apssolution.dto.response.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
public class GetAccountAllResponse {
    private boolean success;
    private String message;
    private List<GetAccountDTO> accounts;
}
