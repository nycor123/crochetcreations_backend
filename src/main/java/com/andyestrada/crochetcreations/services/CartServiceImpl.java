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

import static java.util.Objects.isNull;

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
    public Optional<List<CartItemDto>> getCartItems(String userEmail) {
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
    @Transactional
    public Optional<CartItemDto> addCartItem(String userEmail, CartItemDto newCartItemDto) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Cart userCart = user.getCart();
        Product product = productRepository.findById(newCartItemDto.getProductId()).orElseThrow();
        Hibernate.initialize(userCart.getCartItems());
        List<CartItem> userCartItems = userCart.getCartItems();
        boolean productExistingInCart = userCartItems != null
                && userCartItems.stream().anyMatch(ci -> isNull(ci.getOrder()) && ci.getProduct().getId().equals(product.getId()));
        if (productExistingInCart) {
            CartItem cartItemToUpdate = userCartItems.stream()
                    .filter(ci -> ci.getOrder() == null && ci.getProduct().getId().equals(product.getId()))
                    .toList()
                    .get(0);
            int newCartItemQty = cartItemToUpdate.getQuantity() + newCartItemDto.getQuantity();
            validateStockAvailability(product, newCartItemQty);
            cartItemToUpdate.setQuantity(newCartItemQty);
            cartItemRepository.save(cartItemToUpdate);
        } else {
            validateStockAvailability(product, newCartItemDto.getQuantity());
            CartItem newCartItem = CartItem.builder()
                    .cart(user.getCart())
                    .product(product)
                    .quantity(newCartItemDto.getQuantity())
                    .build();
            CartItem savedCartItem = cartItemRepository.save(newCartItem);
            userCartItems.add(savedCartItem);
            userCart.setCartItems(userCartItems);
            cartRepository.save(userCart);
        }
        // return updated CartItem -> CartItem mapped to CartItemDto
        List<CartItemDto> latestCartItems = getCartItems(userEmail).orElseThrow();
        CartItemDto cartItem = latestCartItems.stream()
                .filter(ci -> ci.getProduct().getId().equals(newCartItemDto.getProductId()))
                .findFirst()
                .orElseThrow();
        return Optional.of(cartItem);
    }

    @Override
    @Transactional
    public Optional<CartItemDto> updateCartItem(String userEmail, Long cartItemId, CartItemDto cartItemDto) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Cart userCart = user.getCart();
        Hibernate.initialize(userCart.getCartItems());
        List<CartItem> cartItems = userCart.getCartItems();
        CartItem cartItemToUpdate = cartItems.stream()
                .filter(ci -> ci.getId().equals(cartItemId)).findFirst().orElseThrow();
        validateStockAvailability(productRepository.findById(cartItemToUpdate.getProduct().getId()).orElseThrow(),
                cartItemDto.getQuantity());
        cartItemToUpdate.setQuantity(cartItemDto.getQuantity());
        CartItem updatedCartItem = cartItemRepository.save(cartItemToUpdate);
        return Optional.of(mapCartItemToCartItemDto(user, updatedCartItem));
    }

    @Override
    @Transactional
    public Boolean deleteCartItem(String userEmail, Long cartItemId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Cart userCart = user.getCart();
        Hibernate.initialize(userCart.getCartItems());
        List<CartItem> cartItems = userCart.getCartItems();
        CartItem cartItemToDelete = cartItems.stream()
                .filter(ci -> ci.getId().equals(cartItemId)).findFirst().orElseThrow();
        cartItemRepository.delete(cartItemToDelete);
        return true;
    }

    private void validateStockAvailability(Product product, int requiredStockCount) {
        if (product.getStock().size() < requiredStockCount) {
            throw new RuntimeException("Not enough stock.");
        }
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
