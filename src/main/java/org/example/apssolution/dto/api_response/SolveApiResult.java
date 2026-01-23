package org.example.apssolution.dto.api_response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolveApiResult {
    private int makespan;
    private String status;
    private List<TaskSchedule> timeline;

    @Getter
    @Setter
    public static class TaskSchedule{
        private String productId;
        private String taskId;
        private String toolId;
        private int start;
        private int end;
        private int duration;
    }
}
