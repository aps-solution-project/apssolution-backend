package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.ScenarioSchedule;
import org.example.apssolution.domain.entity.Task;
import org.example.apssolution.domain.entity.Tool;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EditScenarioScheduleResponse {
    private EditScenarioScheduleResponse.Item scenarioSchedule;

    @Getter
    @Setter
    @Builder
    public static class Item{
        private Task task;
        private Account worker;
        private Tool tool;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
    }

    public static EditScenarioScheduleResponse.Item from(ScenarioSchedule scenarioSchedule) {
        return Item.builder()
                .task(scenarioSchedule.getTask())
                .worker(scenarioSchedule.getWorker())
                .tool(scenarioSchedule.getTool())
                .startAt(scenarioSchedule.getStartAt())
                .endAt(scenarioSchedule.getEndAt())
                .build();
    }
}
