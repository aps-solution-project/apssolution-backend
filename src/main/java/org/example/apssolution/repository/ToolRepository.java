package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool,String> {
    @Query("""
    SELECT DISTINCT t
    FROM Tool t
    JOIN t.category tc
    WHERE tc.id IN (
        SELECT DISTINCT task.toolCategory.id
        FROM Task task
        JOIN task.product p
        JOIN ScenarioProduct sp ON sp.product = p
        WHERE sp.scenario.id = :scenarioId
          AND task.toolCategory IS NOT NULL
    )
""")
    List<Tool> findToolsUsedInScenario(@Param("scenarioId") String scenarioId);

}
