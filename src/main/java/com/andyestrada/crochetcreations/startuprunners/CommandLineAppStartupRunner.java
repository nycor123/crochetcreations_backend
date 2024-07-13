package com.andyestrada.crochetcreations.startuprunners;

import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CommandLineAppStartupRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    }
}
