package org.example.apssolution.dto.response.chat;

import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Chat;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatDirectResponse {
    private String chatRoomId;
    private String ownerId;
    private LocalDateTime createdAt;
    private String signature;

    public static ChatDirectResponse from(Chat chat) {
        return ChatDirectResponse.builder()
                .chatRoomId(chat.getId())
                .ownerId(chat.getOwner().getId())
                .createdAt(chat.getCreatedAt())
                .signature(chat.getSignature())
                .build();
    }
}
