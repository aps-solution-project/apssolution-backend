package org.example.apssolution.service.notice;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.notice.CreateNoticeRequest;
import org.example.apssolution.dto.request.notice.EditNoticeRequest;
import org.example.apssolution.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditNoticeService {
    private final NoticeRepository noticeRepository;

    @Transactional
    public void edit(Long noticeId, EditNoticeRequest request, Account me) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalStateException("공지 없음"));

        if (!notice.getWriter().getId().equals(me.getId())
                && me.getRole() != Role.ADMIN) {
            throw new IllegalStateException("수정 권한 없음");
        }

        if (request.getTitle() != null) {
            notice.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            notice.setContent(request.getContent());
        }

        if (request.getAttachments() != null) {
            // 첨부파일 수정 처리
        }
    }
}
