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
    private LocalDateTime createdAt;
    private String aiScheduleFeedback;

    @OneToMany(mappedBy = "scenario",
            cascade = CascadeType.ALL,
            orphanRemoval = true
            ,fetch = FetchType.EAGER)
    private List<ScenarioProduct> scenarioProducts;

    @OneToMany(mappedBy = "scenario",
            cascade = CascadeType.ALL,
            orphanRemoval = true
            ,fetch = FetchType.EAGER)
    private List<ScenarioSchedule> scenarioSchedules;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString().split("-")[0].toUpperCase();
        this.status = "READY";
        this.makespan = 0;
        this.published = false;
        if (this.title == null) {
            this.title = "";
        }
        if (this.description == null) {
            this.description = "";
        }
        this.createdAt = LocalDateTime.now();
    }
}
