package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.entities.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<List<CartItem>> getCartItemsForUser(String userEmail);
}
