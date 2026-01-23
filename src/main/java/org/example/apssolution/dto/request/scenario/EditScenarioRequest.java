package org.example.apssolution.dto.request.scenario;

import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;

import java.time.LocalDateTime;

@Getter
@Setter
public class EditScenarioRequest {
    private String title;
    private String description;
    private LocalDateTime startAt;
    private Integer maxWorkerCount;

    public Scenario toScenario(Scenario scenario) {
        if (title != null && !title.isBlank())
            scenario.setTitle(title);
        if (description != null && !description.isBlank())
            scenario.setDescription(description);
        if (startAt != null)
            scenario.setStartAt(startAt);
        if (maxWorkerCount != null && maxWorkerCount > 0)
            scenario.setMaxWorkerCount(maxWorkerCount);
        return scenario;
    }
}
