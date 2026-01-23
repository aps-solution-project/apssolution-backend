package org.example.apssolution.service.notice;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchNoticeService {

    private final NoticeRepository noticeRepository;

    public List<Notice> getAll() {
        return noticeRepository.findAll();
    }

    public List<Notice> search(String keyword, String scenarioId) {
        if (keyword != null && !keyword.isBlank()) {
            return noticeRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        }

        if (scenarioId != null) {
            return noticeRepository.findByScenario_Id(scenarioId);
        }

        return noticeRepository.findAll();
    }

    public List<Notice> myNotices(Account me) {
        return noticeRepository.findByWriter(me);
    }
}
