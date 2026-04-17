package com.shortlink.app.api.dto.response;

import com.shortlink.app.domain.entity.TopicStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TopicSummaryResponse {
    String name;
    TopicStatus status;
}
