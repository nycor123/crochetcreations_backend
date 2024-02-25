package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.ProductPrice;
import com.andyestrada.crochetcreations.repositories.ImageRepository;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ImageRepository imageRepository;

    private final Logger _logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final static BigDecimal MINIMUM_PRICE = new BigDecimal("0.01");

    @Override
    public Optional<List<Product>> findAll() {
        try {
            return Optional.of(productRepository.findAll());
        } catch (Exception e) {
            _logger.error("ProductService::findAll | An exception occurred while trying to find all products.", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Product> findById(Long id) {
        try {
            Product product = productRepository.findById(id).orElseThrow();
            Hibernate.initialize(product.getPrices());
            Hibernate.initialize(product.getImages());
            return Optional.of(product);
        } catch (Exception e) {
            _logger.error("ProductService::findById | An exception occurred while trying to find product.", e);
            return Optional.empty();
        }
    }

    /**
     * Method for saving new Products.
     * @param productDtos Products to be saved.
     * @return List of saved Products.
     */
    @Override
    public Optional<List<Product>> saveAll(List<ProductDto> productDtos) {
        List<Product> products = new ArrayList<>();
        for (ProductDto productDto : productDtos) {
            Product product = Product.builder()
                    .name(productDto.getName())
                    .description(productDto.getDescription())
                    .listedForSale(productDto.getListedForSale() != null ? productDto.getListedForSale() : false)
                    .build();
            updatePrice(product, productDto.getPrice());
            products.add(product);
        }
        validateProducts(products);
        try {
            return Optional.of(productRepository.saveAll(products));
        } catch (Exception e) {
            _logger.error("ProductService::saveAll | An error occurred while trying to save product/s.", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Product> updateProduct(Long id, ProductDto productDto) {
        Product productToUpdate = this.findById(id).orElseThrow();
        if (productDto.getPrice() != null) {
            this.updatePrice(productToUpdate, productDto.getPrice());
        }
        if (productDto.getListedForSale() != null) {
            productToUpdate.setListedForSale(productDto.getListedForSale());
        }
        if (productDto.getImageIds() != null) {
            List<Long> existingImageIds = productToUpdate.getImages().stream().map(img -> img.getId()).toList();
            // add Image/s to Product
            for (Long imageId : productDto.getImageIds()) {
                if (!existingImageIds.contains(imageId)) {
                    Image imageToAdd = imageRepository.findById(imageId).orElseThrow();
                    imageToAdd.setProduct(productToUpdate);
                    productToUpdate.getImages().add(imageToAdd);
                }
            }
            // remove Image/s that are not included in productDto from Product
            List<Long> imageIdsToRemove = existingImageIds.stream()
                    .filter(existingImageId -> !productDto.getImageIds().contains(existingImageId)).toList();
            for (Long imageIdToRemove : imageIdsToRemove) {
                Image imageToRemove = imageRepository.findById(imageIdToRemove).orElseThrow();
                productToUpdate.getImages().remove(imageToRemove);
                imageRepository.delete(imageToRemove);
            }
        }
        List<Product> productsForValidation = new ArrayList<>();
        productsForValidation.add(productToUpdate);
        this.validateProducts(productsForValidation);
        Product updatedProduct = productRepository.save(productToUpdate);
        return Optional.of(updatedProduct);
    }

    /*
    Update the price of a product if amount is NOT null AND is greater than 0.01.
    Throw an exception if amount IS null AND is less than 0.01.
     */
    private void updatePrice(Product product, BigDecimal amount) {
        if (amount != null && amount.compareTo(MINIMUM_PRICE) >= 0) {
            // initialize product prices if needed
            List<ProductPrice> prices = product.getPrices();
            if (prices == null) {
                prices = new ArrayList<>();
                product.setPrices(prices);
            }
            // expire existing price/s
            for (ProductPrice price : prices) {
                if (price.getUntil() == null) {
                    price.setUntil(LocalDateTime.now());
                }
            }
            // add new price and set it as the effective price
            ProductPrice newPrice = ProductPrice.builder()
                    .product(product)
                    .amount(amount)
                    .asOfDate(LocalDateTime.now())
                    .build();
            prices.add(newPrice);
        } else if (amount != null && amount.compareTo(MINIMUM_PRICE) < 0) {
            throw new IllegalStateException("Product price amount should be greater than zero (0.00).");
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
