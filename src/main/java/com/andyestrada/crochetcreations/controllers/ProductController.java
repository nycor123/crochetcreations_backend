package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.response.search.ProductSearchResultDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.search.ProductSearchCriteriaDto;
import com.andyestrada.crochetcreations.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
    public ProductController(ProductService productService,
                             @Value("${products.search.max_page_size}") int maxPageSize) {
        this.productService = productService;
        this.maxPageSize = maxPageSize;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable long id) {
        Optional<Product> productOptional = productService.findById(id);
        return productOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResultDto> searchProducts(@RequestParam(required = false) Map<String, String> queryParams) {
        try {
            ProductSearchCriteriaDto criteria = ProductSearchCriteriaDto.builder().build();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("page")) {
                    criteria.setPage(Long.valueOf(queryParams.get("page")));
                }
                if (entry.getKey().equalsIgnoreCase("pageSize")) {
                    criteria.setPageSize(Long.valueOf(queryParams.get("pageSize")));
                }
                if (entry.getKey().equalsIgnoreCase("name")) {
                    criteria.setName(queryParams.get("name"));
                }
                if (entry.getKey().equalsIgnoreCase("sortBy")) {
                    criteria.setSortBy(queryParams.get("sortBy"));
                }
                if (entry.getKey().equalsIgnoreCase("sortDirection")) {
                    Sort.Direction sortDirection = queryParams.get("sortDirection").equalsIgnoreCase("desc") ?
                            Sort.Direction.DESC : Sort.Direction.ASC;
                    criteria.setSortDirection(sortDirection);
                }
            }
            return productService.findWithCriteria(criteria).map(ResponseEntity::ok).orElseThrow();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
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
