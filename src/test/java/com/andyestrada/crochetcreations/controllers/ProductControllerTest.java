package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProductService productService;

    private List<Product> savedProducts;

    @BeforeEach
    public void setup() {
        List<ProductDto> productDtoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ProductDto productDto = ProductDto.builder()
                    .name("Product_" + i)
                    .description("test description")
                    .build();
            productDtoList.add(productDto);
        }
        savedProducts = productService.saveAll(productDtoList).get();
    }

    @Test
    public void shouldGetAllProducts() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products"));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldGetProductById() throws Exception {
        //given
        Product product = savedProducts.get(0);
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/" + product.getId()));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(product.getName()));
    }

    @Test
    public void shouldSaveValidNewProduct() throws Exception {
        //given
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .price(new BigDecimal("100"))
                .listedForSale(true)
                .build();
        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(productDto);
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productDtos))
                .characterEncoding("utf-8"));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldNotSaveInvalidNewProduct() throws Exception {
        //given
        ProductDto productDto = ProductDto.builder().description("test").build();
        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(productDto);
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productDtos))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldUpdateValidProduct() throws Exception {
        //given
        Product product = savedProducts.get(0);
        BigDecimal newPriceAmount = new BigDecimal("100.01");
        //when
        ProductDto productDto = ProductDto.builder().price(newPriceAmount).listedForSale(true).build();
        ResultActions result = mockMvc.perform(patch("/api/v1/products/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productDto))
                .characterEncoding("utf-8"));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectivePrice.amount").value(newPriceAmount))
                .andExpect(jsonPath("$.listedForSale").value(true));
    }

    @Test
    public void shouldNotUpdateInvalidProduct() throws Exception {
        //given
        Product product = savedProducts.get(0);
        //when
        ProductDto productDto = ProductDto.builder().listedForSale(true).build();
        ResultActions result = mockMvc.perform(put("/api/v1/products/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().is4xxClientError());
    }

}
