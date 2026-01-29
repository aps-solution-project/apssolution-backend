package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Chat;
import org.example.apssolution.domain.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ChatGroupResponse {
    private String chatRoomId;
    private String chatRoomName;
    private String ownerId;
    private LocalDateTime createdAt;
    private String signature;
    private List<Message> messages;

    @Getter
    @Setter
    @Builder
    public static class Message {
        private Long id;
        private String talkerId;
        private String talkerName;
        private String content;
        private LocalDateTime talkedAt;
        private MessageType type;
        private List<Attachment> attachments;
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

    public static ChatGroupResponse from(Chat chat, Account account) {
        return ChatGroupResponse.builder()
                .chatRoomId(chat.getId())
                .chatRoomName(chat.getRoomName() != null ? chat.getRoomName() : String.join(", ", chat.getChatMembers().stream()
                        .filter(m -> {
                            if(m.getAccount() != null){
                                return !m.getAccount().getId().equals(account.getId());
                            }else{
                                return false;
                            }
                        })
                        .map(m -> m.getAccount().getName()).toList()))
                .ownerId(chat.getOwner().getId())
                .createdAt(chat.getCreatedAt())
                .signature(chat.getSignature())
                .messages(chat.getChatMessages() == null ? List.of() : chat.getChatMessages().stream()
                        .map(m ->
                                Message.builder()
                                        .id(m.getId())
                                        .talkerId(m.getTalker().getId())
                                        .talkerName(m.getTalker().getName())
                                        .content(m.getContent())
                                        .talkedAt(m.getTalkedAt())
                                        .type(m.getType())
                                        .attachments(m.getAttachments() == null ? List.of() : m.getAttachments().stream()
                                                .map(a ->
                                                        Attachment.builder()
                                                                .id(a.getId())
                                                                .fileName(a.getFileName())
                                                                .fileUrl(a.getFileUrl())
                                                                .fileType(a.getFileType())
                                                                .build()
                                                ).toList())
                                        .build()
                        ).toList())
                .build();
    }
}
