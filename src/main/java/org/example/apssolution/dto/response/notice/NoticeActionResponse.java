package org.example.apssolution.dto.response.notice;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeActionResponse {
    private boolean success;
    private String message;
    private Long noticeId; // 생성/수정된 게시글의 ID를 돌려주면 프론트가 상세 페이지로 이동하기 편합니다.
    private String title;
    private String content;
    private String writerName;

    public static NoticeActionResponse of(boolean success, String message, Long id) {
        return NoticeActionResponse.builder()
                .success(success)
                .message(message)
                .noticeId(id)
                .build();
    }
}