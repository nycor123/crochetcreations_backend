package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.ProductPrice;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

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

    /**
     * Method for saving new Products.
     * @param productDtos
     * @return List of saved Products.
     */
    public Optional<List<Product>> saveAll(List<ProductDto> productDtos) {
        List<Product> products = new ArrayList<>();
        for (ProductDto productDto : productDtos) {
            Product product = Product.builder()
                    .name(productDto.getName())
                    .description(productDto.getDescription())
                    .images(productDto.getImages())
                    .listedForSale(productDto.getListedForSale() != null ? productDto.getListedForSale() : false)
                    .build();
            updatePrice(product, productDto.getPrice());
            products.add(product);
        }
        validateProducts(products);
        try {
            return Optional.of(productRepository.saveAll(products));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Method for updating the price of a product.
     * @param product Product to be updated.
     * @param amount New price amount.
     */
    public void updatePrice(Product product, BigDecimal amount) {
        List<ProductPrice> prices = product.getPrices();
        if (prices == null) {
            prices = new ArrayList<>();
            product.setPrices(prices);
        }
        BigDecimal zeroBd = new BigDecimal("0.00");
        if (amount != null && amount.compareTo(zeroBd) > 0.00) {
            // expire existing price
            for (ProductPrice price : prices) {
                if (price.getUntil() == null) {
                    price.setUntil(LocalDateTime.now());
                }
            }
            // add new price
            ProductPrice newPrice = ProductPrice.builder()
                    .product(product)
                    .amount(amount)
                    .asOfDate(LocalDateTime.now())
                    .build();
            prices.add(newPrice);
        }
    }

    private void validateProducts(List<Product> products) {
        for (Product product : products) {
            if (product.getListedForSale()
                    && (product.getPrices() == null || product.getPrices().size() < 1)) {
                throw new IllegalStateException("Product cannot be listed for sale without a price.");
            }
        }
    }

}
