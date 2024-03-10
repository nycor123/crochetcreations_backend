package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
