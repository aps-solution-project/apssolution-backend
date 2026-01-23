package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EditScenarioResponse {
    private Item editScenario;

    @Getter
    @Setter
    @Builder
    public static class Item {
        private String id;
        private String title;
        private String description;
        private String status;
        private LocalDateTime startAt;
        private Integer makespan;
        private Integer maxWorkerCount;
        private Boolean published;
    }

    public static Item from(Scenario scenario) {
        return Item.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .status(scenario.getStatus())
                .startAt(scenario.getStartAt())
                .makespan(scenario.getMakespan())
                .maxWorkerCount(scenario.getMaxWorkerCount())
                .published(scenario.getPublished())
                .build();
    }


}
