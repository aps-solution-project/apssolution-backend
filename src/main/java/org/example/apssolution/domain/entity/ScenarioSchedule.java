package org.example.apssolution.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private Scenario scenario;
    @ManyToOne
    private Product product;
    @ManyToOne
    private Task task;
    @ManyToOne
    private Member worker;
    @ManyToOne
    private Tool tool;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

}
