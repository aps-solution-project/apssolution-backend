package org.example.apssolution.service.notice;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.entity.NoticeAttachment;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.dto.request.notice.CreateNoticeRequest;
import org.example.apssolution.repository.NoticeAttachmentRepository;
import org.example.apssolution.repository.NoticeRepository;
import org.example.apssolution.repository.ScenarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateNoticeService {

    private final NoticeRepository noticeRepository;
    private final ScenarioRepository scenarioRepository;
    private final NoticeAttachmentRepository attachmentRepository;

    @Transactional
    public Long create(CreateNoticeRequest request, Account me) throws IOException {

        Scenario scenario = null;
//        if (request.getScenarioId() != null) {
//            scenario = scenarioRepository.findById(request.getScenarioId())
//                    .orElseThrow(() -> new IllegalStateException("시나리오 없음"));
//        }

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(me)
                .scenario(scenario)
                .build();

        noticeRepository.save(notice);

        // 첨부파일은 선택
        List<MultipartFile> attachments = request.getAttachments();

        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (file.isEmpty()) continue;

                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                Path uploadPath = Path.of(System.getProperty("user.home"), "apssolution", "notices", uuid);

                Files.createDirectories(uploadPath);

                String originalFileName = file.getOriginalFilename();
                Path filePath = uploadPath.resolve(originalFileName);
                file.transferTo(filePath.toFile());

                String fileUrl = "/apssolution/notices/" + uuid + "/" + originalFileName;

                NoticeAttachment attachment = NoticeAttachment.builder()
                        .notice(notice)
                        .fileName(originalFileName)
                        .fileType(file.getContentType())
                        .fileUrl(fileUrl)
                        .build();

                attachmentRepository.save(attachment);
            }
        }
        return notice.getId();
    }
}
