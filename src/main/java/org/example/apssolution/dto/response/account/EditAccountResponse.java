package org.example.apssolution.dto.response.account;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditAccountResponse {
    private boolean success;
    private String message;
}
