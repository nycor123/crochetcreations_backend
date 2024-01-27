package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Optional<List<Product>> getAll() {
        try {
            return Optional.of(productRepository.findAll());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Product> getById(long id) {
        try {
            return productRepository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<List<Product>> saveAll(List<ProductDto> productDtos) {
        List<Product> products = new ArrayList<>();
        for (ProductDto productDto : productDtos) {
            Product product = Product.builder()
                    .name(productDto.getName())
                    .description(productDto.getDescription())
                    .images(productDto.getImages())
                    .price(productDto.getPrice())
                    .build();
            products.add(product);
        }
        try {
            return Optional.of(productRepository.saveAll(products));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
