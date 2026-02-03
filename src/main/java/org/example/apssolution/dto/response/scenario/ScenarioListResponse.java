package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ScenarioListResponse {
    List<ScenarioListResponse.Scenario> scenarios;

    @Getter
    @Setter
    @Builder
    public static class Scenario {
        private String id;
        private String title;
        private String description;
        private String status;
        private LocalDateTime startAt;
        private Integer makespan;
        private Integer maxWorkerCount;
        private Boolean published;
        private LocalDateTime createdAt;
    }
}
