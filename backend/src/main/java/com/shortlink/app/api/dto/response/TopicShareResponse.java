package com.shortlink.app.api.dto.response;

import com.shortlink.app.domain.entity.TopicPermission;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TopicShareResponse {
    UUID shareId;
    String userEmail;
    TopicPermission permission;
}
