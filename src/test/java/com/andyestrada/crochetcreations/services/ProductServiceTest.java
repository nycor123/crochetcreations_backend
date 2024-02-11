package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

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

}
