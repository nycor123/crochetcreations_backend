package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.google.GoogleSigninRequestDto;
import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.request.SignUpRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;
import com.andyestrada.crochetcreations.services.authentication.AuthenticationService;
import com.andyestrada.crochetcreations.services.google.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final GoogleOAuthService googleOAuthService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, GoogleOAuthService googleOAuthService) {
        this.authenticationService = authenticationService;
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponseDto> signup(@RequestBody SignUpRequestDto request) {
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponseDto> signin(@RequestBody SignInRequestDto request) {
        try {
            JwtAuthenticationResponseDto responseDto = authenticationService.signin(request);
            HttpHeaders responseHeaders = setSigninResponseHeaders(responseDto);
            return ResponseEntity
                    .ok()
                    .headers(responseHeaders)
                    .body(responseDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/signin-google")
    public ResponseEntity<JwtAuthenticationResponseDto> signinWithGoogle(@RequestBody GoogleSigninRequestDto googleSigninDto) {
        try {
            JwtAuthenticationResponseDto responseDto = this.googleOAuthService.signin(googleSigninDto);
            HttpHeaders responseHeaders = setSigninResponseHeaders(responseDto);
            return ResponseEntity
                    .ok()
                    .headers(responseHeaders)
                    .body(responseDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private HttpHeaders setSigninResponseHeaders(JwtAuthenticationResponseDto responseDto) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", responseDto.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 24) // 24 minutes
                .build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return responseHeaders;
    }

    @PostMapping("/signout")
    public ResponseEntity<String> signout() {
        try {
            ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                    .httpOnly(true)
                    .path("/")
                    .secure(false)
                    .build();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok().headers(responseHeaders).body("{ \"success\": \"true\" }");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
