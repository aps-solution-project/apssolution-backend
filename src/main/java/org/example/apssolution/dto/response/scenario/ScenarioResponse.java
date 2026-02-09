package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.domain.entity.ScenarioProduct;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ScenarioResponse {
    private Item scenario;

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
        private List<ScenarioProductInfo> products;
    }

    @Getter
    @Setter
    @Builder
    public static class ScenarioProductInfo{
        private Product product;
        private Integer qty;
    }

    public static ScenarioResponse.Item from(Scenario scenario, List<ScenarioProduct> scenarioProducts) {
        return Item.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .status(scenario.getStatus())
                .startAt(scenario.getStartAt())
                .makespan(scenario.getMakespan())
                .maxWorkerCount(scenario.getMaxWorkerCount())
                .published(scenario.getPublished())
                .products(scenarioProducts.stream().map(sp ->{
                    return ScenarioProductInfo.builder()
                            .product(sp.getProduct())
                            .qty(sp.getQty())
                            .build();
                }).toList())
                .build();
    }
}
