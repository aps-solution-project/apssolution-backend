package org.example.apssolution.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.time.LocalDateTime;
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

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString().split("-")[4];
        this.createdAt = LocalDateTime.now();
    }
}
