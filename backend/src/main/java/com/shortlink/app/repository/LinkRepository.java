package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
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

    Optional<Link> findByTopicAndSlugAndStatus(String topic, String slug, LinkStatus status);

    boolean existsByTopicAndSlugAndStatus(String topic, String slug, LinkStatus status);

    Optional<Link> findByPublicIdAndStatus(UUID publicId, LinkStatus status);

    @EntityGraph(attributePaths = {"createdBy"})
    List<Link> findByCreatedByIdAndIsGuestIsFalseAndStatusOrderByCreatedAtDesc(UUID createdById, LinkStatus status);

    List<Link> findByCreatedByIdAndTopicAndStatus(UUID createdById, String topic, LinkStatus status);

    @Query("SELECT l FROM Link l WHERE l.isGuest = true AND l.status = 'ACTIVE' AND l.expireAt < :cutoff")
    List<Link> findGuestLinksExpiredBefore(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("UPDATE Link l SET l.status = :toStatus WHERE l.createdBy.id = :ownerId AND l.topic = :topic AND l.status = :fromStatus")
    int updateStatusByOwnerIdAndTopic(
            @Param("ownerId") UUID ownerId,
            @Param("topic") String topic,
            @Param("fromStatus") LinkStatus fromStatus,
            @Param("toStatus") LinkStatus toStatus);

    @Modifying
    @Query("UPDATE Link l SET l.clickCount = l.clickCount + 1 WHERE l.id = :id")
    void incrementClickCount(@Param("id") UUID id);
}
