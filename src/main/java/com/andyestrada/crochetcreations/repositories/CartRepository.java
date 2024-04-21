package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
