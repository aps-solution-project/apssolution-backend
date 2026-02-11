package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.domain.entity.ScenarioSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScenarioScheduleRepository extends JpaRepository<ScenarioSchedule,Integer> {
    void deleteByScenario(Scenario scenario);
    @Query("""
        select s
        from ScenarioSchedule s
        where s.worker = :worker
          and s.endAt > :startOfDay
          and s.startAt < :endOfDay
        order by s.startAt
    """)
    List<ScenarioSchedule> findDailyScheduleByWorker(
            @Param("worker") Account worker,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
