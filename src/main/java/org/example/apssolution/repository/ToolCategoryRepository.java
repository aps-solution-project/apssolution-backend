package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolCategoryRepository extends JpaRepository<ToolCategory,String> {
}
