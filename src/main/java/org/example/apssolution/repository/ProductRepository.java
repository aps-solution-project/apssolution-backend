package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("""
    select p from Product p
    where p.name like %:keyword%
       or p.description like %:keyword%
    order by p.createdAt desc
""")
    List<Product> search(@Param("keyword") String keyword, Pageable pageable);

}
