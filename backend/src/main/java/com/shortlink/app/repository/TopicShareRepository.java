package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicShare;
import com.shortlink.app.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicShareRepository extends JpaRepository<TopicShare, UUID> {

    Optional<TopicShare> findByTopicAndUser(Topic topic, User user);

    List<TopicShare> findByTopicId(UUID topicId);

    boolean existsByTopicAndUser(Topic topic, User user);
}
