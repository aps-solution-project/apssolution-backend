package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("""
    select distinct n
    from Notice n
    left join n.writer w
    left join n.scenario s
    where n.title like %:keyword%
       or n.content like %:keyword%
       or w.id like %:keyword%
       or w.name like %:keyword%
       or s.id like %:keyword%
       or s.title like %:keyword%
       or s.description like %:keyword%
    order by n.createdAt desc
""")
    List<Notice> search(@Param("keyword") String keyword, Pageable pageable);


    // 제목 OR 내용 검색
    List<Notice> findByTitleContainingOrContentContaining(String title, String content);

    // 시나리오별 조회
    List<Notice> findByScenario_Id(String scenarioId);

    // 작성자 기준
    List<Notice> findByWriter(Account writer);
}
