package org.example.apssolution.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
@Schema(description = "전체 사원 조회 응답")
public class GetAccountAllResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    @Schema(description = "응답 메시지", example = "요청 처리 성공")
    private String message;
    @Schema(description = "사원 목록", implementation = GetAccountDTO.class)
    private List<GetAccountDTO> accounts;
}
