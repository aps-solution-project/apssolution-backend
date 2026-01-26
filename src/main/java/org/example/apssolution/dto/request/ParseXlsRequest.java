package org.example.apssolution.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;


public record ParseXlsRequest(@Schema(type = "string", format = "binary", description = "엑셀 파일") MultipartFile file) {
}
