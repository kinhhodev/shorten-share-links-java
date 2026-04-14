package com.shortlink.app.service;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.repository.LinkRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestLinkCleanupService {

    private final LinkRepository linkRepository;
    private final LinkRedisCache linkRedisCache;

    @Transactional
    public int purgeExpiredGuestLinks() {
        Instant now = Instant.now();
        List<Link> expired = linkRepository.findGuestLinksExpiredBefore(now);
        for (Link link : expired) {
            linkRedisCache.evict(link.getTopic(), link.getSlug());
            linkRepository.delete(link);
        }
        if (!expired.isEmpty()) {
            log.info("Purged {} expired guest links", expired.size());
        }
        return expired.size();
    }
}
