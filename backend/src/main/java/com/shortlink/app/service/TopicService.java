package com.shortlink.app.service;

import com.shortlink.app.api.dto.response.TopicSummaryResponse;
import com.shortlink.app.api.mapper.LinkMapper;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.api.dto.response.TopicShareResponse;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicShare;
import com.shortlink.app.domain.entity.TopicStatus;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.repository.TopicShareRepository;
import com.shortlink.app.repository.TopicRepository;
import com.shortlink.app.repository.UserRepository;
import com.shortlink.app.util.PathSegments;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private static final int MAX_TOPIC_LENGTH = 100;
    private static final int MAX_SLUG_LENGTH = 64;
    private static final int MAX_ALLOCATION_ATTEMPTS = 10_000;

    private final TopicRepository topicRepository;
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final TopicShareRepository topicShareRepository;
    private final CurrentUserService currentUserService;
    private final LinkRedisCache linkRedisCache;
    private final LinkMapper linkMapper;

    @Transactional
    public void ensureTopicActive(User owner, String topicName) {
        topicRepository.findByOwnerIdAndName(owner.getId(), topicName).ifPresentOrElse(topic -> {
            if (topic.getStatus() != TopicStatus.ACTIVE) {
                topic.setStatus(TopicStatus.ACTIVE);
                topicRepository.save(topic);
            }
        }, () -> topicRepository.save(Topic.builder().owner(owner).name(topicName).status(TopicStatus.ACTIVE).build()));
    }

    @Transactional
    public void softDeleteMineByName(String topicRaw) {
        User user = currentUserService.requireCurrentUser();
        final String topicName;
        try {
            topicName = PathSegments.normalizeTopic(topicRaw);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_topic", e.getMessage());
        }

        Topic topic = topicRepository
                .findByOwnerIdAndName(user.getId(), topicName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));

        if (topic.getStatus() == TopicStatus.DELETED) {
            return;
        }

        List<Link> activeLinks = linkRepository.findByCreatedByIdAndTopicAndStatus(user.getId(), topicName, LinkStatus.ACTIVE);
        for (Link link : activeLinks) {
            linkRedisCache.evict(link.getTopic(), link.getSlug());
        }
        linkRepository.updateStatusByOwnerIdAndTopic(user.getId(), topicName, LinkStatus.ACTIVE, LinkStatus.DELETED);
        topic.setStatus(TopicStatus.DELETED);
        topicRepository.save(topic);
        log.info("Soft deleted topic={} ownerPublicId={} links={}", topicName, user.getPublicId(), activeLinks.size());
    }

    @Transactional(readOnly = true)
    public java.util.List<TopicSummaryResponse> listMineSummariesByStatus(TopicStatus status) {
        User user = currentUserService.requireCurrentUser();
        return topicRepository.findByOwnerIdAndStatusOrderByNameAsc(user.getId(), status).stream()
                .map(t -> TopicSummaryResponse.builder().name(t.getName()).status(t.getStatus()).build())
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<LinkResponse> listMineTopicLinksByStatus(String topicRaw, LinkStatus status) {
        User user = currentUserService.requireCurrentUser();
        final String topicName;
        try {
            topicName = PathSegments.normalizeTopic(topicRaw);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_topic", e.getMessage());
        }

        // List by link ownership only; a `topics` row is not required (soft-deleted / legacy data).
        return linkRepository
                .findByCreatedByIdAndTopicIgnoreCaseAndStatusOrderByCreatedAtDesc(user.getId(), topicName, status)
                .stream()
                .filter(link -> !link.isGuest())
                .map(linkMapper::toResponse)
                .toList();
    }

    @Transactional
    public void restoreMineByName(String topicRaw) {
        User user = currentUserService.requireCurrentUser();
        final String topicName;
        try {
            topicName = PathSegments.normalizeTopic(topicRaw);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_topic", e.getMessage());
        }

        Topic topic = topicRepository
                .findByOwnerIdAndName(user.getId(), topicName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));

        if (topic.getStatus() != TopicStatus.DELETED) {
            return;
        }

        // Check for slug conflicts before flipping status back to ACTIVE
        java.util.List<Link> deletedLinks =
                linkRepository.findByCreatedByIdAndTopicAndStatus(user.getId(), topicName, LinkStatus.DELETED);
        for (Link deleted : deletedLinks) {
            boolean existsActive =
                    linkRepository.existsByTopicAndSlugAndStatus(topicName, deleted.getSlug(), LinkStatus.ACTIVE);
            if (existsActive) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "topic_restore_conflict",
                        "Cannot restore topic because some slugs are already used by active links.");
            }
        }

        // No conflicts: restore links and topic
        for (Link link : deletedLinks) {
            link.setStatus(LinkStatus.ACTIVE);
            linkRedisCache.put(link.getTopic(), link.getSlug(), link.getOriginalUrl(), java.util.Optional.ofNullable(link.getExpireAt()));
        }
        topic.setStatus(TopicStatus.ACTIVE);
        topicRepository.save(topic);
        log.info("Restored topic={} ownerPublicId={} links={}", topicName, user.getPublicId(), deletedLinks.size());
    }

    @Transactional
    public TopicShareResponse shareMineByNameToEmail(String topicRaw, String recipientEmailRaw) {
        User owner = currentUserService.requireCurrentUser();
        final String topicName;
        try {
            topicName = PathSegments.normalizeTopic(topicRaw);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_topic", e.getMessage());
        }
        String recipientEmail = recipientEmailRaw == null ? "" : recipientEmailRaw.trim().toLowerCase(Locale.ROOT);
        if (recipientEmail.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_recipient_email", "Recipient email is required.");
        }

        User recipient = userRepository
                .findByEmailIgnoreCase(recipientEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "recipient_not_found", "Recipient not found"));
        if (recipient.getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "cannot_share_self", "You cannot share a topic with yourself.");
        }

        Topic sourceTopic = topicRepository
                .findByOwnerIdAndName(owner.getId(), topicName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));
        if (sourceTopic.getStatus() != TopicStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "topic_not_active", "Only active topics can be shared.");
        }

        String recipientTopic = allocateRecipientTopicName(recipient.getId(), topicName);
        ensureTopicActive(recipient, recipientTopic);

        List<Link> sourceLinks = linkRepository.findByCreatedByIdAndTopicAndStatus(owner.getId(), topicName, LinkStatus.ACTIVE);
        int copiedCount = 0;
        for (Link source : sourceLinks) {
            if (source.isGuest()) {
                continue;
            }
            Link cloned = cloneActiveLinkForRecipient(source, recipient, recipientTopic);
            linkRedisCache.put(cloned.getTopic(), cloned.getSlug(), cloned.getOriginalUrl(), java.util.Optional.ofNullable(cloned.getExpireAt()));
            copiedCount++;
        }

        topicShareRepository.save(TopicShare.builder()
                .owner(owner)
                .recipient(recipient)
                .sourceTopic(topicName)
                .recipientTopic(recipientTopic)
                .sharedLinksCount(copiedCount)
                .build());

        log.info(
                "Shared topic={} ownerPublicId={} recipientPublicId={} recipientTopic={} links={}",
                topicName,
                owner.getPublicId(),
                recipient.getPublicId(),
                recipientTopic,
                copiedCount);

        return TopicShareResponse.builder()
                .recipientEmail(recipient.getEmail())
                .topic(recipientTopic)
                .sharedLinksCount(copiedCount)
                .build();
    }

    private Link cloneActiveLinkForRecipient(Link source, User recipient, String recipientTopic) {
        int attempt = 0;
        while (attempt < MAX_ALLOCATION_ATTEMPTS) {
            String candidate = candidateSlug(source.getSlug(), attempt);
            if (candidate.length() > MAX_SLUG_LENGTH) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "slug_allocation_exhausted",
                        "Could not allocate a unique slug in recipient topic.");
            }
            if (linkRepository.existsByTopicAndSlugAndStatus(recipientTopic, candidate, LinkStatus.ACTIVE)) {
                attempt++;
                continue;
            }
            try {
                Link clone = Link.builder()
                        .topic(recipientTopic)
                        .slug(candidate)
                        .originalUrl(source.getOriginalUrl())
                        .createdBy(recipient)
                        .isGuest(false)
                        .expireAt(source.getExpireAt())
                        .clickCount(0)
                        .status(LinkStatus.ACTIVE)
                        .build();
                return linkRepository.saveAndFlush(clone);
            } catch (DataIntegrityViolationException ex) {
                if (!isTopicSlugUniqueConstraintViolation(ex)) {
                    throw ex;
                }
                attempt++;
            }
        }
        throw new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "slug_allocation_failed",
                "Could not allocate a unique slug for recipient topic.");
    }

    private String allocateRecipientTopicName(java.util.UUID recipientId, String sourceTopic) {
        if (topicRepository.findByOwnerIdAndName(recipientId, sourceTopic).isEmpty()) {
            return sourceTopic;
        }
        int attempt = 1;
        while (attempt <= MAX_ALLOCATION_ATTEMPTS) {
            String suffix = "-shared-" + attempt;
            int baseLimit = MAX_TOPIC_LENGTH - suffix.length();
            if (baseLimit < 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "topic_name_too_long", "Topic name too long to allocate.");
            }
            String base = sourceTopic.length() > baseLimit ? sourceTopic.substring(0, baseLimit) : sourceTopic;
            String candidate = base + suffix;
            if (topicRepository.findByOwnerIdAndName(recipientId, candidate).isEmpty()) {
                return candidate;
            }
            attempt++;
        }
        throw new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "topic_allocation_failed",
                "Could not allocate a unique topic name for recipient.");
    }

    private static String candidateSlug(String normalizedBase, int n) {
        if (n == 0) {
            return normalizedBase;
        }
        return normalizedBase + "-" + n;
    }

    private static boolean isTopicSlugUniqueConstraintViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase(Locale.ROOT);
        return lower.contains("uq_links_topic_slug");
    }
}
