package org.example.apssolution.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "작업 공정 목록 응답")
public class TaskListResponse {

    @Schema(description = "작업 공정 리스트")
    private List<TaskItem> tasks;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 작업 공정 정보")
    public static class TaskItem {

        @Schema(description = "작업 ID", example = "TASK_DOUGH_001")
        private String id;

        @Schema(description = "소속 제품 ID", example = "BREAD_BAGUETTE")
        private String productId;

        @Schema(description = "사용 장비 카테고리 ID", example = "OVEN")
        private String toolCategoryId;

        @Schema(description = "공정 순서", example = "3")
        private Integer seq;

        @Schema(description = "작업명", example = "1차 발효")
        private String name;

        @Schema(description = "작업 설명", example = "반죽을 28도에서 1시간 발효")
        private String description;

        @Schema(description = "소요 시간(분)", example = "60")
        private Integer duration;
    }
}
