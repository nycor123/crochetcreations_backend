package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.CartItemDto;
import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.dto.CartDto;
import com.andyestrada.crochetcreations.entities.Cart;
import com.andyestrada.crochetcreations.entities.CartItem;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.CartItemRepository;
import com.andyestrada.crochetcreations.repositories.CartRepository;
import com.andyestrada.crochetcreations.repositories.ProductRepository;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CartRepository cartRepository;

    @Autowired
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public Optional<List<CartItemDto>> getCartItemsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Hibernate.initialize(user.getCart().getCartItems());
        List<CartItem> cartItems = user.getCart().getCartItems();
        if (cartItems == null) {
            return Optional.empty();
        }
        List<CartItemDto> cartItemDtoList = cartItems.stream().map(ci -> mapCartItemToCartItemDto(user, ci)).toList();
        return Optional.of(cartItemDtoList);
    }

    @Override
    public Optional<List<CartItemDto>> addCartItemForUser(String userEmail, CartItemDto newCartItemDto) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Cart userCart = user.getCart();
        Product product = productRepository.findById(newCartItemDto.getProductId()).orElseThrow();
        List<CartItem> userCartItems = userCart.getCartItems();
        CartItem newCartItem = CartItem.builder()
                .cart(user.getCart())
                .product(product)
                .quantity(newCartItemDto.getQuantity())
                .build();
        // save new CartItem
        CartItem savedCartItem = cartItemRepository.save(newCartItem);
        userCartItems.add(savedCartItem);
        // add new CartItem to Cart
        userCart.setCartItems(userCartItems);
        cartRepository.save(userCart);
        // return updated Cart -> List<CartItem> mapped to List<CartItemDto>
        Cart updatedCart = cartRepository.findById(userCart.getId()).orElseThrow();
        List<CartItemDto> cartItemDtoList = updatedCart.getCartItems().stream()
                .map(ci -> mapCartItemToCartItemDto(user, ci)).toList();
        return Optional.of(cartItemDtoList);
    }

    private CartItemDto mapCartItemToCartItemDto(User user, CartItem cartItem) {
        Cart userCart = user.getCart();
        CartDto cartDto = CartDto.builder()
                .id(userCart.getId())
                .cartOwner(user.getEmail())
                .build();
        Product cartItemProduct = cartItem.getProduct();
        ProductDto productDto = ProductDto.builder()
                .id(cartItemProduct.getId())
                .name(cartItemProduct.getName())
                .description(cartItemProduct.getDescription())
                .price(cartItemProduct.getEffectivePrice().orElseThrow().getAmount())
                .build();
        return CartItemDto.builder()
                .id(cartItem.getId())
                .cart(cartDto)
                .product(productDto)
                .quantity(cartItem.getQuantity())
                .build();
    }
}
