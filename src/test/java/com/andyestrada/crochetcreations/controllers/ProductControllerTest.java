package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import com.andyestrada.crochetcreations.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
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
        String[] imageUrls = {"link_1, link_2, link_3"};
        for (int i = 0; i < 3; i++) {
            ProductDto productDto = ProductDto.builder()
                    .name("Product_" + i)
                    .description("test description")
                    .images(imageUrls)
                    .price(new BigDecimal("100.00"))
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
    public void shouldSaveValidProduct() throws Exception {
        //given
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .price(new BigDecimal("1"))
                .build();
        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(productDto);
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products/save")
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
    public void shouldNotSaveInvalidProduct() throws Exception {
        //given
        ProductDto productDto = ProductDto.builder().name("Test_Product").build();
        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(productDto);
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productDtos))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().is4xxClientError());
    }

}
