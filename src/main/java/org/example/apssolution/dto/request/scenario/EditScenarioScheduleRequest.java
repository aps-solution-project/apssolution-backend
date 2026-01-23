package org.example.apssolution.dto.request.scenario;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EditScenarioScheduleRequest {
    private String workerId;
    private String toolId;
}
