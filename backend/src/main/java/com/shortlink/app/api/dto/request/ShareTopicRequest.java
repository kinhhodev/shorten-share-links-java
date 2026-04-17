package com.shortlink.app.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareTopicRequest {

    @NotBlank
    @Email
    @Size(max = 320)
    private String recipientEmail;
}
