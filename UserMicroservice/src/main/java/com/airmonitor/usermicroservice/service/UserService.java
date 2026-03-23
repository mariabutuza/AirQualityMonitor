package com.airmonitor.usermicroservice.service;

import com.airmonitor.usermicroservice.dto.AuthResponse;
import com.airmonitor.usermicroservice.dto.EmailRequest;
import com.airmonitor.usermicroservice.dto.LoginRequest;
import com.airmonitor.usermicroservice.dto.RegisterRequest;
import com.airmonitor.usermicroservice.entity.Role;
import com.airmonitor.usermicroservice.entity.User;
import com.airmonitor.usermicroservice.repository.UserRepository;
import com.airmonitor.usermicroservice.security.JwtService;
import com.airmonitor.usermicroservice.specification.UserSpecifications;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${email.service.token}")
    private String emailServiceToken;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        try {
            sendConfirmationEmail(user.getFullName(), user.getEmail());
        } catch (Exception e) {
            System.err.println("Eroare la trimiterea emailului de confirmare: " + e.getMessage());
        }

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }

    public void sendConfirmationEmail(String name, String email) throws Exception {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setName(name);
        emailRequest.setRecipientEmail(email);
        emailRequest.setSubject("Confirmare cont – Air Quality Monitor");
        emailRequest.setCustomMessage1("Contul tău a fost creat cu succes!");
        emailRequest.setCustomMessage2("Îți mulțumim că te-ai alăturat comunității Air Quality Monitor.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth(emailServiceToken);

        HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(emailRequest, headers);

        String endpoint = emailServiceUrl + "/sendConfirmationEmail";
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Request body: " + new ObjectMapper().writeValueAsString(emailRequest));

        ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, requestEntity, Void.class);
        System.out.println("Email de confirmare trimis cu status: " + response.getStatusCode());
    }

    public User updateUser(Long id, RegisterRequest request, boolean isAdminUpdate) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getEmail() != null
                && !user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (isAdminUpdate && request.getRole() != null && !request.getRole().isBlank()) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role value");
            }
        }

        return userRepository.save(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public Page<User> getUsers(String search, String role, String emailDomain,
                               LocalDateTime from, LocalDateTime to,
                               Pageable pageable) {
        Specification<User> spec = UserSpecifications.withFilters(search, role, emailDomain, from, to);
        return userRepository.findAll(spec, pageable);
    }
}
