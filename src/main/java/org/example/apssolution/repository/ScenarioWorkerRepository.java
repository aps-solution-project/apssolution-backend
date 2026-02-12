package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.domain.entity.ScenarioWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioWorkerRepository extends JpaRepository<ScenarioWorker, Integer> {
    void deleteAllByScenario_Id(String scenarioId);
    Optional<ScenarioWorker> findByScenario_IdAndWorker_Id(String scenarioId, String workerId);
    Integer countByWorkerAndIsReadFalse(Account worker);
}
