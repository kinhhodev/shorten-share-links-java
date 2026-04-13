package com.shortlink.app.api.mapper;

import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.config.AppProperties;
import com.shortlink.app.domain.entity.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkMapper {

    private final AppProperties appProperties;

    public LinkResponse toResponse(Link link) {
        String base = appProperties.getPublicBaseUrl().replaceAll("/$", "");
        String prefix = appProperties.getRedirect().getPathPrefix().startsWith("/")
                ? appProperties.getRedirect().getPathPrefix()
                : "/" + appProperties.getRedirect().getPathPrefix();
        String shortUrl = base + prefix + "/" + link.getShortSlug();
        return LinkResponse.builder()
                .publicId(link.getPublicId())
                .shortSlug(link.getShortSlug())
                .shortUrl(shortUrl)
                .originalUrl(link.getOriginalUrl())
                .topicPublicId(link.getTopic().getPublicId())
                .topicSlug(link.getTopic().getSlug())
                .createdAt(link.getCreatedAt())
                .build();
    }
}
