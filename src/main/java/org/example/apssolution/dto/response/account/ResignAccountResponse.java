package org.example.apssolution.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@Schema(description = "퇴직 처리 응답")
public class ResignAccountResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "요청 처리 성공")
    private String message;
}
