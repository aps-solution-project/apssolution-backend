package org.example.apssolution.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateAccountRequest {
    private String accountId;
    private String pw;
    private String name;
    private String role;
    private String email;
}
