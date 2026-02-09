package org.example.apssolution.dto.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.enums.MessageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "채팅 메시지 전송 요청 (multipart/form-data)")
public class CreateMessageRequest {

    @Schema(
            description = "메시지 타입 (TEXT 또는 FILE)",
            example = "TEXT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "메시지 타입은 필수입니다.")
    private MessageType type;

    @Schema(
            description = "텍스트 메시지 내용 (TEXT 타입일 때 필수)",
            example = "안녕하세요"
    )
    private String content;

    @Schema(
            description = "첨부 파일 목록 (FILE 타입일 때 사용, 다중 업로드 가능)",
            type = "array",
            format = "binary"
    )
    private List<MultipartFile> files;
}
