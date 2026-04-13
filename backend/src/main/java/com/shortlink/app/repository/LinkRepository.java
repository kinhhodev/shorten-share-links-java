package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.Link;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinkRepository extends JpaRepository<Link, UUID> {

    Optional<Link> findByShortSlug(String shortSlug);

    Optional<Link> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"topic"})
    List<Link> findByTopicIdOrderByCreatedAtDesc(UUID topicId);

    @Modifying
    @Query("UPDATE Link l SET l.clickCount = l.clickCount + 1 WHERE l.id = :id")
    void incrementClickCount(@Param("id") UUID id);
}
