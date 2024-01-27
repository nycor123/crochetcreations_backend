package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
