package org.example.apssolution.dto.request.notice;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreateNoticeRequest {
    private String title;
    private String content;
    private String scenarioId;
    private List<MultipartFile> attachments;
}