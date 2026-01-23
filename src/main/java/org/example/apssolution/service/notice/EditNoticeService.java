package org.example.apssolution.service.notice;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.entity.NoticeAttachment;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.notice.CreateNoticeRequest;
import org.example.apssolution.dto.request.notice.EditNoticeRequest;
import org.example.apssolution.repository.NoticeAttachmentRepository;
import org.example.apssolution.repository.NoticeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditNoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository attachmentRepository; // 추가 필요

    @Transactional
    public void edit(Long noticeId, EditNoticeRequest request, Account me) throws IOException {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalStateException("공지 없음"));

        // 1. 권한 체크 (ID가 String인 경우와 Long인 경우를 모두 고려하여 비교)
        // 만약 Account의 ID가 String이라면 .equals()를 사용하세요.
        if (!notice.getWriter().getId().equals(me.getId()) && me.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        // 2. 제목/내용 수정
        if (request.getTitle() != null) {
            notice.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            notice.setContent(request.getContent());
        }

        // 3. 첨부파일 수정 처리 (새 파일이 들어온 경우만)
        List<MultipartFile> newFiles = request.getAttachments();
        if (newFiles != null && !newFiles.isEmpty()) {

            // 기존 물리 파일 및 DB 정보 삭제 (선택 사항: 유지하려면 이 로직 수정)
            // CascadeType.ALL과 orphanRemoval=true 설정 덕분에 리스트를 비우면 DB도 지워짐
            notice.getAttachments().clear();

            for (MultipartFile file : newFiles) {
                if (file.isEmpty()) continue;

                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                Path uploadPath = Path.of(System.getProperty("user.home"), "apssolution", "notices", uuid);
                Files.createDirectories(uploadPath);

                String originalFileName = file.getOriginalFilename();
                file.transferTo(uploadPath.resolve(originalFileName).toFile());

                String fileUrl = "/apssolution/notices/" + uuid + "/" + originalFileName;

                NoticeAttachment attachment = NoticeAttachment.builder()
                        .notice(notice)
                        .fileName(originalFileName)
                        .fileType(file.getContentType())
                        .fileUrl(fileUrl)
                        .build();

                notice.getAttachments().add(attachment);
            }
        }
    }
}