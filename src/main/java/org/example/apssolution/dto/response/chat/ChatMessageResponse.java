package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ChatMessage;
import org.example.apssolution.domain.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class ChatMessageResponse {
    private Long id;
    private ChatDetailResponse.OtherUser talker;
    private String content;
    private LocalDateTime talkedAt;
    private MessageType type;
    private List<ChatDetailResponse.Attachment> attachments;

    public static ChatMessageResponse from(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .talker(ChatDetailResponse.OtherUser.builder()
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
                                        .map(a -> ChatDetailResponse.Attachment.builder()
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
