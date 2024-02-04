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
    public void newProductWithPriceCanBeListedForSale() {
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
        assertEquals(product.getEffectivePrice().get().getAmount(), priceAmount);
    }

    /*
     Should not set new Product's listedForSale property to true if:
     1. Product does not have an active effective price.
     */
    @Test
    public void newProductWithoutPriceShouldNotBeListedForSale() {
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
        assertEquals(actualMessage, expectedMessage);
    }

}
