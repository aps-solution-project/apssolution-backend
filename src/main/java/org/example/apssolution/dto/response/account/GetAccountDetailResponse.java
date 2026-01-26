package org.example.apssolution.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "사원 상세 조회 응답")
public class GetAccountDetailResponse {
    @Schema(description = "사원번호", example = "P26762991")
    private String accountId;

    @Schema(description = "사원명", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "test@test.com")
    private String email;

    @Schema(description = "권한", example = "ADMIN")
    private Role role;

    @Schema(description = "입사일", example = "2024-01-01")
    private LocalDate workedAt;

    @Schema(description = "퇴사일 (재직 중인 경우 null)", example = "2025-01-31", nullable = true)
    private LocalDate resignedAt;

    @Schema(description = "프로필 이미지 URL", example = "/apssolution/profile/P26762991/abc123.png", nullable = true)
    private String profileImageUrl;

    public static GetAccountDetailResponse from(Account account) {
        return GetAccountDetailResponse.builder()
                .accountId(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .role(account.getRole())
                .workedAt(account.getWorkedAt())
                .resignedAt(account.getResignedAt())
                .profileImageUrl(account.getProfileImageUrl())
                .build();
    }
}
