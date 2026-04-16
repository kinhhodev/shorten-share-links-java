package com.shortlink.app.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortlink.app.api.dto.request.LoginRequest;
import com.shortlink.app.api.dto.request.RegisterRequest;
import com.shortlink.app.api.dto.response.AuthResponse;
import com.shortlink.app.domain.entity.AuthProvider;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.security.JwtAuthenticationFilter;
import com.shortlink.app.security.RateLimitFilter;
import com.shortlink.app.service.AuthService;
import com.shortlink.app.service.CurrentUserService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @Test
    void loginReturnsJson() throws Exception {
        AuthResponse res =
                AuthResponse.builder()
                        .accessToken("jwt-token")
                        .tokenType("Bearer")
                        .expiresInMs(3600_000)
                        .userPublicId(UUID.randomUUID())
                        .email("user@example.com")
                        .displayName("Neo User")
                        .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        LoginRequest body = new LoginRequest();
        body.setEmail("user@example.com");
        body.setPassword("password123");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.displayName").value("Neo User"));
    }

    @Test
    void registerReturnsCreated() throws Exception {
        AuthResponse res =
                AuthResponse.builder()
                        .accessToken("jwt-new")
                        .tokenType("Bearer")
                        .expiresInMs(3600_000)
                        .userPublicId(UUID.randomUUID())
                        .email("new@example.com")
                        .displayName("Neo")
                        .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        RegisterRequest body = new RegisterRequest();
        body.setEmail("new@example.com");
        body.setPassword("password12345");
        body.setDisplayName("Neo");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt-new"))
                .andExpect(jsonPath("$.displayName").value("Neo"));
    }

    @Test
    void loginValidatesBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void meReturnsCurrentUserProfile() throws Exception {
        UUID publicId = UUID.randomUUID();
        User currentUser = User.builder()
                .publicId(publicId)
                .email("me@example.com")
                .displayName("My Name")
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
        when(currentUserService.requireCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/api/v1/auth/me").with(user("me@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userPublicId").value(publicId.toString()))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.displayName").value("My Name"));
    }
}
