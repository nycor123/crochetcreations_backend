package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.response.search.ProductSearchResultDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.search.ProductSearchCriteriaDto;
import com.andyestrada.crochetcreations.services.CustomUserDetailsService;
import com.andyestrada.crochetcreations.services.ProductService;
import com.andyestrada.crochetcreations.services.authentication.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ProductService productService;

    private final int maxPageSize;

    private final Logger _logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    public ProductController(JwtService jwtService,
                             CustomUserDetailsService userDetailsService,
                             ProductService productService,
                             @Value("${products.search.max_page_size}") int maxPageSize) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.productService = productService;
        this.maxPageSize = maxPageSize;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@Nullable @CookieValue("accessToken") String accessToken,
                                                  @PathVariable long id) {
        Optional<Product> productOptional = productService.findById(id);
        try {
            boolean hasAccesstoken = accessToken != null && !accessToken.isEmpty();
            boolean productListedForSale = productOptional.orElseThrow().getListedForSale();
            if (!hasAccesstoken && !productListedForSale) {
                return ResponseEntity.notFound().build();
            }
            if (hasAccesstoken && !productListedForSale) {
                String username = jwtService.extractUsername(accessToken);
                if (!userDetailsService.isAdmin(username)) {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (Exception e) {
            _logger.error("There was an error while trying to fetch product information.", e);
            return ResponseEntity.of(Optional.empty());
        }
        return productOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResultDto> searchProducts(@Nullable @CookieValue("accessToken") String accessToken,
                                                                 @RequestParam(required = false) Map<String, String> queryParams) {
        try {
            // Build the product search criteria.
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
            // Non-admin users should only see products that are listed for sale.
            boolean hasAccesstoken = accessToken != null && !accessToken.isEmpty();
            if (!hasAccesstoken) {
                criteria.setListedForSale(true);
            }
            if (hasAccesstoken) {
                String username = jwtService.extractUsername(accessToken);
                if (!userDetailsService.isAdmin(username)) {
                    criteria.setListedForSale(true);
                }
            }
            // Return search result.
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
