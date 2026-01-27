package org.example.apssolution.dto.response.task;

import lombok.*;
import org.example.apssolution.domain.entity.Task;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponse {
    private List<TaskItem> tasks;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskItem {
        private String id;
        private String productId;
        private String categoryId;
        private Integer seq;
        private String name;
        private String description;
        private Integer duration;

    }

}
