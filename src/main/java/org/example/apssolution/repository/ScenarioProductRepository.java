package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.ScenarioProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioProductRepository extends JpaRepository<ScenarioProduct,Integer> {
}
