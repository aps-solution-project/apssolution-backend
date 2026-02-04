package org.example.apssolution.repository;

import jakarta.transaction.Transactional;
import org.example.apssolution.domain.entity.ScenarioProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioProductRepository extends JpaRepository<ScenarioProduct,Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ScenarioProduct sp WHERE sp.scenario.id = :scenarioId")
    void deleteByScenarioId(@Param("scenarioId") String scenarioId);
    List<ScenarioProduct> findByScenarioId(String scenarioId);
}
