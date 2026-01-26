package org.example.apssolution.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "공통 에러 응답")
@Getter
@AllArgsConstructor
public class ErrorResponse {

    @Schema(description = "성공 여부", example = "false")
    private boolean success;

    @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
    private String message;

    @Schema(
            description = "에러 코드 (프론트/외부 연동용)",
            example = "ACCOUNT_NOT_FOUND",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String errorCode;

}
