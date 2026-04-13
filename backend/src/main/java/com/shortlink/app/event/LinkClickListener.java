package com.shortlink.app.event;

import com.shortlink.app.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkClickListener {

    private final LinkRepository linkRepository;

    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void onLinkClicked(LinkClickedEvent event) {
        try {
            linkRepository.incrementClickCount(event.linkId());
        } catch (Exception e) {
            log.warn("Failed to increment click count for {}", event.linkId(), e);
        }
    }
}
