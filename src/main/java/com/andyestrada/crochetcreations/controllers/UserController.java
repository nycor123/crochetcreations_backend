package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/cart")
    public ResponseEntity<String> getUserCart(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return ResponseEntity.ok(String.format("Viewing user cart of user - %s.", user.getUsername()));
    }
}
