package com.shortlink.app.service;

import com.shortlink.app.api.dto.request.CreateGuestLinkRequest;
import com.shortlink.app.api.dto.response.GuestLinkCreatedResponse;
import com.shortlink.app.config.AppProperties;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.util.PathSegments;
import com.shortlink.app.util.UrlValidator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestLinkService {

    private static final int MAX_SLUG_LENGTH = 64;
    private static final int MAX_SLUG_ALLOCATION_ATTEMPTS = 10_000;

    private final LinkRepository linkRepository;
    private final LinkRedisCache linkRedisCache;
    private final AppProperties appProperties;
    private final ShortLinkUrlComposer shortLinkUrlComposer;

    @Transactional
    public GuestLinkCreatedResponse create(CreateGuestLinkRequest request) {
        String topic;
        try {
            topic = PathSegments.normalizeTopic(request.getTopic());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_topic", e.getMessage());
        }

        String rawUrl = request.getOriginalUrl() == null ? "" : request.getOriginalUrl().trim();
        if (rawUrl.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_url", "URL is required");
        }
        String safeUrl;
        try {
            safeUrl = UrlValidator.requireHttpUrl(rawUrl);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_url", e.getMessage());
        }

        String requestedBase = request.getSlug().trim().toLowerCase(Locale.ROOT);
        Instant expireAt = Instant.now().plus(appProperties.getGuestPublic().getLinkTtlDays(), ChronoUnit.DAYS);

        int n = 0;
        while (n < MAX_SLUG_ALLOCATION_ATTEMPTS) {
            String candidate = candidateSlug(requestedBase, n);
            if (candidate.length() > MAX_SLUG_LENGTH) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "slug_allocation_exhausted",
                        "Could not allocate a unique slug within "
                                + MAX_SLUG_LENGTH
                                + " characters; use a shorter base slug.");
            }
            if (linkRepository.existsByTopicAndSlug(topic, candidate)) {
                log.debug(
                        "Guest slug taken topic={} requestedBase={} candidate={} attempt={}",
                        topic,
                        requestedBase,
                        candidate,
                        n);
                n++;
                continue;
            }
            try {
                Link link =
                        Link.builder()
                                .topic(topic)
                                .slug(candidate)
                                .originalUrl(safeUrl)
                                .createdBy(null)
                                .isGuest(true)
                                .expireAt(expireAt)
                                .clickCount(0)
                                .build();
                link = linkRepository.saveAndFlush(link);
                linkRedisCache.put(topic, candidate, safeUrl, Optional.of(expireAt));
                log.info(
                        "Created guest link publicId={} topic={} slug={} requestedBase={} expires={}",
                        link.getPublicId(),
                        topic,
                        candidate,
                        requestedBase,
                        expireAt);

                return GuestLinkCreatedResponse.builder()
                        .shortUrl(shortLinkUrlComposer.toAbsoluteUrl(topic, candidate))
                        .build();
            } catch (DataIntegrityViolationException ex) {
                if (!isLinkPathUniqueConstraintViolation(ex)) {
                    throw ex;
                }
                log.warn("Guest unique constraint race topic={} candidate={} — retrying", topic, candidate);
                n++;
            }
        }
        throw new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "slug_allocation_failed",
                "Could not allocate a unique slug after " + MAX_SLUG_ALLOCATION_ATTEMPTS + " attempts");
    }

    private static String candidateSlug(String normalizedBase, int num) {
        if (num == 0) {
            return normalizedBase;
        }
        return normalizedBase + "-" + num;
    }

    private static boolean isLinkPathUniqueConstraintViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase(Locale.ROOT);
        return lower.contains("uq_links_topic_slug");
    }
}
