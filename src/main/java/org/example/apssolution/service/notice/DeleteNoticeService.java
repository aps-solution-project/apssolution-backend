package org.example.apssolution.service.notice;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteNoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void delete(Long noticeId, Account me) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalStateException("공지 없음"));

        if (!notice.getWriter().getId().equals(me.getId()) && me.getRole() != Role.ADMIN) {
            throw new IllegalStateException("삭제 권한 없음");
        }

        noticeRepository.delete(notice);
    }
}
