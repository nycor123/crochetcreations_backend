package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
