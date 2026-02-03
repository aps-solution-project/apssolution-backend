package org.example.apssolution.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Chat;
import org.example.apssolution.domain.entity.ChatMember;
import org.example.apssolution.domain.entity.ChatMessage;
import org.example.apssolution.domain.enums.MessageType;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
public class ChatListResponse {
    List<ChatRoom> myChatList;

    @Getter
    @Setter
    @Builder
    public static class ChatRoom {
        private String id;
        private String name;
        private String lastMessage;
        private MessageType lastMessageType;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastMessageTime;
        private Long unreadCount;
        private List<OtherUser> otherUsers;
    }

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

    public static ChatListResponse from(List<Chat> myChats, Account account) {
        return ChatListResponse.builder()
                .myChatList(myChats.stream()
                        .map(c -> {
                            ChatMessage lastMessage = c.getChatMessages().stream()
                                    .filter(cm -> cm.getType() != MessageType.LEAVE)
                                    .max(Comparator.comparing(ChatMessage::getTalkedAt))
                                    .orElse(null);
                            String lastMsgText;
                            switch (lastMessage.getType()) {
                                case TEXT: lastMsgText = lastMessage.getContent(); break;
                                case IMAGE: lastMsgText = "사진을 보냈습니다."; break;
                                case FILE: lastMsgText = "파일을 보냈습니다."; break;
                                default: lastMsgText = "메시지를 보냈습니다.";
                            }

                            ChatMember me = c.getChatMembers().stream()
                                    .filter(m -> m.getAccount().getId().equals(account.getId()))
                                    .findFirst().orElse(null);
                            String defaultRoomName = String.join(", ", c.getChatMembers().stream()
                                    .filter(cm -> cm.getLeftAt() == null && !cm.getAccount().getId().equals(account.getId()))
                                    .map(m -> m.getAccount().getName()).toList());

                            if (lastMessage == null || me == null) {
                                return null;
                            }

                            return ChatRoom.builder()
                                    .id(c.getId())
                                    .name((c.getRoomName() == null || c.getRoomName().isBlank()) ? defaultRoomName : c.getRoomName())
                                    .lastMessage(lastMsgText)
                                    .lastMessageType(lastMessage.getType())
                                    .lastMessageTime(lastMessage.getTalkedAt())
                                    .unreadCount(c.getChatMessages().stream()
                                            .filter(m -> !m.getTalker().getId().equals(account.getId())
                                                    && m.getTalkedAt().isAfter(me.getLastActiveAt()))
                                            .count())
                                    .otherUsers(c.getChatMembers().stream()
                                            .filter(m -> m.getLeftAt() == null && !m.getAccount().getId().equals(account.getId()))
                                            .map(a -> OtherUser.builder()
                                                    .userId(a.getAccount().getId())
                                                    .name(a.getAccount().getName())
                                                    .role(a.getAccount().getRole())
                                                    .email(a.getAccount().getEmail())
                                                    .profileImageUrl(a.getAccount().getProfileImageUrl())
                                                    .build()).toList())
                                    .build();
                        }).filter(Objects::nonNull)
                        .toList())
                .build();
    }
}
