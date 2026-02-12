package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.domain.entity.ScenarioSchedule;
import org.example.apssolution.domain.entity.Task;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PersonalScheduleResponse {
    ScenarioSolution scenario;
    List<ScenarioProducts> products;


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

        public static ScenarioSolution from(Scenario scenario) {
            return ScenarioSolution.builder()
                    .id(scenario.getId())
                    .title(scenario.getTitle())
                    .description(scenario.getDescription())
                    .startAt(scenario.getStartAt())
                    .makespan(scenario.getMakespan())
                    .maxWorkerCount(scenario.getMaxWorkerCount())
                    .published(scenario.getPublished())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class ScenarioProducts {
        private String id;
        private String name;
        private String description;
        private List<ScenarioSchedules> scenarioSchedules;

        public static ScenarioProducts from(Product product, List<ScenarioSchedule> schedules) {
            return ScenarioProducts.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .scenarioSchedules(schedules.stream()
                            .filter(s -> s.getProduct().getId().equals(product.getId()))
                            .map(ScenarioSchedules::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    public static class ScenarioSchedules {
        private int id;
        private ScheduleTask scheduleTask;
        private String toolId;
        private String categoryId;
        private LocalDateTime startAt;
        private LocalDateTime endAt;

        public static ScenarioSchedules from(ScenarioSchedule schedule) {
            return ScenarioSchedules.builder()
                    .id(schedule.getId())
                    .scheduleTask(ScheduleTask.from(schedule.getTask()))
                    .toolId(schedule.getTool().getId())
                    .categoryId(schedule.getTool().getCategory().getId())
                    .startAt(schedule.getStartAt())
                    .endAt(schedule.getEndAt())
                    .build();
        }
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

        public static ScheduleTask from(Task task) {
            return ScheduleTask.builder()
                    .id(task.getId())
                    .seq(task.getSeq())
                    .name(task.getName())
                    .description(task.getDescription())
                    .duration(task.getDuration())
                    .build();
        }
    }


    public static PersonalScheduleResponse from(Scenario scenario, List<ScenarioSchedule> scenarioSchedules) {
        return PersonalScheduleResponse.builder()
                .scenario(ScenarioSolution.from(scenario))
                .products(scenarioSchedules.stream().map(ScenarioSchedule::getProduct)
                        .distinct().map(p -> ScenarioProducts.from(p, scenarioSchedules))
                        .toList())
                .build();
    }
}
