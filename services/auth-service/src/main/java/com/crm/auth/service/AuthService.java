package com.crm.auth.service;

import com.crm.auth.dto.*;
import com.crm.auth.entity.*;
import com.crm.auth.repository.*;
import com.crm.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_SALES_REP";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder().name(DEFAULT_ROLE).build()));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(java.util.Set.of(role))
                .build();

        userRepository.save(user);
        log.info("Registered new user with id: {}", user.getId());

        kafkaTemplate.send("auth.user.registered", Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName()
        ));

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("User logged in with id: {}", user.getId());
        kafkaTemplate.send("auth.user.login", Map.of("email", user.getEmail()));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.isValid(token) || jwtUtil.isAccessToken(token)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (isBlacklisted(token)) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Rotate: blacklist old refresh token
        blacklist(token);

        return buildAuthResponse(user);
    }

    public void logout(String refreshToken) {
        if (jwtUtil.isValid(refreshToken)) {
            blacklist(refreshToken);
            log.info("User logged out successfully");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getEmail(), roles))
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
                .tokenType("Bearer")
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    private void blacklist(String token) {
        var expiry = jwtUtil.extractClaims(token).getExpiration();
        long ttl = expiry.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", Duration.ofMillis(ttl));
        }
    }

    private boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
