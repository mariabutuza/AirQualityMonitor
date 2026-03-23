package com.airmonitor.usermicroservice.controller;

import com.airmonitor.usermicroservice.dto.*;
import com.airmonitor.usermicroservice.entity.User;
import com.airmonitor.usermicroservice.security.JwtService;
import com.airmonitor.usermicroservice.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import com.airmonitor.usermicroservice.dto.CurrentUserDto;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> me(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtService.extractAllClaims(token);

        String email = claims.getSubject();
        User u = userService.getByEmail(email);

        return ResponseEntity.ok(new CurrentUserDto(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole().name()
        ));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUserAsAdmin(@PathVariable Long id,
                                           @Valid @RequestBody RegisterRequest updateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, updateRequest,true));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/update")
    public ResponseEntity<User> updateMyAccount(HttpServletRequest request,
                                                @Valid @RequestBody RegisterRequest updateRequest) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtService.extractEmail(token);
        User currentUser = userService.getByEmail(email);
        updateRequest.setRole(null);
        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), updateRequest,false));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update-password/{id}")
    public ResponseEntity<String> updatePassword(@PathVariable Long id,
                                                 @RequestBody Map<String, String> payload) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        userService.updatePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String emailDomain,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParts.length > 1 ? sortParts[1] : "asc");
        String sortBy = sortParts[0];

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> users = userService.getUsers(search, role, emailDomain, from, to, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me/email")
    public ResponseEntity<String> getMyEmail(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(userService.getUserById(userId).getEmail());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{userId}/email")
    public ResponseEntity<String> getEmailByUserId(@PathVariable Long userId) {
        String email = userService.getUserById(userId).getEmail();
        return ResponseEntity.ok(email);
    }

}
