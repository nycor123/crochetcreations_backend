package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.CartItemDto;
import com.andyestrada.crochetcreations.entities.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemService {
    Optional<List<CartItem>> addCartItem(CartItemDto cartItemDto);
}
