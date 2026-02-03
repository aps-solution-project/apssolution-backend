package org.example.apssolution.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "엑셀 파싱 결과 응답")
public class ParseTaskXlsResponse {

    @Schema(description = "엑셀에서 파싱된 작업 공정 목록")
    private List<ParseTaskItem> tasks;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "엑셀 한 행에서 변환된 작업 공정 정보")
    public static class ParseTaskItem {

        @Schema(description = "작업 ID", example = "TASK_BAKE_001")
        private String id;

        @Schema(description = "소속 제품 ID", example = "BREAD_BAGUETTE")
        private String productId;

        @Schema(description = "사용 장비 카테고리 ID", example = "OVEN")
        private String toolCategoryId;

        @Schema(description = "공정 순서", example = "3")
        private int seq;

        @Schema(description = "작업명", example = "굽기")
        private String name;

        @Schema(description = "작업 설명", example = "220도 오븐에서 25분간 굽기")
        private String description;

        @Schema(description = "소요 시간(분)", example = "25")
        private int duration;

        @Schema(description = "요구 인원", example = "1")
        private Integer requiredWorkers;
    }
}
