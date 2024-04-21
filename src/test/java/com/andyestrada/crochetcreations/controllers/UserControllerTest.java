package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.CartItemDto;
import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.request.SignUpRequestDto;
import com.andyestrada.crochetcreations.dto.request.UpdateStockDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;
import com.andyestrada.crochetcreations.entities.*;
import com.andyestrada.crochetcreations.repositories.CartRepository;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.CartItemService;
import com.andyestrada.crochetcreations.services.CartService;
import com.andyestrada.crochetcreations.services.InventoryService;
import com.andyestrada.crochetcreations.services.ProductService;
import com.andyestrada.crochetcreations.services.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    private User user;
    private String bearerToken;

    @BeforeAll
    public void createUser() {
        // create user
        String email = "john_doe@email.com";
        String password = "12345";
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .email(email)
                .password(password)
                .firstName("John")
                .lastName("Doe")
                .build();
        authenticationService.signup(signUpRequestDto);
        user = userRepository.findByEmail(email).orElseThrow();
        // sign-in user
        SignInRequestDto signInRequestDto = SignInRequestDto.builder()
                .email(email)
                .password(password)
                .build();
        JwtAuthenticationResponseDto signInResponseDto = authenticationService.signin(signInRequestDto);
        bearerToken = signInResponseDto.getToken();
    }

    @Test
    public void canViewUserInfo() {
        // TODO
    }

    @Test
    public void canViewCart() throws Exception {
        //given
        Product product = createValidProduct();
        int cartItemQuantity = 1;
        CartItemDto cartItemDto = CartItemDto.builder()
                .id(user.getCart().getId())
                .productId(product.getId())
                .quantity(cartItemQuantity)
                .build();
        cartItemService.addCartItem(cartItemDto);
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/user/cart")
                .header("Authorization", "Bearer " + bearerToken));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].cart").exists())
                .andExpect(jsonPath("$[0].product").exists())
                .andExpect(jsonPath("$[0].quantity").exists());
    }

    @Test
    public void canAddToCart() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 3);
        //when
        CartItemDto cartItemDto = CartItemDto.builder()
                .id(user.getCart().getId())
                .productId(product.getId())
                .quantity(3)
                .build();
        ResultActions result = mockMvc.perform(post("/api/v1/user/cart/add")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cartItemDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().isOk());
        List<CartItemDto> cartItems = cartService.getCartItems(user.getEmail()).orElseThrow();
        boolean cartItemAdded = cartItems
                .stream().anyMatch(cartItem -> cartItem.getProduct().getId().equals(cartItemDto.getProductId()));
        assertTrue(cartItemAdded);
    }

    @Test
    public void sameProductAddedToCartWillIncrementCartItemQuantity() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 3);
        CartItemDto cartItemDto = cartService.addCartItem(user.getEmail(),
                        CartItemDto.builder()
                                .id(user.getCart().getId())
                                .productId(product.getId())
                                .quantity(1)
                                .build())
                .orElseThrow();
        int quantityBefore = cartService.getCartItems(user.getEmail()).orElseThrow().stream()
                .filter(ciDto -> ciDto.getProduct().getId().equals(product.getId())).findFirst().orElseThrow().getQuantity();
        //when
        int quantityAdditional = 2;
        CartItemDto cartItemUpdateDto = CartItemDto.builder()
                .id(user.getCart().getId())
                .productId(product.getId())
                .quantity(quantityAdditional)
                .build();
        ResultActions result = mockMvc.perform(post("/api/v1/user/cart/add")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cartItemUpdateDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().isOk());
        List<CartItemDto> cartItems = cartService.getCartItems(user.getEmail()).orElseThrow();
        assertEquals(1, cartItems.stream().filter(ci -> ci.getProduct().getId().equals(product.getId())).toList().size());
        assertEquals(quantityBefore + quantityAdditional,
                cartItems.stream()
                        .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                        .findFirst()
                        .orElseThrow()
                        .getQuantity());
    }

    @Test
    public void canUpdateCartItemQuantity() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 3);
        CartItemDto cartItemToUpdateDto = cartService.addCartItem(user.getEmail(),
                        CartItemDto.builder()
                                .id(user.getCart().getId())
                                .productId(product.getId())
                                .quantity(3)
                                .build())
                .orElseThrow();
        //when
        int newQuantity = 1;
        String endpoint = String.format("/api/v1/user/cart/item/%s", cartItemToUpdateDto.getId());
        CartItemDto updateCartItemDto = CartItemDto.builder().quantity(newQuantity).build();
        ResultActions result = mockMvc.perform(patch(endpoint)
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateCartItemDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().isOk());
        CartItemDto updatedCartItemDto = cartService.getCartItems(user.getEmail()).orElseThrow()
                .stream().filter(ciDto -> ciDto.getProduct().getId().equals(product.getId())).findFirst().orElseThrow();
        assertEquals(newQuantity, updatedCartItemDto.getQuantity());
    }

    @Test
    public void canDeleteCartItem() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 1);
        CartItemDto cartItemToDeleteDto = cartService.addCartItem(user.getEmail(),
                        CartItemDto.builder()
                                .id(user.getCart().getId())
                                .productId(product.getId())
                                .quantity(1)
                                .build())
                .orElseThrow();
        //when
        String endpoint = String.format("/api/v1/user/cart/item/%s", cartItemToDeleteDto.getId());
        ResultActions result = mockMvc.perform(delete(endpoint).header("Authorization", "Bearer " + bearerToken));
        //then
        result.andExpect(status().isOk());
        boolean cartItemDeleted = cartService.getCartItems(user.getEmail()).orElseThrow()
                .stream().noneMatch(ci -> ci.getId().equals(cartItemToDeleteDto.getId()));
        assertTrue(cartItemDeleted);
    }

    @Test
    public void cannotExceedProductStockWhenAddingToCart() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 1);
        //when
        CartItemDto cartItemDto = CartItemDto.builder()
                .id(user.getCart().getId())
                .productId(product.getId())
                .quantity(2)
                .build();
        ResultActions result = mockMvc.perform(post("/api/v1/user/cart/add")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cartItemDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void cannotExceedProductStockWhenAddingCartItemWithSameProduct() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 1);
        CartItemDto cartItemDto = cartService.addCartItem(user.getEmail(),
                        CartItemDto.builder()
                                .id(user.getCart().getId())
                                .productId(product.getId())
                                .quantity(1)
                                .build())
                .orElseThrow();
        int quantityBefore = cartService.getCartItems(user.getEmail()).orElseThrow().stream()
                .filter(ciDto -> ciDto.getProduct().getId().equals(product.getId())).findFirst().orElseThrow().getQuantity();
        //when
        CartItemDto cartItemUpdateDto = CartItemDto.builder()
                .id(user.getCart().getId())
                .productId(product.getId())
                .quantity(1)
                .build();
        ResultActions result = mockMvc.perform(post("/api/v1/user/cart/add")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cartItemUpdateDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void cannotExceedProductStockWhenUpdatingCartItem() throws Exception {
        //given
        Product product = createValidProduct();
        updateProductStock(product, 1);
        CartItemDto cartItemToUpdateDto = cartService.addCartItem(user.getEmail(),
                        CartItemDto.builder()
                                .id(user.getCart().getId())
                                .productId(product.getId())
                                .quantity(1)
                                .build())
                .orElseThrow();
        //when
        int newQuantity = 2;
        String endpoint = String.format("/api/v1/user/cart/item/%s", cartItemToUpdateDto.getId());
        CartItemDto updateCartItemDto = CartItemDto.builder().quantity(newQuantity).build();
        ResultActions result = mockMvc.perform(patch(endpoint)
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateCartItemDto))
                .characterEncoding("utf-8"));
        //then
        result.andExpect(status().isBadRequest());
    }

    private Product createValidProduct() {
        List<ProductDto> productDtoList = new ArrayList<>();
        ProductDto productDto = ProductDto.builder()
                .name("Test Product")
                .description("test description")
                .price(new BigDecimal("1500"))
                .listedForSale(true)
                .build();
        productDtoList.add(productDto);
        return productService.saveAll(productDtoList).orElseThrow().get(0);
    }

    private void updateProductStock(Product product, int quantity) {
        UpdateStockDto updateStockDto = UpdateStockDto.builder()
                .productId(product.getId())
                .quantity(quantity)
                .build();
        inventoryService.updateStock(updateStockDto);
    }
}
