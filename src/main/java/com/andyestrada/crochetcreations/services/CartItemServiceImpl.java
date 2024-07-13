package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.CartItemDto;
import com.andyestrada.crochetcreations.entities.Cart;
import com.andyestrada.crochetcreations.entities.CartItem;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.repositories.CartItemRepository;
import com.andyestrada.crochetcreations.repositories.CartRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Autowired
    public CartItemServiceImpl(CartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Optional<List<CartItem>> addCartItem(CartItemDto cartItemDto) {
        Cart cart = cartRepository.findById(cartItemDto.getId()).orElseThrow();
        Product product = productService.findById(cartItemDto.getProductId()).orElseThrow();
        CartItem newCartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(cartItemDto.getQuantity())
                .build();
        CartItem savedCartItem = cartItemRepository.save(newCartItem);
        Hibernate.initialize(cart.getCartItems());
        List<CartItem> cartItems = cart.getCartItems();
        cartItems.add(savedCartItem);
        cart.setCartItems(cartItems);
        Cart updatedCart = cartRepository.save(cart);
        return Optional.of(updatedCart.getCartItems());
    }
}
