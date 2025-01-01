package com.andyestrada.crochetcreations.startuprunners;

import com.andyestrada.crochetcreations.dto.ProductDto;
import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

@Component
@Profile({"dev", "test"})
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private final UserRepository _userRepository;
    private final PasswordEncoder _passwordEncoder;
    private final ProductService _productService;
    private final String _adminEmail;
    private final String _adminPassword;
    private final String _userEmail;
    private final String _userPassword;

    @Autowired
    public CommandLineAppStartupRunner(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       ProductService productService,
                                       @Value("${admin.email}") String adminEmail,
                                       @Value("${admin.password}") String adminPassword,
                                       @Value("${user.email}") String userEmail,
                                       @Value("${user.password}") String userPassword) {
        _userRepository = userRepository;
        _passwordEncoder = passwordEncoder;
        _productService = productService;
        _adminEmail = adminEmail;
        _adminPassword = adminPassword;
        _userEmail = userEmail;
        _userPassword = userPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email(_adminEmail)
                .password(_passwordEncoder.encode(_adminPassword))
                .role(Role.ADMIN)
                .build();
        _userRepository.save(admin);
        // Create regular user
        User user = User.builder()
                .firstName("Regular")
                .lastName("User")
                .email(_userEmail)
                .password(_passwordEncoder.encode(_userPassword))
                .role(Role.USER)
                .build();
        _userRepository.save(user);
        // Create products
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            String priceString = String.valueOf(random.nextInt(1000, 2501));
            boolean listedForSale = random.nextInt(0, 2) == 1 ? true : false;
            ProductDto productDto = ProductDto.builder()
                    .name("Test Product " + i)
                    .description("Test Product " + i)
                    .price(new BigDecimal(priceString))
                    .listedForSale(listedForSale)
                    .build();
            _productService.saveAll(Arrays.asList(new ProductDto[]{productDto}));
        }
    }
}
