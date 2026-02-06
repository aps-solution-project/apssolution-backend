package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.domain.entity.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,String> {
    @Query("""
    select t from Task t
    where t.name like %:keyword%
       or t.description like %:keyword%
""")
    List<Task> search(@Param("keyword") String keyword, Pageable pageable);

    List<Task> findByProduct(Product product);
}
