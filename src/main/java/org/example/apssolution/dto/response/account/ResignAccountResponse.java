package org.example.apssolution.dto.response.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ResignAccountResponse {
    private boolean success;
    private String message;
}
