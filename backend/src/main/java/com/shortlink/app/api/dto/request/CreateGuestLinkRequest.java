package com.shortlink.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGuestLinkRequest {

    /** Topic/chủ đề path segment; omit or blank for default {@code _}. */
    @Size(max = 100)
    private String topic;

    @NotBlank
    @Size(min = 3, max = 60)
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Slug may contain letters, digits, underscore, and hyphen only")
    private String slug;

    @NotBlank
    @Size(max = 4096)
    private String originalUrl;

    @NotBlank
    private String turnstileToken;
}
