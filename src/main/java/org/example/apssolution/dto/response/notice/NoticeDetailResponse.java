package org.example.apssolution.dto.response.notice;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class NoticeDetailResponse {
    private Long noticeId;
    private String title;
    private String content;
    private WriterInfo writer;
    private String scenarioId; // 시나리오 ID (없을 수 있음)
    private LocalDateTime createdAt;
    private List<FileDto> attachments; // 파일 정보 리스트

    @Getter
    @Setter
    @Builder
    public static class WriterInfo{
        private String id;
        private String name;
        private Role role;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    public static class FileDto {
        private String fileName;
        private String fileUrl;
        private String fileType;
    }

    public static WriterInfo fromAccount(Account account) {
        return WriterInfo.builder()
                .id(account.getId())
                .name(account.getName())
                .role(account.getRole())
                .profileImageUrl(account.getProfileImageUrl())
                .build();
    }

    public static NoticeDetailResponse from(Notice notice) {
        return NoticeDetailResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writer(fromAccount(notice.getWriter()))
                .scenarioId(notice.getScenario() != null ? String.valueOf(notice.getScenario().getId()) : null)
                .createdAt(notice.getCreatedAt())
                .attachments(notice.getAttachments().stream()
                        .map(file -> FileDto.builder()
                                .fileName(file.getFileName())
                                .fileUrl(file.getFileUrl())
                                .fileType(file.getFileType())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}