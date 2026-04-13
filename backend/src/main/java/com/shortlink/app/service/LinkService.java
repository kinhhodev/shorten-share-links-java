package com.shortlink.app.service;

import com.shortlink.app.api.dto.request.CreateLinkRequest;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.api.mapper.LinkMapper;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.repository.TopicRepository;
import com.shortlink.app.util.UrlValidator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final TopicRepository topicRepository;
    private final TopicAccessService topicAccessService;
    private final LinkMapper linkMapper;
    private final LinkRedisCache linkRedisCache;
    private final CurrentUserService currentUserService;

    @Transactional
    public LinkResponse create(CreateLinkRequest request) {
        User user = currentUserService.requireCurrentUser();
        Topic topic =
                topicRepository
                        .findByPublicId(request.getTopicPublicId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));
        if (!topicAccessService.canCreateLinks(user, topic)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "You cannot add links to this topic");
        }
        String slug = request.getShortSlug().trim();
        String normalizedSlug = slug.toLowerCase(Locale.ROOT);
        if (linkRepository.findByShortSlug(normalizedSlug).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "slug_taken", "Short slug is already in use");
        }
        String safeUrl;
        try {
            safeUrl = UrlValidator.requireHttpUrl(request.getOriginalUrl());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_url", e.getMessage());
        }
        Link link =
                Link.builder()
                        .shortSlug(normalizedSlug)
                        .originalUrl(safeUrl)
                        .topic(topic)
                        .createdBy(user)
                        .clickCount(0)
                        .build();
        link = linkRepository.save(link);
        linkRedisCache.put(normalizedSlug, safeUrl);
        log.info("Created link {} -> topic {}", link.getPublicId(), topic.getPublicId());
        return linkMapper.toResponse(link);
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> listByTopic(UUID topicPublicId) {
        User user = currentUserService.requireCurrentUser();
        Topic topic =
                topicRepository
                        .findByPublicId(topicPublicId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));
        if (!topicAccessService.canViewTopic(user, topic)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "You cannot view this topic");
        }
        return linkRepository.findByTopicIdOrderByCreatedAtDesc(topic.getId()).stream()
                .map(linkMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteByPublicId(UUID linkPublicId) {
        User user = currentUserService.requireCurrentUser();
        Link link =
                linkRepository
                        .findByPublicId(linkPublicId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "link_not_found", "Link not found"));
        Topic topic = link.getTopic();
        if (!topicAccessService.canCreateLinks(user, topic)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "You cannot delete this link");
        }
        linkRedisCache.evict(link.getShortSlug());
        linkRepository.delete(link);
        log.info("Deleted link {}", linkPublicId);
    }
}
