package org.example.apssolution.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "품목에 속한 작업 공정을 생성/수정하기 위한 벌크 요청")
public class UpsertTaskRequest {

    @Valid
    @NotEmpty
    @Schema(description = "추가 또는 수정할 작업 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> tasks;

    @Getter
    @Setter
    @Schema(description = "개별 작업 공정 정보")
    public static class Item {

        @NotBlank
        @Schema(description = "작업 ID (기존 작업 수정 시 사용, 신규 생성 시에도 클라이언트에서 생성해서 전달)",
                example = "TASK_001",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String taskId;

        @NotBlank
        @Schema(description = "소속 품목 ID",
                example = "PRODUCT_A",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String productId;

        @NotBlank
        @Schema(description = "사용할 툴 카테고리 ID",
                example = "MIXER",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String categoryId;

        @Positive
        @Schema(description = "공정 순서 (작업 진행 순번)",
                example = "1",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private int seq;

        @NotBlank
        @Schema(description = "작업명",
                example = "배터 혼합",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Positive
        @Schema(description = "소요 시간 (분 단위)",
                example = "30",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private int duration;

        @Schema(description = "작업 상세 설명",
                example = "반죽 혼합 공정")
        private String description;
    }
}
