package org.example.apssolution.dto.open_ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.dto.api_response.SolveApiResult;
import org.example.apssolution.dto.request.scenario.SolveScenarioRequest;

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
    @Builder
    public static class Analysis {

        // 가장 많이 사용된 설비
        private BottleneckTool bottleneckTool;

        // 전체 인력 가동률 (0~1)
        private Double workerUtilization;

        // 공정 간 평균 대기시간
        private Double averageIdleTimeBetweenTasks;

        // 최대 동시 작업자 수
        private Double peakConcurrentWorkers;

        // 🔥 설비 전체 가동률 (0~1)
        private Double equipmentUtilization;

        // 🔥 병목 공정 정보
        private BottleneckProcess bottleneckProcess;
    }
    @Getter
    @Setter
    @Builder
    public static class BottleneckTool {

        private String tool;
        private String toolCategoryId;
        // 해당 설비 총 사용 시간
        private Integer totalUsageTime;
    }

    @Getter
    @Setter
    @Builder
    public static class BottleneckProcess {

        private String taskId;
        private String productId;

        // 해당 공정 소요시간
        private Integer duration;
    }
    public static ScenarioAiFeedbackRequest from(Scenario scenario, SolveApiResult sar) {
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

        Analysis analysis = Analysis.builder()
                .bottleneckTool(BottleneckTool.builder()
                        .tool(sar.getAnalysis().getBottleneckTool().getTool())
                        .toolCategoryId(sar.getAnalysis().getBottleneckTool().getToolCategoryId())
                        .totalUsageTime(sar.getAnalysis().getBottleneckTool().getTotalUsageTime())
                        .build())
                .workerUtilization(sar.getAnalysis().getWorkerUtilization())
                .averageIdleTimeBetweenTasks(sar.getAnalysis().getAverageIdleTimeBetweenTasks())
                .peakConcurrentWorkers(sar.getAnalysis().getPeakConcurrentWorkers())
                .equipmentUtilization(sar.getAnalysis().getEquipmentUtilization())
                .bottleneckProcess(BottleneckProcess.builder()
                        .taskId(sar.getAnalysis().getBottleneckProcess().getTaskId())
                        .productId(sar.getAnalysis().getBottleneckProcess().getProductId())
                        .duration(sar.getAnalysis().getBottleneckProcess().getDuration())
                        .build())
                .build();

        resp.setScenario(scenarioSolution);
        resp.setScenarioProductList(scenarioProducts);
        resp.setAnalysis(analysis);
        return resp;
    }
}
