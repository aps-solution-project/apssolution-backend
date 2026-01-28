package org.example.apssolution.dto.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Task;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ParseTaskXlsResponse {

    private List<ParseTaskItem> tasks;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ParseTaskItem {
        private String id;
        private String productId;
        private String toolCategoryId;
        private int seq;
        private String name;
        private String description;
        private int duration;
    }
}
