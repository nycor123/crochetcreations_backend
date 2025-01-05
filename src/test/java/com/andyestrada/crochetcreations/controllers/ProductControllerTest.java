package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.ProductImageDto;
import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.response.search.ProductSearchResultDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.ImageService;
import com.andyestrada.crochetcreations.services.ProductService;
import com.andyestrada.crochetcreations.services.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerTest {

    private final MockMvc _mockMvc;
    private final ObjectMapper _mapper;
    private final ProductService _productService;
    private final ImageService _imageService;
    private final AuthenticationService _authenticationService;
    private final UserRepository _userRepository;
    private final PasswordEncoder _passwordEncoder;

    private final String _adminEmail = "admin@email.com";
    private final String _adminPassword = "12345";

    private final String _userEmail = "test_user@email.com";
    private final String _userPassword = "12345";

    private List<Product> _savedProducts;
    private List<Long> _imageIds = new ArrayList<>();

    @Autowired
    public ProductControllerTest(MockMvc mockMvc,
                                 ObjectMapper objectMapper,
                                 ProductService productService,
                                 ImageService imageService,
                                 AuthenticationService authenticationService,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        _mockMvc = mockMvc;
        _mapper = objectMapper;
        _productService = productService;
        _imageService = imageService;
        _authenticationService = authenticationService;
        _userRepository = userRepository;
        _passwordEncoder = passwordEncoder;
    }

    @BeforeAll
    public void setup() {
        // Create admin user
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email(_adminEmail)
                .password(_passwordEncoder.encode(_adminPassword))
                .role(Role.ADMIN)
                .build();
        _userRepository.save(admin);
        // Create non-admin user
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(_userEmail)
                .password(_passwordEncoder.encode(_userPassword))
                .role(Role.USER)
                .build();
        _userRepository.save(user);
        // Create products
        List<ProductDto> productDtoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ProductDto productDto = ProductDto.builder()
                    .name("Product_" + i)
                    .description("test description")
                    .price(new BigDecimal("1"))
                    .listedForSale(true)
                    .build();
            productDtoList.add(productDto);
        }
        _savedProducts = _productService.saveAll(productDtoList).get();
    }

    @AfterAll
    public void cleanup() {
        for (Long imageId : _imageIds) {
            _imageService.deleteImage(imageId, true);
        }
    }

    @Test
    public void shouldGetProductById() throws Exception {
        //given
        Product product = _savedProducts.get(0);
        //when
        ResultActions result = _mockMvc.perform(get("/api/v1/products/" + product.getId()));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(product.getName()));
    }

    @Test
    public void shouldSaveValidNewProduct() throws Exception {
        /* GIVEN **/
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .price(new BigDecimal("100"))
                .listedForSale(true)
                .build();
        // create an Image
        File file = new File("src/test/resources/sample_image.png");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "image/png",
                IOUtils.toByteArray(input));
        ImageDto imageDto = ImageDto.builder().file(multipartFile).build();
        Image image = _imageService.uploadImage(imageDto).orElseThrow();
        // create a ProductDto associated with the created Image
        ProductImageDto productImageDto = ProductImageDto.builder()
                .id(image.getId())
                .priority(1)
                .build();
        productDto.setImages(Collections.singletonList(productImageDto));
        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(productDto);
        /* WHEN **/
        ResultActions result = _mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(_mapper.writeValueAsString(productDtos))
                .characterEncoding("utf-8"));
        /* THEN **/
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].images").isNotEmpty());
        /* CLEANUP **/
        Integer productId = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$[0].id");
        List<Image> imagesToDelete = _productService.findById(Long.valueOf(productId)).orElseThrow()
                .getImages()
                .stream()
                .map(img -> (Image) img)
                .toList();
        for (Image img : imagesToDelete) {
            _imageIds.add(img.getId());
        }
    }

    @Test
    public void shouldNotSaveInvalidNewProduct() throws Exception {
        //given
        ProductDto productDto = ProductDto.builder().description("test").build();
        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(productDto);
        //when
        ResultActions result = _mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(_mapper.writeValueAsString(productDtos))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldUpdateValidProduct() throws Exception {
        //given
        Product product = _savedProducts.get(0);
        BigDecimal newPriceAmount = new BigDecimal("100.01");
        //when
        ProductDto productDto = ProductDto.builder().price(newPriceAmount).listedForSale(true).build();
        ResultActions result = _mockMvc.perform(patch("/api/v1/products/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(_mapper.writeValueAsString(productDto))
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
        Product product = _savedProducts.get(0);
        //when
        ProductDto productDto = ProductDto.builder().listedForSale(true).build();
        ResultActions result = _mockMvc.perform(put("/api/v1/products/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(_mapper.writeValueAsString(productDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void nonAdminCannotFetchUnlistedProductById() throws Exception {
        //given
        List<ProductDto> productDtoList = new ArrayList<>();
        ProductDto productDto = ProductDto.builder()
                .name("Test_Product")
                .description("test description")
                .build();
        productDtoList.add(productDto);
        Product unlistedProduct = _productService.saveAll(productDtoList).orElseThrow().get(0);
        //when
        ResultActions result = _mockMvc.perform(get("/api/v1/products/" + unlistedProduct.getId()));
        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void adminCanFetchUnlistedProductById() throws Exception {
        //given
        List<ProductDto> productDtoList = new ArrayList<>();
        ProductDto productDto = ProductDto.builder()
                .name("Test Product")
                .description("test description")
                .build();
        productDtoList.add(productDto);
        Product unlistedProduct = _productService.saveAll(productDtoList).orElseThrow().get(0);
        SignInRequestDto signInRequestDto = SignInRequestDto.builder()
                .email(_adminEmail)
                .password(_adminPassword)
                .build();
        String accessToken = _authenticationService.signin(signInRequestDto).getToken();
        //when
        ResultActions result = _mockMvc.perform(get("/api/v1/products/" + unlistedProduct.getId())
                .cookie(new Cookie("accessToken", accessToken)));
        //then
        result.andExpect(status().is2xxSuccessful());
    }

    @Test
    public void nonAdminCannotSearchUnlistedProducts() throws Exception {
        //given
        List<ProductDto> productDtoList = new ArrayList<>();
        ProductDto productDto = ProductDto.builder()
                .name("Test Product")
                .description("test description")
                .build();
        productDtoList.add(productDto);
        _productService.saveAll(productDtoList);
        SignInRequestDto signInRequestDto = SignInRequestDto.builder()
                .email(_userEmail)
                .password(_userPassword)
                .build();
        String accessToken = _authenticationService.signin(signInRequestDto).getToken();
        //when
        ResultActions result = _mockMvc.perform(get("/api/v1/products/search")
                .cookie(new Cookie("accessToken", accessToken))
                .queryParam("name", "")
                .accept(MediaType.APPLICATION_JSON));
        String responseJson = result.andReturn().getResponse().getContentAsString();
        ProductSearchResultDto responseObj = _mapper.readValue(responseJson, ProductSearchResultDto.class);
        //then
        boolean hasUnlistedProduct = responseObj.getPageData().stream().anyMatch(product -> !product.getListedForSale());
        assertFalse(hasUnlistedProduct);
    }

    @Test
    public void adminCanSearchUnlistedProducts() throws Exception {
        //given
        List<ProductDto> productDtoList = new ArrayList<>();
        ProductDto productDto = ProductDto.builder()
                .name("Test Product")
                .description("test description")
                .build();
        productDtoList.add(productDto);
        _productService.saveAll(productDtoList);
        SignInRequestDto signInRequestDto = SignInRequestDto.builder()
                .email(_adminEmail)
                .password(_adminPassword)
                .build();
        String accessToken = _authenticationService.signin(signInRequestDto).getToken();
        //when
        ResultActions result = _mockMvc.perform(get("/api/v1/products/search")
                .cookie(new Cookie("accessToken", accessToken))
                .queryParam("name", "")
                .accept(MediaType.APPLICATION_JSON));
        String responseJson = result.andReturn().getResponse().getContentAsString();
        ProductSearchResultDto responseObj = _mapper.readValue(responseJson, ProductSearchResultDto.class);
        //then
        boolean hasUnlistedProduct = responseObj.getPageData().stream().anyMatch(product -> !product.getListedForSale());
        assertTrue(hasUnlistedProduct);
    }

}
