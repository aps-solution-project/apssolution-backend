package org.example.apssolution.dto.response.notice;


import lombok.Builder;
import lombok.Getter;
import org.example.apssolution.domain.entity.Notice;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeResponse {
    private Long id;
    private String title;
    private String content;
    private String writerId;
    private String scenarioId;
    private LocalDateTime createdAt;

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writerId(notice.getWriter().getName())
                .scenarioId(notice.getScenario() == null ? null : notice.getScenario().getId())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}