package com.airmonitor.usermicroservice.security;

import com.airmonitor.usermicroservice.entity.Role;
import com.airmonitor.usermicroservice.entity.User;
import com.airmonitor.usermicroservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String prefix = "Bearer ";

        if (authHeader == null || !authHeader.startsWith(prefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(prefix.length());

        if (!jwtService.isTokenValid(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(jwt);
        if (email == null || email.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                Role role = jwtService.extractRoleEnum(jwt);
                List<GrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority(role.name()));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
