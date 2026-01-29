package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,String> {
    List<Task> findByProduct(Product product);
}
