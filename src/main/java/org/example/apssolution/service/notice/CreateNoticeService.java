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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateNoticeService {
    private final NoticeRepository noticeRepository;
    private final ScenarioRepository scenarioRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;

    @Transactional
    public void create(CreateNoticeRequest request,
                       List<MultipartFile> attachments,
                       Account writer
    ) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(writer)
                .build();

        if (request.getScenarioId() != null) {
            Scenario scenario = scenarioRepository
                    .findById(request.getScenarioId())
                    .orElseThrow(() -> new IllegalArgumentException("시나리오 없음"));
            notice.setScenario(scenario);
        }

        noticeRepository.save(notice);

        saveAttachments(notice, attachments);
    }

    private void saveAttachments(
            Notice notice,
            List<MultipartFile> attachments
    ) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        for (MultipartFile file : attachments) {
            // 실제로는 S3 업로드 같은 거 들어감
            String fileUrl = uploadFile(file);

            NoticeAttachment attachment = NoticeAttachment.builder()
                    .notice(notice)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileUrl(fileUrl)
                    .build();

            noticeAttachmentRepository.save(attachment);
        }
    }

    private String uploadFile(MultipartFile file) {
        // 임시
        return "/files/" + file.getOriginalFilename();
    }
}
