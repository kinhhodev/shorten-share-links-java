package com.shortlink.app.schedule;

import com.shortlink.app.service.GuestLinkCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuestLinkCleanupJob {

    private final GuestLinkCleanupService guestLinkCleanupService;

    @Scheduled(cron = "${app.guest-public.cleanup-cron}")
    public void purgeExpiredGuestLinks() {
        guestLinkCleanupService.purgeExpiredGuestLinks();
    }
}
