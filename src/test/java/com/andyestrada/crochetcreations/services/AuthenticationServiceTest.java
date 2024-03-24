package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.SignUpRequestDto;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.authentication.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void newUserShouldHaveACart() {
        //given
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .email("test@email.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();
        //when
        authenticationService.signup(signUpRequestDto);
        //then
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElseThrow();
        assertTrue(user.getCart() != null);
    }
}
