package org.example.apssolution.dto.response.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "품목 벌크 저장/수정 결과")
public class UpsertProductResponse {

    @Schema(description = "신규 생성된 품목 수", example = "2")
    private int created;

    @Schema(description = "수정된 품목 수", example = "3")
    private int updated;

    @Schema(description = "삭제된 품목 수", example = "1")
    private int deleted;
}
