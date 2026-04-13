package com.shortlink.app.api.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LinkResponse {
    UUID publicId;
    String shortSlug;
    String shortUrl;
    String originalUrl;
    UUID topicPublicId;
    String topicSlug;
    Instant createdAt;
}
