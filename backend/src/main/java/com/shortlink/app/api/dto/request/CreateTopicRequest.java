package com.shortlink.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTopicRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(min = 3, max = 100)
    @Pattern(
            regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
            message = "Slug must be lowercase letters, digits, and single hyphens between segments")
    private String slug;

    @Size(max = 2000)
    private String description;
}
