package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.NoticeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Integer> {
    int countByNoticeId(Long noticeId);
}
