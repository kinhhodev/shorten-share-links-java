package com.shortlink.app.service;

import com.shortlink.app.api.dto.request.LoginRequest;
import com.shortlink.app.api.dto.request.RegisterRequest;
import com.shortlink.app.api.dto.response.AuthResponse;
import com.shortlink.app.config.AppProperties;
import com.shortlink.app.domain.entity.AuthProvider;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.UserRepository;
import com.shortlink.app.security.JwtService;
import com.shortlink.app.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "email_taken", "Email is already registered");
        }
        String display = InputSanitizer.safePlainText(request.getDisplayName());
        User user =
                User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(request.getPassword()))
                        .displayName(display)
                        .authProvider(AuthProvider.LOCAL)
                        .enabled(true)
                        .build();
        user = userRepository.save(user);
        log.info("Registered user {}", user.getPublicId());
        return buildTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid email or password"));
        if (user.getAuthProvider() != AuthProvider.LOCAL || user.getPasswordHash() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Use social login for this account");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "account_disabled", "Account is disabled");
        }
        return buildTokenResponse(user);
    }

    private AuthResponse buildTokenResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        String displayName = user.getDisplayName() != null && !user.getDisplayName().isBlank() ? user.getDisplayName() : user.getEmail();
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(appProperties.getJwt().getExpirationMs())
                .userPublicId(user.getPublicId())
                .email(user.getEmail())
                .displayName(displayName)
                .build();
    }
}
