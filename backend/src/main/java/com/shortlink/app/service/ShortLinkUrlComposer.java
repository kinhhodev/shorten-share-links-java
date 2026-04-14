package com.shortlink.app.service;

import com.shortlink.app.config.AppProperties;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds absolute short URLs (same rules as API responses): {@code {base}{prefix}/{topic}/{linkSlug}}.
 */
@Component
@RequiredArgsConstructor
public class ShortLinkUrlComposer {

    private final AppProperties appProperties;

    public String toAbsoluteUrl(String topic, String linkSlug) {
        String base = appProperties.getPublicBaseUrl().replaceAll("/$", "");
        String prefix = appProperties.getRedirect().getPathPrefix();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        String ts = topic.trim().toLowerCase(Locale.ROOT);
        String ls = linkSlug.trim().toLowerCase(Locale.ROOT);
        return base + prefix + "/" + ts + "/" + ls;
    }
}
