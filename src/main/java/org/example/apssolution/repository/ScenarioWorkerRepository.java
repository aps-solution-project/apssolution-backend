package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.domain.entity.ScenarioWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioWorkerRepository extends JpaRepository<ScenarioWorker, Integer> {
    void deleteAllByScenario_Id(String scenarioId);
}
