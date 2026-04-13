package com.shortlink.app.service;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.event.LinkClickedEvent;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedirectService {

    private final LinkRepository linkRepository;
    private final LinkRedisCache linkRedisCache;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Resolves the target URL (Redis-first), warms cache on miss, and records clicks asynchronously.
     */
    @Transactional(readOnly = true)
    public String resolveAndRecordClick(String shortSlug) {
        String key = shortSlug.trim().toLowerCase(Locale.ROOT);
        return linkRedisCache
                .getTargetUrl(key)
                .map(
                        url -> {
                            publishClickEvent(key);
                            return url;
                        })
                .orElseGet(() -> loadFromDatabase(key));
    }

    private String loadFromDatabase(String key) {
        Link link =
                linkRepository
                        .findByShortSlug(key)
                        .orElseThrow(
                                () -> new ApiException(HttpStatus.NOT_FOUND, "link_not_found", "Short link not found"));
        linkRedisCache.put(key, link.getOriginalUrl());
        eventPublisher.publishEvent(new LinkClickedEvent(link.getId()));
        return link.getOriginalUrl();
    }

    private void publishClickEvent(String shortSlugKey) {
        linkRepository.findByShortSlug(shortSlugKey).ifPresent(link -> eventPublisher.publishEvent(new LinkClickedEvent(link.getId())));
    }
}
