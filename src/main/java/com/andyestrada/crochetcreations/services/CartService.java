package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.CartItemDto;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<List<CartItemDto>> getCartItemsForUser(String userEmail);
    Optional<List<CartItemDto>> addCartItemForUser(String userEmail, CartItemDto cartItemDto);
}
