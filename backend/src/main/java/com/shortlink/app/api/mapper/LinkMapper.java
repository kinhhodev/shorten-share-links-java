package com.shortlink.app.api.mapper;

import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.service.ShortLinkUrlComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkMapper {

    private final ShortLinkUrlComposer shortLinkUrlComposer;

    public LinkResponse toResponse(Link link) {
        String shortUrl = shortLinkUrlComposer.toAbsoluteUrl(link.getTopic(), link.getSlug());
        return LinkResponse.builder()
                .publicId(link.getPublicId())
                .topic(link.getTopic())
                .slug(link.getSlug())
                .shortUrl(shortUrl)
                .originalUrl(link.getOriginalUrl())
                .createdAt(link.getCreatedAt())
                .build();
    }
}
