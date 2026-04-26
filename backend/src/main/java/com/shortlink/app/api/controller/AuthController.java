package com.shortlink.app.api.controller;

import com.shortlink.app.api.dto.request.LoginRequest;
import com.shortlink.app.api.dto.request.RegisterRequest;
import com.shortlink.app.api.dto.response.AuthResponse;
import com.shortlink.app.api.dto.response.CurrentUserResponse;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.service.AuthService;
import com.shortlink.app.service.CurrentUserService;
import com.shortlink.app.service.TurnstileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final TurnstileService turnstileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        turnstileService.verify(request.getTurnstileToken());
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        turnstileService.verify(request.getTurnstileToken());
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        User user = currentUserService.requireCurrentUser();
        String displayName = user.getDisplayName() != null && !user.getDisplayName().isBlank() ? user.getDisplayName() : user.getEmail();
        return CurrentUserResponse.builder()
                .userPublicId(user.getPublicId())
                .email(user.getEmail())
                .displayName(displayName)
                .build();
    }
}
