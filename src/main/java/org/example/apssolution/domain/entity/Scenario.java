package org.example.apssolution.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {
    @Id
    private String id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime startAt;
    private Integer makespan;
    private Integer maxWorkerCount;
    private Boolean published;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString().split("-")[0].toUpperCase();
        this.status = "READY";
        this.makespan = 0;
        this.published = false;
        if(this.title == null){
            this.title = "";
        }
    }
}
