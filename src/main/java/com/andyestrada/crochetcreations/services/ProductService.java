package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    public Optional<List<Product>> findAll();
    public Optional<Product> findById(Long id);
    public Optional<List<Product>> saveAll(List<ProductDto> productDtos);
    public Optional<Product> updateProduct(Long id, ProductDto productDto);
}
