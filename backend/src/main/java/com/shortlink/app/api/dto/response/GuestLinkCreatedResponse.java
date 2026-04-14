package com.shortlink.app.api.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GuestLinkCreatedResponse {
    String shortUrl;
}
