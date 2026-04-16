package com.shortlink.app.service;

import com.shortlink.app.api.dto.request.CreateLinkRequest;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.api.mapper.LinkMapper;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.util.PathSegments;
import com.shortlink.app.util.UrlValidator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private static final int MAX_SLUG_LENGTH = 64;
    private static final int MAX_SLUG_ALLOCATION_ATTEMPTS = 10_000;

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final LinkRedisCache linkRedisCache;
    private final CurrentUserService currentUserService;
    private final TopicService topicService;

    @Transactional
    public LinkResponse create(CreateLinkRequest request) {
        User user = currentUserService.requireCurrentUser();
        String topic;
        try {
            topic = PathSegments.normalizeTopic(request.getTopic());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_topic", e.getMessage());
        }
        String requestedBase = request.getSlug().trim().toLowerCase(Locale.ROOT);
        String safeUrl;
        try {
            safeUrl = UrlValidator.requireHttpUrl(request.getOriginalUrl());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_url", e.getMessage());
        }

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
            if (linkRepository.existsByTopicAndSlugAndStatus(topic, candidate, LinkStatus.ACTIVE)) {
                log.debug(
                        "Slug taken in topic topic={} requestedBase={} candidate={} attempt={}",
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
                                .createdBy(user)
                                .isGuest(false)
                                .expireAt(null)
                                .clickCount(0)
                                .status(LinkStatus.ACTIVE)
                                .build();
                topicService.ensureTopicActive(user, topic);
                link = linkRepository.saveAndFlush(link);
                linkRedisCache.put(topic, candidate, safeUrl);
                log.info(
                        "Created link publicId={} topic={} slug={} requestedBase={} userPublicId={}",
                        link.getPublicId(),
                        topic,
                        candidate,
                        requestedBase,
                        user.getPublicId());
                return linkMapper.toResponse(link);
            } catch (DataIntegrityViolationException ex) {
                if (!isLinkPathUniqueConstraintViolation(ex)) {
                    throw ex;
                }
                log.warn(
                        "Unique constraint race topic={} candidate={} userPublicId={} — retrying",
                        topic,
                        candidate,
                        user.getPublicId());
                n++;
            }
        }
        throw new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "slug_allocation_failed",
                "Could not allocate a unique slug after " + MAX_SLUG_ALLOCATION_ATTEMPTS + " attempts");
    }

    private static String candidateSlug(String normalizedBase, int n) {
        if (n == 0) {
            return normalizedBase;
        }
        return normalizedBase + "-" + n;
    }

    private static boolean isLinkPathUniqueConstraintViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase(Locale.ROOT);
        return lower.contains("uq_links_topic_slug");
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> listMine() {
        User user = currentUserService.requireCurrentUser();
        return linkRepository.findByCreatedByIdAndIsGuestIsFalseAndStatusOrderByCreatedAtDesc(user.getId(), LinkStatus.ACTIVE).stream()
                .map(linkMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteByPublicId(UUID linkPublicId) {
        User user = currentUserService.requireCurrentUser();
        Link link =
                linkRepository
                        .findByPublicIdAndStatus(linkPublicId, LinkStatus.ACTIVE)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "link_not_found", "Link not found"));
        if (link.isGuest() || link.getCreatedBy() == null || !link.getCreatedBy().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "You cannot delete this link");
        }
        linkRedisCache.evict(link.getTopic(), link.getSlug());
        link.setStatus(LinkStatus.DELETED);
        linkRepository.save(link);
        log.info("Soft deleted link {}", linkPublicId);
    }
}
