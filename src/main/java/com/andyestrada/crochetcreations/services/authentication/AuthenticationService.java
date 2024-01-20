package com.andyestrada.crochetcreations.services.authentication;

import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.request.SignUpRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;

public interface AuthenticationService {
    JwtAuthenticationResponseDto signup(SignUpRequestDto request);
    JwtAuthenticationResponseDto signin(SignInRequestDto request);
}
