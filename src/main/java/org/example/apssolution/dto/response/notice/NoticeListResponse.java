package org.example.apssolution.dto.response.notice;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class NoticeListResponse {
    List<NoticeInfo> notices;

    @Getter
    @Setter
    @Builder
    public static class NoticeInfo {
        private Long id;
        private WriterInfo writer;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private int commentCount;
    }

    @Getter
    @Setter
    @Builder
    public static class WriterInfo{
        private String id;
        private String name;
        private Role role;
        private String profileImageUrl;
    }

    public static WriterInfo fromAccount(Account account) {
        return WriterInfo.builder()
                .id(account.getId())
                .name(account.getName())
                .role(account.getRole())
                .profileImageUrl(account.getProfileImageUrl())
                .build();
    }

    public static NoticeInfo from(Notice n, int commentCount){
        return NoticeInfo.builder()
                .id(n.getId())
                .writer(fromAccount(n.getWriter()))
                .title(n.getTitle())
                .content(n.getContent())
                .createdAt(n.getCreatedAt())
                .commentCount(commentCount)
                .build();
    }
}
