package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.CartItemDto;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<List<CartItemDto>> getCartItems(String userEmail);
    Optional<CartItemDto> addCartItem(String userEmail, CartItemDto cartItemDto);
    Optional<CartItemDto> updateCartItem(String userEmail, Long cartItemId, CartItemDto cartItemDto);
    Boolean deleteCartItem(String userEmail, Long cartItemId);
}
