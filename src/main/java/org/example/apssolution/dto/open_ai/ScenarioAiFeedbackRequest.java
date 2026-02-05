package org.example.apssolution.dto.open_ai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.dto.api_response.SolveApiResult;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ScenarioAiFeedbackRequest {
    ScenarioSolution scenario;
    List<ScenarioProducts> scenarioProductList;
    private Analysis analysis;

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

    @Getter
    @Setter
    public static class Analysis {

        private BottleneckTool bottleneckTool;

        // 전체 인력 가동률 (0~1)
        private Double workerUtilization;

        // 공정 간 평균 대기시간
        private Double averageIdleTimeBetweenTasks;
    }

    // -------------------------------
    // 병목 설비 정보
    // -------------------------------
    @Getter
    @Setter
    @Builder
    public static class BottleneckTool {
        private String tool;
        // 해당 설비 총 사용 시간
        private Integer totalUsageTime;
    }

    public static ScenarioAiFeedbackRequest from(Scenario scenario) {
        ScenarioAiFeedbackRequest resp = new ScenarioAiFeedbackRequest();

        ScenarioSolution scenarioSolution = ScenarioSolution.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .startAt(scenario.getStartAt())
                .makespan(scenario.getMakespan())
                .maxWorkerCount(scenario.getMaxWorkerCount())
                .build();

        List<ScenarioProducts> scenarioProducts = scenario.getScenarioProducts().stream().map(m ->
                ScenarioProducts.builder()
                .id(m.getProduct().getId())
                .name(m.getProduct().getName())
                .description(m.getProduct().getDescription())
                .scenarioSchedules(scenario.getScenarioSchedules().stream().filter(s ->
                        s.getProduct().getId().equals(m.getProduct().getId())
                ).map(s ->
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
                                .id(s.getWorker() == null ? null : s.getWorker().getId())
                                .name(s.getWorker() == null ? null : s.getWorker().getName())
                                .build())
                        .toolId(s.getTool().getId())
                        .startAt(s.getStartAt())
                        .endAt(s.getEndAt())
                        .build()).toList())
                .build()
        ).toList();
        resp.setScenario(scenarioSolution);
        resp.setScenarioProductList(scenarioProducts);
        return resp;
    }
}
