package org.example.apssolution.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record ParseXlsRequest(MultipartFile file) {
}
