package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ChatMessage;
import org.example.apssolution.domain.enums.MessageType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CreateMessageResponse {
    private String chatId;
    private String talkerId;
    private String content;
    private LocalDateTime talkedAt;
    private MessageType type;

    public static CreateMessageResponse from(ChatMessage chatMessage) {
        return CreateMessageResponse.builder()
                .chatId(chatMessage.getChat().getId())
                .talkerId(chatMessage.getTalker().getId())
                .content(chatMessage.getContent())
                .talkedAt(chatMessage.getTalkedAt())
                .type(chatMessage.getType())
                .build();
    }
}
