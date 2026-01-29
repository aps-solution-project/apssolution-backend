package org.example.apssolution.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Task;

@Getter
@Setter
@Builder
@Schema(description = "작업 단건 조회 응답")
public class TaskResponse {

    @Schema(description = "작업 상세 정보")
    Task task;
}
