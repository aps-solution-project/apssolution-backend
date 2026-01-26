package org.example.apssolution.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 응답")
public class LoginResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "요청 처리 성공")
    private String message;

    @Schema(description = "JWT 인증 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "사원번호", example = "P26762991")
    private String accountId;

    @Schema(description = "사원명", example = "홍길동")
    private String accountName;

    @Schema(description = "권한", example = "ADMIN")
    private Role role;
}