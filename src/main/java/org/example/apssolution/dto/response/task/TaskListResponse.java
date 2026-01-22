package org.example.apssolution.dto.response.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Task;

import java.util.List;

@Getter
@Setter
@Builder
public class TaskListResponse {
    List<Task> tasks;
}
