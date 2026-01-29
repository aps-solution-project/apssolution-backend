package org.example.apssolution.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.apssolution.domain.enums.MessageType;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Chat chat;
    @ManyToOne
    private Account talker;
    private String content;
    private LocalDateTime talkedAt;
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @PrePersist
    public void prePersist() {
        this.talkedAt = LocalDateTime.now();
    }
}
