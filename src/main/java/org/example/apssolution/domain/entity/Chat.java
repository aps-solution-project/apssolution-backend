package org.example.apssolution.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    private String id;
    @ManyToOne
    private Account owner;
    private String roomName;
    private LocalDateTime createdAt;
    private String signature;


    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages;
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMember> chatMembers;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString().split("-")[4];
        this.createdAt = LocalDateTime.now();
    }
}
