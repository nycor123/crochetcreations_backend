package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.response.search.ProductSearchDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.search.ProductSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Optional<Product> findById(Long id);
    Optional<ProductSearchDto> findWithCriteria(ProductSearchCriteria criteria);
    Optional<List<Product>> saveAll(List<ProductDto> productDtos);
    Optional<Product> updateProduct(Long id, ProductDto productDto);
}
