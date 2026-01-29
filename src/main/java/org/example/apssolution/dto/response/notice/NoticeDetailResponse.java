package org.example.apssolution.dto.response.notice;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "공지사항 상세 응답")
public class NoticeDetailResponse {

    @Schema(description = "공지사항 ID", example = "12")
    private Long noticeId;

    @Schema(description = "공지 제목", example = "2월 생산 스케줄 공유")
    private String title;

    @Schema(description = "공지 내용", example = "이번 달 제빵 라인 점검 일정 안내")
    private String content;

    @Schema(description = "작성자 정보")
    private WriterInfo writer;

    @Schema(description = "연결된 시나리오 ID", example = "SCN-2026-02", nullable = true)
    private String scenarioId;

    @Schema(description = "작성 일시", example = "2026-02-01T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "첨부파일 목록")
    private List<FileDto> attachments;


    @Getter
    @Setter
    @Builder
    @Schema(description = "작성자 정보")
    public static class WriterInfo {

        @Schema(description = "작성자 계정 ID", example = "emp01")
        private String id;

        @Schema(description = "작성자 이름", example = "김지훈")
        private String name;

        @Schema(description = "권한", example = "ADMIN")
        private Role role;

        @Schema(description = "프로필 이미지 URL", example = "/images/profile/emp01.png")
        private String profileImageUrl;
    }


    @Getter
    @Builder
    @Schema(description = "첨부 파일 정보")
    public static class FileDto {

        @Schema(description = "파일명", example = "production-plan.xlsx")
        private String fileName;

        @Schema(description = "파일 접근 URL", example = "/files/notices/12/plan.xlsx")
        private String fileUrl;

        @Schema(description = "파일 MIME 타입", example = "application/pdf")
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
