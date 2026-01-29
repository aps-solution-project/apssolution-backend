package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Chat;
import org.example.apssolution.domain.entity.ChatMessage;
import org.example.apssolution.domain.enums.MessageType;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    private List<OtherUser> otherUsers;
    private List<Message> messages;

    @Getter
    @Setter
    @Builder
    public static class OtherUser{
        private String userId;
        private String name;
        private Role role;
        private String email;
        private String profileImageUrl;
    }

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

    public static ChatGroupResponse from(Chat chat, Account account, List<Account> members) {
        return ChatGroupResponse.builder()
                .chatRoomId(chat.getId())
                .chatRoomName(chat.getRoomName() != null ? chat.getRoomName() : String.join(", ", members.stream().map(Account::getId).toList()))
                .ownerId(account.getId())
                .createdAt(chat.getCreatedAt())
                .signature(chat.getSignature())
                .otherUsers(members.stream()
                        .map(a -> OtherUser.builder()
                                .userId(a.getId())
                                .name(a.getName())
                                .email(a.getEmail())
                                .role(a.getRole())
                                .profileImageUrl(a.getProfileImageUrl())
                                .build()).toList())
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
                        ).sorted(Comparator.comparing(Message::getTalkedAt))
                        .toList())
                .build();
    }
}
