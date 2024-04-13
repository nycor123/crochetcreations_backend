package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.entities.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Optional<List<Order>> findAll();
    Optional<Order> save(Order order);
}
