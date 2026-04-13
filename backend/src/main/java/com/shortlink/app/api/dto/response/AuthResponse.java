package com.shortlink.app.api.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String accessToken;
    String tokenType;
    long expiresInMs;
    UUID userPublicId;
    String email;
}
