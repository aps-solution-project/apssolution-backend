package org.example.apssolution.dto.response.service;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResultResponse {
    private boolean success;
    private String message;
}
