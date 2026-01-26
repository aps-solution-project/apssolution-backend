package org.example.apssolution.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사원 등록 응답")
public class CreateAccountResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "요청 처리 성공")
    private String message;

    @Schema(description = "사원번호", example = "P26762991")
    private String accountId;

    @Schema(description = "사원명", example = "홍길동")
    private String accountName;

    @Schema(description = "이메일", example = "test@test.com")
    private String accountEmail;

    @Schema(description = "권한", example = "ADMIN")
    private Role role;
}
