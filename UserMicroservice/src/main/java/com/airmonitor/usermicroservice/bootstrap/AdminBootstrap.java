package com.airmonitor.usermicroservice.bootstrap;

import com.airmonitor.usermicroservice.entity.Role;
import com.airmonitor.usermicroservice.entity.User;
import com.airmonitor.usermicroservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
                User admin = User.builder()
                        .email("mariabutuza@gmail.com")
                        .password(passwordEncoder.encode("3333"))
                        .fullName("Maria Butuza")
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("Admin bootstrap creat: mariabutuza@gmail.com / 3333");
            } else {
                System.out.println("Admin existent, bootstrap omis");
            }
        };
    }
}