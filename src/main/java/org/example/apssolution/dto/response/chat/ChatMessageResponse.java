package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ChatMessage;
import org.example.apssolution.domain.enums.MessageType;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class ChatMessageResponse {
    private Long id;
    private String chatId;
    private OtherUser talker;
    private String content;
    private LocalDateTime talkedAt;
    private MessageType type;
    private List<Attachment> attachments;

    @Getter
    @Setter
    @Builder
    public static class OtherUser {
        private String userId;
        private String name;
        private Role role;
        private String email;
        private String profileImageUrl;
    }

    @Getter
    @Setter
    @Builder
    public static class Attachment {
        private Long id;
        private String fileName;
        private String fileUrl;
        private String fileType;
    }

    public static ChatMessageResponse from(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .chatId(m.getChat().getId())
                .talker(OtherUser.builder()
                        .userId(m.getTalker().getId())
                        .name(m.getTalker().getName())
                        .role(m.getTalker().getRole())
                        .email(m.getTalker().getEmail())
                        .profileImageUrl(m.getTalker().getProfileImageUrl())
                        .build())
                .content(m.getContent())
                .talkedAt(m.getTalkedAt())
                .type(m.getType())
                .attachments(
                        m.getAttachments() == null ? List.of() :
                                m.getAttachments().stream()
                                        .map(a -> Attachment.builder()
                                                .id(a.getId())
                                                .fileName(a.getFileName())
                                                .fileUrl(a.getFileUrl())
                                                .fileType(a.getFileType())
                                                .build())
                                        .toList()
                )
                .build();
    }

}
