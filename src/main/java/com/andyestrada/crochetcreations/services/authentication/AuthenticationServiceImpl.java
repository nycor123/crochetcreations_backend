package com.andyestrada.crochetcreations.services.authentication;

import com.andyestrada.crochetcreations.dto.request.SignInRequestDto;
import com.andyestrada.crochetcreations.dto.request.SignUpRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;
import com.andyestrada.crochetcreations.entities.Cart;
import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.CartRepository;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CartRepository cartRepository;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     AuthenticationManager authenticationManager,
                                     CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.cartRepository = cartRepository;
    }

    @Override
    public JwtAuthenticationResponseDto signup(SignUpRequestDto request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        createUserCart(user);
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponseDto.builder().token(jwt).build();
    }

    @Override
    public JwtAuthenticationResponseDto signin(SignInRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponseDto.builder().token(jwt).build();
    }

    private void createUserCart(User user) {
        Cart cart = Cart.builder().user(user).build();
        user.setCart(cartRepository.save(cart));
        userRepository.save(user);
    }
}
