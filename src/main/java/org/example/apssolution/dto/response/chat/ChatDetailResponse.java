package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Chat;
import org.example.apssolution.domain.entity.ChatMember;
import org.example.apssolution.domain.entity.ChatMessage;
import org.example.apssolution.domain.enums.MessageType;
import org.example.apssolution.domain.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@Builder
public class ChatDetailResponse {
    private String chatRoomId;
    private String chatRoomName;
    private List<OtherUser> otherUsers;
    private List<Message> messages;

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

    @Getter
    @Setter
    @Builder
    public static class Message {
        private Long id;
        private OtherUser talker;
        private String content;
        private LocalDateTime talkedAt;
        private MessageType type;
        private List<Attachment> attachments;
    }

    public static ChatDetailResponse from(Chat chat, Account account, ChatMember chatMember) {
        List<ChatMember> members = chat.getChatMembers() == null ? List.of() : chat.getChatMembers();
        String defaultRoomName = String.join(", ", members.stream()
                .filter(cm -> cm.getLeftAt() == null && !cm.getAccount().getId().equals(account.getId()))
                .map(m -> m.getAccount().getName()).toList());
        List<ChatMessage> chatMessages =
                chat.getChatMessages() == null ? List.of() : chat.getChatMessages();

        return ChatDetailResponse.builder()
                .chatRoomId(chat.getId())
                .chatRoomName(chat.getRoomName().isBlank() ? defaultRoomName : chat.getRoomName())
                .otherUsers(members.stream()
                        .filter(m -> !m.getAccount().getId().equals(account.getId()))
                        .map(m -> OtherUser.builder()
                                .userId(m.getAccount().getId())
                                .name(m.getAccount().getName())
                                .role(m.getAccount().getRole())
                                .email(m.getAccount().getEmail())
                                .profileImageUrl(m.getAccount().getProfileImageUrl())
                                .build())
                        .toList())
                .messages(chatMessages.stream()
                        .filter(m ->
                                m.getTalkedAt() != null &&
                                        chatMember.getJoinedAt() != null &&
                                        m.getTalkedAt().isAfter(chatMember.getJoinedAt())
                        )
                        .map(m -> Message.builder()
                                .id(m.getId())
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
                                .attachments(m.getAttachments() == null ? List.of() : m.getAttachments().stream()
                                        .map(a -> Attachment.builder()
                                                .id(a.getId())
                                                .fileName(a.getFileName())
                                                .fileUrl(a.getFileUrl())
                                                .fileType(a.getFileType())
                                                .build())
                                        .toList())
                                .build())
                        .sorted(Comparator.comparing(Message::getTalkedAt).reversed())
                        .toList())
                .build();
    }


}
