package com.shortlink.app.service;

import com.shortlink.app.api.dto.request.CreateTopicRequest;
import com.shortlink.app.api.dto.request.ShareTopicRequest;
import com.shortlink.app.api.dto.response.TopicResponse;
import com.shortlink.app.api.dto.response.TopicShareResponse;
import com.shortlink.app.api.mapper.TopicMapper;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicShare;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.repository.TopicRepository;
import com.shortlink.app.repository.TopicShareRepository;
import com.shortlink.app.repository.UserRepository;
import com.shortlink.app.util.InputSanitizer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final LinkRepository linkRepository;
    private final TopicShareRepository topicShareRepository;
    private final UserRepository userRepository;
    private final TopicAccessService topicAccessService;
    private final TopicMapper topicMapper;
    private final CurrentUserService currentUserService;
    private final LinkRedisCache linkRedisCache;

    @Transactional
    public TopicResponse create(CreateTopicRequest request) {
        User owner = currentUserService.requireCurrentUser();
        String slug = InputSanitizer.normalizeSlugSegment(request.getSlug()).toLowerCase();
        if (topicRepository.findByOwnerAndSlug(owner, slug).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "topic_slug_taken", "You already have a topic with this slug");
        }
        Topic topic =
                Topic.builder()
                        .owner(owner)
                        .name(InputSanitizer.safePlainText(request.getName()))
                        .slug(slug)
                        .description(InputSanitizer.safePlainText(request.getDescription()))
                        .build();
        topic = topicRepository.save(topic);
        log.info("Created topic {} for user {}", topic.getPublicId(), owner.getPublicId());
        return topicMapper.toResponse(topic);
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> listMine() {
        User user = currentUserService.requireCurrentUser();
        return topicRepository.findAllAccessibleByUser(user.getId()).stream().map(topicMapper::toResponse).toList();
    }

    @Transactional
    public TopicShareResponse shareTopic(UUID topicPublicId, ShareTopicRequest request) {
        User owner = currentUserService.requireCurrentUser();
        Topic topic =
                topicRepository
                        .findByPublicId(topicPublicId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));
        if (!topicAccessService.isOwner(owner, topic)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "Only the topic owner can share");
        }
        String email = request.getUserEmail().trim().toLowerCase();
        User target =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user_not_found", "No user with that email"));
        if (target.getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_share", "Cannot share with yourself");
        }
        TopicShare share =
                topicShareRepository
                        .findByTopicAndUser(topic, target)
                        .map(
                                existing -> {
                                    existing.setPermission(request.getPermission());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        TopicShare.builder()
                                                .topic(topic)
                                                .user(target)
                                                .permission(request.getPermission())
                                                .build());
        share = topicShareRepository.save(share);
        log.info("Shared topic {} with {}", topicPublicId, email);
        return TopicShareResponse.builder()
                .shareId(share.getId())
                .userEmail(target.getEmail())
                .permission(share.getPermission())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopicShareResponse> listShares(UUID topicPublicId) {
        User user = currentUserService.requireCurrentUser();
        Topic topic =
                topicRepository
                        .findByPublicId(topicPublicId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));
        if (!topicAccessService.isOwner(user, topic)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "Only the topic owner can list shares");
        }
        return topicShareRepository.findByTopicId(topic.getId()).stream()
                .map(
                        ts ->
                                TopicShareResponse.builder()
                                        .shareId(ts.getId())
                                        .userEmail(ts.getUser().getEmail())
                                        .permission(ts.getPermission())
                                        .build())
                .toList();
    }

    @Transactional
    public void deleteByPublicId(UUID topicPublicId) {
        User user = currentUserService.requireCurrentUser();
        Topic topic =
                topicRepository
                        .findByPublicId(topicPublicId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "topic_not_found", "Topic not found"));
        if (!topicAccessService.isOwner(user, topic)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden", "Only the topic owner can delete the topic");
        }
        for (Link link : linkRepository.findByTopicIdOrderByCreatedAtDesc(topic.getId())) {
            linkRedisCache.evict(link.getShortSlug());
        }
        topicRepository.delete(topic);
        log.info("Deleted topic {}", topicPublicId);
    }
}
