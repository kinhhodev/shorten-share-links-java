package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    Optional<Topic> findByOwnerIdAndName(UUID ownerId, String name);

    java.util.List<Topic> findByOwnerIdAndStatusOrderByNameAsc(UUID ownerId, TopicStatus status);
}
