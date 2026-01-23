package org.example.apssolution.dto.request.notice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
public class EditNoticeRequest {
    private String title;
    private String content;
    private List<MultipartFile> attachments;
}
