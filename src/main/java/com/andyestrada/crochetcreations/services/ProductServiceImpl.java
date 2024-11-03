package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.ProductImageDto;
import com.andyestrada.crochetcreations.dto.response.search.ProductSearchDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.ProductImage;
import com.andyestrada.crochetcreations.entities.ProductPrice;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import com.andyestrada.crochetcreations.search.ProductSearchCriteria;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final int maxPageSize;

    private final Logger _logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final static BigDecimal MINIMUM_PRICE = new BigDecimal("0.01");

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ImageService imageService,
                              @Value("${products.search.max_page_size}") int maxPageSize) {
        this.productRepository = productRepository;
        this.imageService = imageService;
        this.maxPageSize = maxPageSize;
    }

    @Override
    public Optional<List<Product>> findAll() {
        return findWithPagination(0, maxPageSize);
    }

    @Override
    public Optional<List<Product>> findWithPagination(int page, int size) {
        size = Math.min(size, maxPageSize);
        try {
            Sort sort = Sort.by(Sort.Direction.ASC, "id");
            List<Product> result = productRepository.findAll(PageRequest.of(page, size, sort)).get().toList();
            return Optional.of(result);
        } catch (Exception e) {
            _logger.error("ProductService::findAll | An exception occurred while trying to find all products.", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProductSearchDto> findWithCriteria(ProductSearchCriteria criteria) {
        ProductSearchDto result = ProductSearchDto.builder().pageData(new ArrayList<>()).build();
        Long page = criteria.getPage() != null ? criteria.getPage() : 0;
        Long pageSize = criteria.getPageSize() != null ?
                (criteria.getPageSize() <= Integer.valueOf(maxPageSize).longValue()) ? criteria.getPageSize() : maxPageSize
                : maxPageSize;
        Sort.Direction direction = criteria.getSortDirection() != null ? criteria.getSortDirection() : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page.intValue(), pageSize.intValue(), Sort.by(direction, "id"));

        if (criteria.getSortBy() != null) {
            pageable = PageRequest.of(page.intValue(), pageSize.intValue(), Sort.by(direction, criteria.getSortBy()));
        }

        if (criteria.getName() != null) {
            List<Product> products = productRepository.findByNameContaining(criteria.getName(), pageable).get().toList();
            result.setNumberOfResults(productRepository.findByNameContainingCount(criteria.getName()));
            result.getPageData().addAll(products);
        }

        result.setPageIndex(page);
        result.setPageSize(pageSize);

        return Optional.of(result);
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
            updateImages(product, productDto.getImages());
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
        if (productDto.getName() != null) {
            productToUpdate.setName(productDto.getName());
        }
        if (productDto.getDescription() != null) {
            productToUpdate.setDescription(productDto.getDescription());
        }
        if (productDto.getPrice() != null) {
            this.updatePrice(productToUpdate, productDto.getPrice());
        }
        if (productDto.getListedForSale() != null) {
            productToUpdate.setListedForSale(productDto.getListedForSale());
        }
        if (productDto.getImages() != null) {
            updateImages(productToUpdate, productDto.getImages());
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

    private void updateImages(Product product, List<ProductImageDto> productImageDtoList) {
        try {
            if (productImageDtoList != null) {
                List<ProductImage> updatedProductImages = new ArrayList<>();
                for (ProductImageDto productImageDto : productImageDtoList) {
                    Image image = imageService.findById(productImageDto.getId()).orElseThrow();
                    ProductImage productImage = image instanceof ProductImage ? (ProductImage) image : convertImageToProductImage(image);
                    productImage.setProduct(product);
                    productImage.setPriority(productImageDto.getPriority());
                    updatedProductImages.add(productImage);
                }
                if (product.getImages() != null) {
                    List<Long> existingImageList = product.getImages().stream().map(pi -> pi.getId()).toList();
                    List<Long> updatedImageList = updatedProductImages.stream().map(pi -> pi.getId()).toList();
                    for (Long imageId : existingImageList) {
                        if (!updatedImageList.contains(imageId)) {
                            imageService.deleteImage(imageId, true);
                        }
                    }
                }
                product.setImages(updatedProductImages);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while trying to update product's images.", e);
        }
    }

    private ProductImage convertImageToProductImage(Image image) {
        ProductImage productImage = new ProductImage(image);
        imageService.deleteImage(image.getId(), false);
        return productImage;
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
