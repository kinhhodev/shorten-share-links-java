package com.shortlink.app.service;

import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicPermission;
import com.shortlink.app.domain.entity.TopicShare;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.repository.TopicShareRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicAccessService {

    private final TopicShareRepository topicShareRepository;

    public boolean isOwner(User user, Topic topic) {
        return topic.getOwner().getId().equals(user.getId());
    }

    public Optional<TopicShare> findShare(User user, Topic topic) {
        return topicShareRepository.findByTopicAndUser(topic, user);
    }

    public boolean canManageTopic(User user, Topic topic) {
        return isOwner(user, topic);
    }

    /** Create links inside a topic: owner or collaborator with EDIT. */
    public boolean canCreateLinks(User user, Topic topic) {
        if (isOwner(user, topic)) {
            return true;
        }
        return findShare(user, topic)
                .map(ts -> ts.getPermission() == TopicPermission.EDIT)
                .orElse(false);
    }

    /** Read topic and its links: owner, or any share (VIEW or EDIT). */
    public boolean canViewTopic(User user, Topic topic) {
        if (isOwner(user, topic)) {
            return true;
        }
        return findShare(user, topic).isPresent();
    }
}
