package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.CartItemDto;
import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private List<Product> products;

    @BeforeAll
    public void createUserAndProducts() {
        // create user
        String email = "john_doe@email.com";
        String password = "12345";
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        Cart cart = Cart.builder().user(user).build();
        user.setCart(cartRepository.save(cart));
        userRepository.save(user);
        // sign-in user
        SignInRequestDto signInRequestDto = SignInRequestDto.builder()
                .email(email)
                .password(password)
                .build();
        JwtAuthenticationResponseDto signInResponseDto = authenticationService.signin(signInRequestDto);
        bearerToken = signInResponseDto.getToken();
        // create products
        List<ProductDto> productDtoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ProductDto productDto = ProductDto.builder()
                    .name("ICT_Product_" + i)
                    .description("test description")
                    .price(new BigDecimal("1500"))
                    .listedForSale(true)
                    .build();
            productDtoList.add(productDto);
        }
        products = productService.saveAll(productDtoList).orElseThrow();
    }

    @Test
    public void userCanViewCart() throws Exception {
        //given
        int cartItemsCountBefore = cartService.getCartItemsForUser(user.getEmail()).orElse(new ArrayList<>()).size();
        for (Product product : products) {
            UpdateStockDto updateStockDto = UpdateStockDto.builder()
                    .productId(product.getId())
                    .quantity(1)
                    .build();
            inventoryService.updateStock(updateStockDto);
            com.andyestrada.crochetcreations.dto.CartItemDto cartItemDto = CartItemDto.builder()
                    .id(user.getCart().getId())
                    .productId(product.getId())
                    .quantity(1)
                    .build();
            cartItemService.addCartItem(cartItemDto);
        }
        int cartItemsCountAfter = cartItemsCountBefore + products.size();
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/user/cart")
                .header("Authorization", "Bearer " + bearerToken));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cartItemsCountBefore + cartItemsCountAfter)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].cart").exists())
                .andExpect(jsonPath("$[0].product").exists())
                .andExpect(jsonPath("$[0].quantity").exists());
    }

    @Test
    public void userCanAddToCart() throws Exception {
        //given
        Product product = products.get(0);
        UpdateStockDto updateStockDto = UpdateStockDto.builder()
                .productId(product.getId())
                .quantity(3)
                .build();
        inventoryService.updateStock(updateStockDto);
        int cartItemsCountBefore = cartService.getCartItemsForUser(user.getEmail()).orElse(new ArrayList<>()).size();
        //when
        com.andyestrada.crochetcreations.dto.CartItemDto cartItemDto = CartItemDto.builder()
                .id(user.getCart().getId())
                .productId(product.getId())
                .quantity(3)
                .build();
        ResultActions result = mockMvc.perform(post("/api/v1/user/cart/add")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cartItemDto))
                .characterEncoding("utf-8"));
        int cartItemsCountAfter = cartItemsCountBefore + 1;
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cartItemsCountAfter)));
        String responseString = result.andReturn().getResponse().getContentAsString();
        List<CartItemDto> cartItems = Arrays.asList(mapper.readValue(responseString, CartItemDto[].class));
        boolean cartItemAdded = cartItems.stream().anyMatch(cartItem ->
                cartItem.getProduct().getId().equals(cartItemDto.getProductId())
                    && cartItem.getQuantity().equals(cartItemDto.getQuantity()));
        assertTrue(cartItemAdded);
    }

}
