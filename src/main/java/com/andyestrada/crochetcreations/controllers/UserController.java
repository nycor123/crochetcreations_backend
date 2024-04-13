package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.CartItemDto;
import com.andyestrada.crochetcreations.services.CartService;
import com.andyestrada.crochetcreations.services.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final int authHeaderBeginIndex = 7;

    @GetMapping("/cart")
    public ResponseEntity<List<CartItemDto>> getUserCart(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String username = jwtService.extractUsername(authHeader.substring(authHeaderBeginIndex));
            Optional<List<CartItemDto>> cartItemDtoListOptional = cartService.getCartItems(username);
            return cartItemDtoListOptional.map(ResponseEntity::ok).orElse(ResponseEntity.ok(new ArrayList<>()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/cart/add")
    public ResponseEntity<CartItemDto> addToCart(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                       @RequestBody CartItemDto cartItemDto) {
        try {
            String username = jwtService.extractUsername((authHeader.substring(authHeaderBeginIndex)));
            Optional<CartItemDto> cartItemOptional = cartService.addCartItem(username, cartItemDto);
            return cartItemOptional.map(ResponseEntity::ok).orElseThrow();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PatchMapping("/cart/item/{cartItemId}")
    public ResponseEntity<CartItemDto> updateItemQuantity(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                          @PathVariable Long cartItemId,
                                                          @RequestBody CartItemDto cartItemDto) {
        try {
            String username = jwtService.extractUsername((authHeader.substring(authHeaderBeginIndex)));
            Optional<CartItemDto> cartItemOptional = cartService.updateCartItem(username, cartItemId, cartItemDto);
            return cartItemOptional.map(ResponseEntity::ok).orElseThrow();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/cart/item/{cartItemId}")
    public ResponseEntity<Boolean> deleteCartItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                  @PathVariable Long cartItemId) {
        try {
            String username = jwtService.extractUsername((authHeader.substring(authHeaderBeginIndex)));
            Boolean cartItemDeleted = cartService.deleteCartItem(username, cartItemId);
            return ResponseEntity.ok(cartItemDeleted);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
