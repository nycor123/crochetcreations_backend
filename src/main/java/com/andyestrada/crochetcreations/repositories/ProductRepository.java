package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM Products WHERE Name LIKE %?1%", nativeQuery = true)
    Page<Product> findByNameContaining(String name, Pageable pageable);

    @Query(value = "SELECT COUNT(Id) FROM Products WHERE Name LIKE %?1%", nativeQuery = true)
    Long findByNameContainingCount(String name);

}
