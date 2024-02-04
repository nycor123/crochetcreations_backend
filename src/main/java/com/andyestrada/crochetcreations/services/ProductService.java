package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    public Optional<List<Product>> getAll();
    public Optional<Product> getById(long id);
    public Optional<List<Product>> saveAll(List<ProductDto> productDtos);
}
