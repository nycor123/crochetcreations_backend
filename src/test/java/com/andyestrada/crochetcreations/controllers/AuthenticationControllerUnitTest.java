package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;
import com.andyestrada.crochetcreations.services.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc
public class AuthenticationControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    public void shouldGetJwtTokenOnSuccessfulSignIn() throws Exception {
        //given
        String jwtToken = "jwtTokenString";
        SignInRequestDto request = SignInRequestDto.builder().email("test@email.com").password("12345").build();
        given(authenticationService.signin(request))
                .willReturn(JwtAuthenticationResponseDto.builder().token(jwtToken).build());
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .characterEncoding("utf-8")
                .accept(MediaType.APPLICATION_JSON));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(jwtToken)));

    }

}
