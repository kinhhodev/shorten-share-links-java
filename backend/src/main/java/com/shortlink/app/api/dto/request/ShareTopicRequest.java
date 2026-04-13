package com.shortlink.app.api.dto.request;

import com.shortlink.app.domain.entity.TopicPermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareTopicRequest {

    @NotBlank
    @Email
    private String userEmail;

    @NotNull
    private TopicPermission permission;
}
