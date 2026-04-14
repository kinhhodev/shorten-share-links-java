package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.Link;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinkRepository extends JpaRepository<Link, UUID> {

    Optional<Link> findByTopicAndSlug(String topic, String slug);

    boolean existsByTopicAndSlug(String topic, String slug);

    Optional<Link> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"createdBy"})
    List<Link> findByCreatedByIdAndIsGuestIsFalseOrderByCreatedAtDesc(UUID createdById);

    @Query("SELECT l FROM Link l WHERE l.isGuest = true AND l.expireAt < :cutoff")
    List<Link> findGuestLinksExpiredBefore(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("UPDATE Link l SET l.clickCount = l.clickCount + 1 WHERE l.id = :id")
    void incrementClickCount(@Param("id") UUID id);
}
