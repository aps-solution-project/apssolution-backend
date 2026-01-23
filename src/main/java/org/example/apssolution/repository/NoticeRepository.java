package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 제목 OR 내용 검색
    List<Notice> findByTitleContainingOrContentContaining(String title, String content);

    // 시나리오별 조회
    List<Notice> findByScenario_Id(String scenarioId);

    // 작성자 기준
    List<Notice> findByWriter(Account writer);
}
