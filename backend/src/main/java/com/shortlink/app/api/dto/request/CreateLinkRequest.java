package com.shortlink.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class CreateLinkRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Slug may contain letters, digits, underscore, and hyphen only")
    private String shortSlug;

    @NotBlank
    @URL(regexp = "^(https?://).+", message = "URL must start with http:// or https://")
    @Size(max = 4096)
    private String originalUrl;

    /** Topic to attach this link to (public id from topic list/create). */
    @NotNull
    private UUID topicPublicId;
}
