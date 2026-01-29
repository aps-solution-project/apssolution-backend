package org.example.apssolution.dto.request.chat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateGroupChatRequest {
    @NotEmpty
    @Size(min = 2)
    private List<String> members;
    private String roomName;
}
