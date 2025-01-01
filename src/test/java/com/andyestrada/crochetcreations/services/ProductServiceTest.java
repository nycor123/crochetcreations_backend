package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.ProductImageDto;
import com.andyestrada.crochetcreations.dto.response.search.ProductSearchResultDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.ProductImage;
import com.andyestrada.crochetcreations.repositories.ImageRepository;
import com.andyestrada.crochetcreations.repositories.ProductImageRepository;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import com.andyestrada.crochetcreations.search.ProductSearchCriteriaDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceTest {

    private final ProductService _productService;
    private final ProductRepository _productRepository;
    private final ImageRepository _imageRepository;
    private final ProductImageRepository _productImageRepository;
    private final String _maxPageSize;

    @Autowired
    public ProductServiceTest(ProductService productService,
                              ProductRepository productRepository,
                              ImageRepository imageRepository,
                              ProductImageRepository productImageRepository,
                              @Value("${products.search.max_page_size}") String maxPageSize) {
        _productService = productService;
        _productRepository = productRepository;
        _imageRepository = imageRepository;
        _productImageRepository = productImageRepository;
        _maxPageSize = maxPageSize;
    }

    @BeforeEach
    public void reset() {
        _productRepository.deleteAll();
    }

    @Test
    public void canCreateNewProduct() {
        //given
        //when
        Product product = this.createProduct();
        //then
        assertEquals(product.getDateCreated().toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    /*
     Should set new Product's listedForSale property to true if:
     1. Product has an active effective price.
     */
    @Test
    public void newProductWithPriceGreaterThanZeroCanBeListedForSale() {
        //given
        BigDecimal priceAmount = new BigDecimal("100");
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .price(priceAmount)
                .listedForSale(true)
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        //when
        List<Product> products = _productService.saveAll(productDtoList).get();
        //then
        Product product = products.get(0);
        assertTrue(product.getListedForSale());
        assertEquals(priceAmount, product.getEffectivePrice().get().getAmount());
    }

    /*
     Should NOT set new Product's listedForSale property to true if:
     1. Product does not have an active effective price.
     */
    @Test
    public void newProductWithoutPriceCannotBeListedForSale() {
        //given
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .listedForSale(true)
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        String expectedMessage = "Product cannot be listed for sale without a price.";
        //when
        Exception exception = Assertions.assertThrows(Exception.class, () -> _productService.saveAll(productDtoList));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void newProductCannotHavePriceOfZeroOrLess() {
        //given
        BigDecimal priceAmount = new BigDecimal("0.00");
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .price(priceAmount)
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        String expectedMessage = "Product price amount should be greater than zero (0.00).";
        //when
        Exception exception = Assertions.assertThrows(Exception.class, () -> _productService.saveAll(productDtoList));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void productWithPriceGreaterThanZeroCanBeListedForSale() {
        //given
        ProductDto productDto = ProductDto.builder()
                .name("Test_Prodcut")
                .price(new BigDecimal("100.00"))
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        Long id = _productService.saveAll(productDtoList).get().get(0).getId();
        //when
        ProductDto updateProductDto = ProductDto.builder().listedForSale(true).build();
        _productService.updateProduct(id, updateProductDto);
        //then
        Product product = _productService.findById(id).get();
        assertTrue(product.getListedForSale());
    }

    @Test
    public void canUpdateProductPrice() {
        //given
        ProductDto productDto = ProductDto.builder().name("Test_Product").build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        Long id = _productService.saveAll(productDtoList).get().get(0).getId();
        BigDecimal newPriceAmount = new BigDecimal("100.00");
        //when
        ProductDto updateProductDto = ProductDto.builder().price(newPriceAmount).build();
        _productService.updateProduct(id, updateProductDto);
        //then
        Product product = _productService.findById(id).get();
        assertEquals(newPriceAmount, product.getEffectivePrice().get().getAmount());
    }

    @Test
    public void productWithoutPriceCannotBeListedForSale() {
        //given
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        Long id = _productService.saveAll(productDtoList).get().get(0).getId();
        String expectedMessage = "Product cannot be listed for sale without a price.";
        //when
        ProductDto updateProductDto = ProductDto.builder().listedForSale(true).build();
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> _productService.updateProduct(id, updateProductDto));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void productCannotHavePriceOfZeroOrLess() {
        //given
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        Long id = _productService.saveAll(productDtoList).get().get(0).getId();
        String expectedMessage = "Product price amount should be greater than zero (0.00).";
        //when
        ProductDto updateProductDto = ProductDto.builder().price(new BigDecimal("0.00")).build();
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> _productService.updateProduct(id, updateProductDto));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void canAddImageToProduct() {
        //given
        Product product = createProduct();
        Image image = createImage();
        //when
        ProductImageDto productImageDto = ProductImageDto.builder()
                .id(image.getId())
                .priority(1)
                .build();
        ProductDto updateProductDto = ProductDto.builder().images(Collections.singletonList(productImageDto)).build();
        _productService.updateProduct(product.getId(), updateProductDto);
        //then
        product = _productService.findById(product.getId()).orElseThrow();
        assertNotNull(product.getImages());
        List<String> productImageUrls = product.getImages().stream().map(img -> img.getUrl()).toList();
        assertTrue(productImageUrls.contains(image.getUrl()));
    }

    @Test
    public void canRemoveImageFromProduct() {
        //given
        Image image1 = createImage();
        Image image2 = createImage();
        List<Image> images = new ArrayList<>();
        images.add(image1);
        images.add(image2);
        List<ProductImageDto> productImageDtos = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ProductImageDto productImageDto = ProductImageDto.builder()
                    .id(images.get(i).getId())
                    .priority(i)
                    .build();
            productImageDtos.add(productImageDto);
        }
        ProductDto updateProductDto = ProductDto.builder().images(productImageDtos).build();
        Product product = _productService.updateProduct(createProduct().getId(), updateProductDto).orElseThrow();
        //when
        ProductImage productImage = product.getImages().get(0);
        ProductImageDto productImageDto = ProductImageDto.builder()
                .id(productImage.getId())
                .priority(1)
                .build();
        ProductDto removeImageProductDto = ProductDto.builder()
                .images(Collections.singletonList(productImageDto))
                .build();
        _productService.updateProduct(product.getId(), removeImageProductDto);
        //then
        Product productWithUpdatedImages = _productService.findById(product.getId()).orElseThrow();
        assertEquals(1, productWithUpdatedImages.getImages().size());
    }

    @Test
    public void canUpdateProductImagePriority() {
        //given
        Image image1 = createImage();
        Image image2 = createImage();
        List<Image> images = new ArrayList<>();
        images.add(image1);
        images.add(image2);
        List<ProductImageDto> productImageDtos = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ProductImageDto productImageDto = ProductImageDto.builder()
                    .id(images.get(i).getId())
                    .priority(i)
                    .build();
            productImageDtos.add(productImageDto);
        }
        ProductDto productDto = ProductDto.builder().images(productImageDtos).build();
        Product product = _productService.updateProduct(createProduct().getId(), productDto).orElseThrow();
        //when
        List<ProductImage> productImages = product.getImages();
        List<ProductImageDto> updateProductImageDtos = new ArrayList<>();
        for (int i = 0; i < productImages.size(); i++) {
            ProductImageDto productImageDto = ProductImageDto.builder()
                    .id(productImages.get(i).getId())
                    .priority(100 + i)
                    .build();
            updateProductImageDtos.add(productImageDto);
        }
        Product productWithUpdatedImages = _productService.updateProduct(product.getId(),
                ProductDto.builder().images(updateProductImageDtos).build()).orElseThrow();
        List<Integer> imagePriorities = productWithUpdatedImages.getImages().stream().map(img -> img.getPriority()).toList();
        assertEquals(imagePriorities.size(), productImages.size());
        assertTrue(imagePriorities.contains(100));
        assertTrue(imagePriorities.contains(101));
    }

    @Test
    public void orphanedImageShouldBeDeleted() {
        //given
        Product product = createProduct();
        Image image = createImage();
        ProductImageDto productImageDto = ProductImageDto.builder()
                .id(image.getId())
                .priority(1)
                .build();
        ProductDto addImageProductDto = ProductDto.builder().images(Collections.singletonList(productImageDto)).build();
        _productService.updateProduct(product.getId(), addImageProductDto);
        List<Long> productImageIds = _productService.findById(product.getId()).orElseThrow().getImages()
                .stream().map(pi -> pi.getId()).toList();
        //when
        ProductDto removeImageProductDto = ProductDto.builder().images(new ArrayList<>()).build();
        _productService.updateProduct(product.getId(), removeImageProductDto);
        //then
        Optional<Image> imageOptional = _imageRepository.findById(image.getId());
        assertTrue(imageOptional.isEmpty());
        Optional<ProductImage> productImageOptional = _productImageRepository.findById(productImageIds.get(0));
        assertTrue(productImageOptional.isEmpty());
    }

    @Test
    public void findAllCountMatchesMaxPageSize() {
        //given
        int productCount = 51;
        for (int i = 0; i < productCount; i++) {
            createProduct();
        }
        //when
        ProductSearchCriteriaDto searchCriteria = ProductSearchCriteriaDto.builder()
                .name("")
                .page(0L)
                .build();
        Long findAllProductCount = _productService.findWithCriteria(searchCriteria).orElseThrow().getPageSize();
        //then
        assertEquals(Long.valueOf(_maxPageSize), findAllProductCount);
    }

    @Test
    public void canPaginateResults() {
        //given
        int pageSize = 10;
        int productCount = 101;
        for (int i = 0; i < productCount; i++) {
            createProduct();
        }
        //when
        List<Long> productIds = new ArrayList<>();
        for (int i = 0; (i * pageSize) < productCount; i++) {
            ProductSearchCriteriaDto searchCriteria = ProductSearchCriteriaDto.builder()
                    .name("")
                    .page(Long.valueOf(i))
                    .pageSize(Long.valueOf(pageSize))
                    .build();
            List<Product> pagedResults = _productService.findWithCriteria(searchCriteria).orElseThrow().getPageData();
            productIds.addAll(pagedResults.stream().map(Product::getId).toList());
        }
        Set<Long> productIdsSet = new HashSet<>(productIds);
        //then
        assertEquals(productCount, productIdsSet.size());
    }

    @Test
    public void canSearchProductsByName() {
        //given
        String productName = "The Product";
        int productCount = 10;
        for (int i = 0; i < productCount; i++) {
            createProduct();
        }
        Product product = _productService.findWithCriteria(ProductSearchCriteriaDto.builder().name("").build())
                .orElseThrow()
                .getPageData()
                .get(0);
        _productService.updateProduct(product.getId(), ProductDto.builder().name(productName).build());
        //when
        ProductSearchCriteriaDto productSearchCriteria = ProductSearchCriteriaDto.builder()
                .name(productName)
                .build();
        ProductSearchResultDto result = _productService.findWithCriteria(productSearchCriteria).orElseThrow();
        //then
        assertEquals(productName, result.getPageData().get(0).getName());
    }

    private Product createProduct() {
        BigDecimal priceAmount = new BigDecimal("100");
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .price(priceAmount)
                .listedForSale(true)
                .build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        return _productService.saveAll(productDtoList).get().get(0);
    }

    private Image createImage() {
        Image image = Image.builder()
                .remotePublicId("public_id")
                .url("test_url")
                .build();
        return _imageRepository.save(image);
    }
}
