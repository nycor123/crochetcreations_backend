package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.request.SignUpRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;
import com.andyestrada.crochetcreations.services.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponseDto> signup(@RequestBody SignUpRequestDto request) {
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponseDto> signin(@RequestBody SignInRequestDto request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }
}
