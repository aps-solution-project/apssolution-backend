package org.example.apssolution.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Chat chat;
    @ManyToOne
    private Account account;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime leftAt;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.leftAt = null;
    }
}
