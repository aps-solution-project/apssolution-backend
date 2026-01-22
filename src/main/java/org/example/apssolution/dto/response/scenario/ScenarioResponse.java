package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ScenarioResponse {
    ScenarioSolution scenario;
    List<ScenarioProducts> scenarioProductList;

    @Getter
    @Setter
    @Builder
    public static class ScenarioSolution {
        private String id;
        private String title;
        private String description;
        private LocalDateTime startAt;
        private Integer makespan;
        private Integer maxWorkerCount;
        private Boolean published;
    }

    @Getter
    @Setter
    @Builder
    public static class ScenarioProducts {
        private String id;
        private String name;
        private String description;
        private List<ScenarioSchedules> scenarioSchedules;
    }

    @Getter
    @Setter
    @Builder
    public static class ScenarioSchedules {
        private int id;
        private ScheduleTask scheduleTask;
        private Worker worker;
        private String toolId;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
    }

    @Getter
    @Setter
    @Builder
    public static class ScheduleTask {
        private String id;
        private int seq;
        private String name;
        private String description;
        private int duration;
    }

    @Getter
    @Setter
    @Builder
    public static class Worker {
        private String id;
        private String name;
    }

    public static List<ScenarioProducts> from(Scenario scenario) {
        return scenario.getScenarioProducts().stream().map(m ->
                ScenarioProducts.builder()
                .id(m.getProduct().getId())
                .name(m.getProduct().getName())
                .description(m.getProduct().getDescription())
                .scenarioSchedules(scenario.getScenarioSchedules().stream().map(s ->
                        ScenarioSchedules.builder()
                        .id(s.getId())
                        .scheduleTask(ScheduleTask.builder()
                                .id(s.getTask().getId())
                                .seq(s.getTask().getSeq())
                                .name(s.getTask().getName())
                                .description(s.getTask().getDescription())
                                .duration(s.getTask().getDuration())
                                .build())
                        .worker(Worker.builder()
                                .id(s.getWorker().getId())
                                .name(s.getWorker().getName())
                                .build())
                        .toolId(s.getTool().getId())
                        .startAt(s.getStartAt())
                        .endAt(s.getEndAt())
                        .build()).toList())
                .build()
        ).toList();
    }
}
