package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.ScenarioWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioWorkerRepository extends JpaRepository<ScenarioWorker, Integer> {
}
