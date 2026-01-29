package org.example.apssolution.dto.request.chat;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.enums.MessageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateMessageRequest {
    @NotEmpty
    private MessageType type;
    private String content;
    private List<MultipartFile> files;
}
