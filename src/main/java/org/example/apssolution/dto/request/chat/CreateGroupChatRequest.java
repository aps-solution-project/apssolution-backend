package org.example.apssolution.dto.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "그룹 채팅방 생성 요청 DTO")
public class CreateGroupChatRequest {

    @Schema(
            description = "채팅방에 포함될 멤버 계정 ID 목록 (요청자 제외 가능)",
            example = "[\"user_1\", \"user_2\", \"user_3\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "멤버 목록은 비어 있을 수 없습니다.")
    @Size(min = 2, message = "그룹 채팅방은 자신을 포함해 최소 3명 이상이어야 합니다.")
    private List<String> members;

    @Schema(
            description = "채팅방 이름 (미입력 시 참여자 이름 사용)",
            example = "프로젝트 협업 채팅방",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String roomName;
}
