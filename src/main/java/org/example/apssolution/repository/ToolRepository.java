package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolRepository extends JpaRepository<Tool,String> {
}
