package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.NoticeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {

    // notice 필드 내부의 id를 기준으로 조회 (Id의 I는 대문자여야 함)
    List<NoticeComment> findByNoticeId(Long noticeId);

    // 또는 정렬까지 포함하고 싶다면 (생성일 순)
    List<NoticeComment> findByNoticeIdOrderByCreatedAtAsc(Long noticeId);

    // 특정 게시글의 전체 댓글 개수를 가져오는 쿼리 메서드
    int countByNoticeId(Long noticeId);
}
