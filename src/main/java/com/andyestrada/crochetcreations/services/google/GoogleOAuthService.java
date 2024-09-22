package com.andyestrada.crochetcreations.services.google;

import com.andyestrada.crochetcreations.dto.google.GoogleSigninRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;

public interface GoogleOAuthService {
    JwtAuthenticationResponseDto signin(GoogleSigninRequestDto signinRequestDto);
}
