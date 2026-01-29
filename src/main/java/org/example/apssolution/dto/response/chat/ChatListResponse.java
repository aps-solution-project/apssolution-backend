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
                                    .max(Comparator.comparing(ChatMessage::getTalkedAt))
                                    .orElse(null);
                            ChatMember me = c.getChatMembers().stream()
                                    .filter(m -> m.getAccount().getId().equals(account.getId()))
                                    .findFirst().orElse(null);
                            if (lastMessage == null || me == null) {
                                return null;
                            }

                            return ChatRoom.builder()
                                    .id(c.getId())
                                    .name(c.getRoomName())
                                    .lastMessage(lastMessage.getContent() == null ? lastMessage.getType().name() : lastMessage.getContent())
                                    .lastMessageType(lastMessage.getType())
                                    .lastMessageTime(lastMessage.getTalkedAt())
                                    .unreadCount(c.getChatMessages().stream()
                                            .filter(m -> !m.getTalker().getId().equals(account.getId())
                                                    && m.getTalkedAt().isAfter(me.getLastActiveAt()))
                                            .count())
                                    .otherUsers(c.getChatMembers().stream()
                                            .filter(m -> !m.getAccount().getId().equals(account.getId()))
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
