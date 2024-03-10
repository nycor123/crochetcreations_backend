package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.entities.CartItem;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.CartService;
import com.andyestrada.crochetcreations.services.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final CartService cartService;

    @GetMapping("/cart")
    public ResponseEntity<List<CartItem>> getUserCart(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String username = jwtService.extractUsername(authHeader.substring(7));
            Optional<List<CartItem>> cartItemsOptional = cartService.getCartItemsForUser(username);
            return cartItemsOptional.map(ResponseEntity::ok).orElse(ResponseEntity.ok(new ArrayList<>()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
