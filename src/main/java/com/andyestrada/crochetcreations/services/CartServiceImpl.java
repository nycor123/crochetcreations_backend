package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.entities.CartItem;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Optional<List<CartItem>> getCartItemsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Hibernate.initialize(user.getCart().getCartItems());
        List<CartItem> cartItems = user.getCart().getCartItems();
        return cartItems == null ? Optional.empty() : Optional.of(cartItems);
    }
}
