package org.example.apssolution.dto.api_response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolveApiResult {
    private Integer makespan;
    private String status;
    private List<Schedule> schedules;

    @Getter
    @Setter
    @Builder
    public static class Schedule{
        private Integer duration;
        private Integer start;
        private Integer end;
        @JsonProperty("product_id")
        private String productId;
        @JsonProperty("task_id")
        private String taskId;
        @JsonProperty("tool_category_id")
        private String toolCategoryId;
        @JsonProperty("tool_id")
        private String toolId;
    }
}
