package com.airmonitor.usermicroservice.security;

import com.airmonitor.usermicroservice.entity.Role;
import com.airmonitor.usermicroservice.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;


    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret.trim());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object idObj = extractAllClaims(token).get("id");
        if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            return (Long) idObj;
        }
        throw new IllegalArgumentException("Invalid ID format in token.");
    }

    public Role extractRoleEnum(String token) {
        String roleStr = extractAllClaims(token).get("role", String.class);
        return Role.valueOf(roleStr);
    }

}
