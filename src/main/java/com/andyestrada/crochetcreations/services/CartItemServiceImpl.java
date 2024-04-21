package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.CartItemDto;
import com.andyestrada.crochetcreations.entities.Cart;
import com.andyestrada.crochetcreations.entities.CartItem;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.repositories.CartItemRepository;
import com.andyestrada.crochetcreations.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private final CartRepository cartRepository;

    @Autowired
    private final CartItemRepository cartItemRepository;

    @Autowired
    private final ProductService productService;

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
