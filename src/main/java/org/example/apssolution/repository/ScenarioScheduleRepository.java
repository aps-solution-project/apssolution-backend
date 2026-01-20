package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.ScenarioSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioScheduleRepository extends JpaRepository<ScenarioSchedule,Integer> {
}
