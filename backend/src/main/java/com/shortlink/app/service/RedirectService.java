package com.shortlink.app.service;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.event.LinkClickedEvent;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
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
    public String resolveAndRecordClick(String topic, String linkSlug) {
        String ts = topic.trim().toLowerCase(Locale.ROOT);
        String ls = linkSlug.trim().toLowerCase(Locale.ROOT);
        return linkRedisCache
                .getTargetUrl(ts, ls)
                .map(
                        url -> {
                            publishClickEvent(ts, ls);
                            return url;
                        })
                .orElseGet(() -> loadFromDatabase(ts, ls));
    }

    private String loadFromDatabase(String topicKey, String linkSlugKey) {
        Link link =
                linkRepository
                        .findByTopicAndSlug(topicKey, linkSlugKey)
                        .orElseThrow(
                                () -> new ApiException(HttpStatus.NOT_FOUND, "link_not_found", "Short link not found"));
        if (link.getExpireAt() != null && link.getExpireAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.GONE, "link_expired", "This short link has expired");
        }
        linkRedisCache.put(topicKey, linkSlugKey, link.getOriginalUrl(), Optional.ofNullable(link.getExpireAt()));
        eventPublisher.publishEvent(new LinkClickedEvent(link.getId()));
        return link.getOriginalUrl();
    }

    private void publishClickEvent(String topicKey, String linkSlugKey) {
        linkRepository
                .findByTopicAndSlug(topicKey, linkSlugKey)
                .ifPresent(
                        link -> {
                            if (link.getExpireAt() != null && link.getExpireAt().isBefore(Instant.now())) {
                                return;
                            }
                            eventPublisher.publishEvent(new LinkClickedEvent(link.getId()));
                        });
    }
}
