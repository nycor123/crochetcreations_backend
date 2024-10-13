package com.andyestrada.crochetcreations.startuprunners;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;

    @Autowired
    public CommandLineAppStartupRunner(UserRepository userRepository, PasswordEncoder passwordEncoder, ProductService productService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@email.com")
                .password(passwordEncoder.encode("12345"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        // Create regular user
        User user = User.builder()
                .firstName("Regular")
                .lastName("User")
                .email("user@email.com")
                .password(passwordEncoder.encode("12345"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        // Create products
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            String priceString = String.valueOf(random.nextInt(1000, 2501));
            ProductDto productDto = ProductDto.builder()
                    .name("Test Product " + i)
                    .description("Test Product " + i)
                    .price(new BigDecimal(priceString))
                    .listedForSale(true)
                    .build();
            productService.saveAll(Arrays.asList(new ProductDto[]{productDto}));
        }
    }
}
