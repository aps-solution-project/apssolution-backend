package org.example.apssolution.dto.response.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSearchResponse {
    private Long noticeId;
    private String title;
    private String content;      // 요약된 내용
    private String writerName;
    private LocalDateTime createdAt;

    // 시나리오 정보 추가
    private String scenarioId;
    private String scenarioTitle;
}
