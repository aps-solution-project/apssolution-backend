package org.example.apssolution.dto.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
public class EditAccountRequest {
    @Email
    String email;
    MultipartFile profileImage;
}
