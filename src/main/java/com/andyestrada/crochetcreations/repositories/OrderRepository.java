package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
