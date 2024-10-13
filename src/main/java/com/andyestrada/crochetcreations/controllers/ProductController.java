package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final int maxPageSize;

    @Autowired
    public ProductController(ProductService productService, @Value("${products.search.max_page_size}") String maxPageSize) {
        this.productService = productService;
        this.maxPageSize = Integer.parseInt(maxPageSize);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) Map<String, String> queryParams) {
        try {
            Optional<List<Product>> productsOptional;
            int page;
            int size;
            if (!queryParams.isEmpty()) {
                page = queryParams.get("page") != null && queryParams.get("page").chars().allMatch(Character::isDigit) ?
                        Integer.parseInt(queryParams.get("page")) : 0;
                size = queryParams.get("size") != null && queryParams.get("size").chars().allMatch(Character::isDigit) ?
                        Integer.parseInt(queryParams.get("size")) : maxPageSize;
                productsOptional = productService.findWithPagination(page, size);
            } else {
                productsOptional = productService.findAll();
            }
            return productsOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(new ArrayList<>()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable long id) {
        Optional<Product> productOptional = productService.findById(id);
        return productOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<List<Product>> saveProducts(@Nullable @CookieValue("accessToken") String accessToken,
                                                      @Nullable @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                      @RequestBody List<ProductDto> productDtoList) {
        try {
            Optional<List<Product>> productsOptional = productService.saveAll(productDtoList);
            return productsOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable long id, @RequestBody ProductDto productDto) {
        try {
            Optional<Product> productOptional = productService.updateProduct(id, productDto);
            return productOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

}
