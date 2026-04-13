package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    Optional<Topic> findByOwnerAndSlug(User owner, String slug);

    Optional<Topic> findByPublicId(UUID publicId);

    @Query(
            """
            SELECT t FROM Topic t
            WHERE t.owner.id = :userId
               OR EXISTS (SELECT 1 FROM TopicShare ts WHERE ts.topic = t AND ts.user.id = :userId)
            ORDER BY t.updatedAt DESC
            """)
    List<Topic> findAllAccessibleByUser(@Param("userId") UUID userId);
}
