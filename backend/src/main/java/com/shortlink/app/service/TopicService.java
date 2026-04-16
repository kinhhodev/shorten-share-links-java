package com.shortlink.app.service;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicStatus;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.repository.TopicRepository;
import com.shortlink.app.util.PathSegments;
import java.util.List;
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
    private final CurrentUserService currentUserService;
    private final LinkRedisCache linkRedisCache;

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
}
