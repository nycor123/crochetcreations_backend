package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Optional<List<Product>> findAll();
    Optional<List<Product>> findWithPagination(int page, int size);
    Optional<Product> findById(Long id);
    Optional<List<Product>> saveAll(List<ProductDto> productDtos);
    Optional<Product> updateProduct(Long id, ProductDto productDto);
}
