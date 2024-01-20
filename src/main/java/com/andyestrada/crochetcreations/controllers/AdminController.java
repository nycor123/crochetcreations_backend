package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/home")
    public ResponseEntity<String> getHome(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return ResponseEntity.ok(String.format("Hi admin - %s!", username));
    }

}
