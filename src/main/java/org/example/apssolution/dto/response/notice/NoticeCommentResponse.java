package org.example.apssolution.dto.response.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.NoticeComment;

import java.time.LocalDateTime;


@Schema(description = "공지사항 댓글 응답")
@Getter
@Setter
@Builder
public class NoticeCommentResponse {

    @Schema(description = "댓글 정보")
    private Comment comment;

    @Getter
    @Setter
    @Builder
    @Schema(description = "댓글 상세 정보")
    public static class Comment {

        @Schema(description = "댓글 ID", example = "15")
        private Long id;

        @Schema(description = "공지사항 ID", example = "3")
        private Long noticeId;

        @Schema(description = "작성자 계정 ID", example = "EMP001")
        private String writerId;

        @Schema(description = "댓글 내용", example = "작업 일정 확인했습니다.")
        private String content;

        @Schema(description = "부모 댓글 ID (일반 댓글이면 null)", example = "12", nullable = true)
        private Long parentCommentId;

        @Schema(description = "작성 일시", example = "2026-01-28T18:30:00")
        private LocalDateTime createdAt;
    }

    public static Comment from(NoticeComment comment){
        return Comment.builder()
                .id(comment.getId())
                .noticeId(comment.getNotice().getId())
                .writerId(comment.getWriter().getId())
                .content(comment.getContent())
                .parentCommentId(comment.getParent() == null ? null : comment.getParent().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
