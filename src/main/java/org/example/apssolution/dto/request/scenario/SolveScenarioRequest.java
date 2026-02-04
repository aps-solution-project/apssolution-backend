package org.example.apssolution.dto.request.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SolveScenarioRequest {
    ScenarioSolution scenario;
    List<ScenarioProducts> scenarioProductList;
    List<Tool> tools;

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
        private String productId;
        private Integer qty;
        private List<ScenarioTask> scenarioTasks;
    }

    @Getter
    @Setter
    @Builder
    public static class ScenarioTask {
        private String id;
        private ToolCategory toolCategory;
        private Integer seq;
        private String name;
        private String description;
        private Integer duration;
        private Integer requiredWorkers;
    }

    public static SolveScenarioRequest from(Scenario scenario, List<Task> myTasks, List<Tool> tools) {
        SolveScenarioRequest resp = new SolveScenarioRequest();

        SolveScenarioRequest.ScenarioSolution scenarioSolution = SolveScenarioRequest.ScenarioSolution.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .startAt(scenario.getStartAt())
                .makespan(scenario.getMakespan())
                .maxWorkerCount(scenario.getMaxWorkerCount())
                .build();

        List<SolveScenarioRequest.ScenarioProducts> scenarioProducts = scenario.getScenarioProducts().stream().map(m ->
                ScenarioProducts.builder()
                        .id(m.getProduct().getId())
                        .name(m.getProduct().getName())
                        .description(m.getProduct().getDescription())
                        .productId(m.getProduct().getId())
                        .qty(m.getQty())
                        .scenarioTasks(myTasks.stream()
                                .filter(f -> f.getProduct().getId().equals(m.getProduct().getId()))
                                .map(t -> {
                                    return ScenarioTask.builder()
                                            .id(t.getId())
                                            .toolCategory(t.getToolCategory())
                                            .seq(t.getSeq())
                                            .name(t.getName())
                                            .description(t.getDescription())
                                            .duration(t.getDuration())
                                            .requiredWorkers(t.getRequiredWorkers())
                                            .build();
                                }).toList())
                        .build()
        ).toList();

        resp.setScenario(scenarioSolution);
        resp.setScenarioProductList(scenarioProducts);
        resp.setTools(tools);
        return resp;
    }
}
