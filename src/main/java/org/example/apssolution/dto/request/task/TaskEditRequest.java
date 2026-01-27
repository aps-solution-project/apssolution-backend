package org.example.apssolution.dto.request.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Task;

@Getter
@Setter
@Builder
public class TaskEditRequest {
    private String productId;
    private String toolCategoryId;
    private Integer seq;
    private String name;
    private String description;
    private Integer duration;
}
