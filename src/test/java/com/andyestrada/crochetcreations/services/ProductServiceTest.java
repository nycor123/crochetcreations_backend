package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.repositories.ImageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageRepository imageRepository;

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
        List<Product> products = productService.saveAll(productDtoList).get();
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
        Exception exception = Assertions.assertThrows(Exception.class, () -> productService.saveAll(productDtoList));
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
        Exception exception = Assertions.assertThrows(Exception.class, () -> productService.saveAll(productDtoList));
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
        Long id = productService.saveAll(productDtoList).get().get(0).getId();
        //when
        ProductDto updateProductDto = ProductDto.builder().listedForSale(true).build();
        productService.updateProduct(id, updateProductDto);
        //then
        Product product = productService.findById(id).get();
        assertTrue(product.getListedForSale());
    }

    @Test
    public void canUpdateProductPrice() {
        //given
        ProductDto productDto = ProductDto.builder().name("Test_Product").build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto);
        Long id = productService.saveAll(productDtoList).get().get(0).getId();
        BigDecimal newPriceAmount = new BigDecimal("100.00");
        //when
        ProductDto updateProductDto = ProductDto.builder().price(newPriceAmount).build();
        productService.updateProduct(id, updateProductDto);
        //then
        Product product = productService.findById(id).get();
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
        Long id = productService.saveAll(productDtoList).get().get(0).getId();
        String expectedMessage = "Product cannot be listed for sale without a price.";
        //when
        ProductDto updateProductDto = ProductDto.builder().listedForSale(true).build();
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> productService.updateProduct(id, updateProductDto));
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
        Long id = productService.saveAll(productDtoList).get().get(0).getId();
        String expectedMessage = "Product price amount should be greater than zero (0.00).";
        //when
        ProductDto updateProductDto = ProductDto.builder().price(new BigDecimal("0.00")).build();
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> productService.updateProduct(id, updateProductDto));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void canAddImageToProduct() {
        //given
        Product product = createProduct();
        Image image = createImage();
        List<Long> imageIds = new ArrayList<>();
        imageIds.add(image.getId());
        //when
        ProductDto updateProductDto = ProductDto.builder().imageIds(imageIds).build();
        productService.updateProduct(product.getId(), updateProductDto);
        //then
        product = productService.findById(product.getId()).get();
        assertNotNull(product.getImages());
        List<Long> updatedProductImageIds = product.getImages().stream().map(img -> img.getId()).toList();
        assertTrue(updatedProductImageIds.contains(image.getId()));
    }

    @Test
    public void canRemoveImageFromProduct() {
        //given
        Product product = createProduct();
        Image image1 = createImage();
        Image image2 = createImage();
        List<Long> imageIds = new ArrayList<>();
        imageIds.add(image1.getId());
        imageIds.add(image2.getId());
        ProductDto updateProductDto = ProductDto.builder().imageIds(imageIds).build();
        productService.updateProduct(product.getId(), updateProductDto);
        //when
        List<Long> updatedImageIds = new ArrayList<>();
        updatedImageIds.add(image1.getId());
        ProductDto removeImageProductDto = ProductDto.builder().imageIds(updatedImageIds).build();
        productService.updateProduct(product.getId(), removeImageProductDto);
        //then
        Product updatedProduct = productService.findById(product.getId()).get();
        assertTrue(updatedProduct.getImages().size() == 1);
    }

    @Test
    public void orphanedImageShouldBeDeleted() {
        //given
        Product product = createProduct();
        Image image = createImage();
        List<Long> imageIdList = new ArrayList<>();
        imageIdList.add(image.getId());
        ProductDto addImageProductDto = ProductDto.builder().imageIds(imageIdList).build();
        productService.updateProduct(product.getId(), addImageProductDto);
        //when
        List<Long> emptyImageIdList = new ArrayList<>();
        ProductDto removeImageProductDto = ProductDto.builder().imageIds(emptyImageIdList).build();
        productService.updateProduct(product.getId(), removeImageProductDto);
        //then
        Optional<Image> imageOptional = imageRepository.findById(image.getId());
        assertTrue(imageOptional.isEmpty());
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
        return productService.saveAll(productDtoList).get().get(0);
    }

    private Image createImage() {
        Image image = Image.builder()
                .remotePublicId("public_id")
                .url("test_url")
                .build();
        return imageRepository.save(image);
    }

}
