package org.example.apssolution.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    private String id;
    private String pw;
    private String name;
    private String role;
    private String email;
    private LocalDate workedAt;
    private LocalDate resignedAt;

    @PrePersist
    public void prePersist() {
        this.workedAt = LocalDate.now();
        this.resignedAt = null;
    }
}
