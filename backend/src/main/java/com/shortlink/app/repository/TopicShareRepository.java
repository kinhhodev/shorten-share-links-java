package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.TopicShare;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicShareRepository extends JpaRepository<TopicShare, UUID> {}
