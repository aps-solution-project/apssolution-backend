package org.example.apssolution.dto.request.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "공지사항 댓글 생성 요청")
@Getter
@Setter
@Builder
public class CreateCommentRequest {

    @Schema(description = "댓글 내용", example = "작업 일정 확인했습니다.")
    @NotBlank
    private String content;

    @Schema(description = "부모 댓글 ID (대댓글인 경우만 사용)", example = "12", nullable = true)
    private Long commentId;
}

