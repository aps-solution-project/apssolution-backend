package org.example.apssolution.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "품목 벌크 저장/수정 요청")
public class UpsertProductRequest {

    @Valid
    @NotEmpty
    @Schema(description = "저장 또는 수정할 품목 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> products;

    @Getter
    @Setter
    @Schema(description = "개별 품목 정보")
    public static class Item {

        @NotBlank
        @Schema(description = "품목 ID", example = "BREAD_BAGUETTE", requiredMode = Schema.RequiredMode.REQUIRED)
        private String productId;

        @NotBlank
        @Schema(description = "품목명", example = "바게트", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "품목 설명", example = "프랑스식 하드 브레드")
        private String description;
        @Schema(description = "품목 생산라인 가동 여부", example = "true")
        private Boolean active;
    }
}
