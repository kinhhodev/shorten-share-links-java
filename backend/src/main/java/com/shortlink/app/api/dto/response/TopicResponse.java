package com.shortlink.app.api.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TopicResponse {
    UUID publicId;
    String name;
    String slug;
    String description;
    Instant createdAt;
}
