package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Scenario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario,String> {
    @Query("""
    select distinct s
    from Scenario s
    left join s.scenarioSchedules ss
    where s.title like %:keyword%
       or s.description like %:keyword%
       or ss.task.id like %:keyword%
       or ss.tool.id like %:keyword%
    order by s.createdAt desc
""")
    List<Scenario> search(@Param("keyword") String keyword, Pageable pageable);


}
