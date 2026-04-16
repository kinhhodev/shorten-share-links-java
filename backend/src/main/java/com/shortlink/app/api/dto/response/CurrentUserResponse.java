package com.shortlink.app.api.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CurrentUserResponse {
    UUID userPublicId;
    String email;
    String displayName;
}
