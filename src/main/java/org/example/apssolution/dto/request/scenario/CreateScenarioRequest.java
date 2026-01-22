package org.example.apssolution.dto.request.scenario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ScenarioProduct;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CreateScenarioRequest {
    @NotBlank
    private String title;
    private String description;
    private LocalDateTime startAt;
    private Integer maxWorkerCount;
    @Valid
    @NotEmpty
    private List<CreateScenarioRequest.Item> scenarioProduct;

    @Getter
    @Setter
    public static class Item{
        @NotBlank
        private String productId;
        @Positive
        private Integer qty;
    }
}
