package org.example.apssolution.dto.response.task;

import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Task;

import java.util.List;

@Getter
@Setter
public class ParseTaskXlsResponse {
    List<Task> tasks;
}
