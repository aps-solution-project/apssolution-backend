package org.example.apssolution.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사원 정보 수정 응답")
public class EditAccountResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    @Schema(description = "응답 메시지", example = "요청 처리 성공")
    private String message;
}
