package org.example.apssolution.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.apssolution.domain.enums.Role;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @Column(length = 9)
    private String id;
    private String pw;
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String email;
    private LocalDate workedAt;
    private LocalDate resignedAt;
    private String profileImageUrl;

    @PrePersist
    public void prePersist() {
        this.workedAt = LocalDate.now();
        this.resignedAt = null;
    }

}
