package org.example.apssolution.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "작업 공정 엑셀 업로드 요청")
public record ParseXlsRequest(

        @Schema(
                description = "작업 공정 정보가 포함된 엑셀 파일",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        MultipartFile file

) {}
