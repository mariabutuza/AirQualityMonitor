package com.airmonitor.usermicroservice.controller;

import com.airmonitor.usermicroservice.dto.RegisterDeviceTokenRequest;
import com.airmonitor.usermicroservice.entity.DeviceToken;
import com.airmonitor.usermicroservice.repository.DeviceTokenRepository;
import com.airmonitor.usermicroservice.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/user/push")
public class DeviceTokenController {
    private final DeviceTokenRepository repo;
    private final JwtService jwtService;

    public DeviceTokenController(DeviceTokenRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterDeviceTokenRequest body,
                                         HttpServletRequest request) {
        var auth = request.getHeader("Authorization");
        var tokenJwt = auth.substring(7);
        Long userId = jwtService.extractUserId(tokenJwt);

        repo.findByToken(body.getToken()).ifPresentOrElse(existing -> {
            existing.setUserId(userId);
            existing.setPlatform(body.getPlatform());
            existing.setExpo(body.getExpo() != null ? body.getExpo() : true);
            existing.setUpdatedAt(Instant.now());
            repo.save(existing);
        }, () -> {
            var dt = new DeviceToken();
            dt.setUserId(userId);
            dt.setToken(body.getToken());
            dt.setPlatform(body.getPlatform());
            dt.setExpo(body.getExpo() != null ? body.getExpo() : true);
            dt.setUpdatedAt(Instant.now());
            repo.save(dt);
        });

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/unregister")
    public ResponseEntity<Void> unregister(@RequestBody RegisterDeviceTokenRequest body) {
        repo.deleteByToken(body.getToken());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{userId}/tokens")
    public ResponseEntity<List<DeviceToken>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(repo.findByUserId(userId));
    }
}
