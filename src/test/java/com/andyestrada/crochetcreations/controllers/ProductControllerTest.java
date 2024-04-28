package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.services.ImageService;
import com.andyestrada.crochetcreations.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageService imageService;

    private List<Product> savedProducts;
    private List<Image> images = new ArrayList<>();

    @BeforeAll
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

    @AfterAll
    public void cleanup() {
        for (Image image : images) {
            imageService.deleteImage(image.getId());
        }
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

        File file = new File("src/test/resources/sample_image.png");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "image/png",
                IOUtils.toByteArray(input));
        ImageDto imageDto = ImageDto.builder().file(multipartFile).build();
        Image image = imageService.uploadImage(imageDto).orElseThrow();
        images.add(image); // for cleanup purposes
        List<Long> imageIds = new ArrayList<>();
        imageIds.add(image.getId());
        productDto.setImageIds(imageIds);

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
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].images").isNotEmpty());
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
