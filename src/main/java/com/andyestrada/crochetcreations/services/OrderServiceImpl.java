package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.entities.Item;
import com.andyestrada.crochetcreations.entities.Order;
import com.andyestrada.crochetcreations.repositories.ItemRepository;
import com.andyestrada.crochetcreations.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final ItemRepository itemRepository;

    @Override
    public Optional<List<Order>> findAll() {
        return Optional.of(orderRepository.findAll());
    }

    @Override
    public Optional<Order> save(Order order) {
        Order savedOrder = orderRepository.save(order);
        for (Item item : order.getItems()) {
            item.setOrder(order);
            itemRepository.save(item);
        }
        return Optional.of(savedOrder);
    }

}
