package com.shortlink.app.api.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LinkResponse {
    UUID publicId;
    /** Path segment chủ đề trong URL {@code /r/{topic}/{slug}}. */
    String topic;
    String slug;
    String shortUrl;
    String originalUrl;
    Instant createdAt;
}
