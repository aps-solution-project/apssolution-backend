package org.example.apssolution.dto.request.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Scenario;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EditScenarioRequest {
    private String title;
    private String description;
    private LocalDateTime startAt;
    private Integer maxWorkerCount;
    private List<ProductInfo> scenarioProducts;


    @Getter
    @Setter
    @Builder
    public static class ProductInfo {
        private String productId;
        private Integer qty;
    }

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
