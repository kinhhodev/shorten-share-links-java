package com.shortlink.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Redirect redirect = new Redirect();
    private final RateLimit rateLimit = new RateLimit();
    private final LinkCache linkCache = new LinkCache();
    private final OAuth2 oauth2 = new OAuth2();
    private final GuestPublic guestPublic = new GuestPublic();
    private final Turnstile turnstile = new Turnstile();

    /** Used to build absolute short URLs in API responses (e.g. https://go.example.com). */
    @Getter
    @Setter
    private String publicBaseUrl = "http://localhost:8080";

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }

    @Getter
    @Setter
    public static class Redirect {
        /** First segment of public redirect path; full URL is {@code {base}{pathPrefix}/{topic}/{linkSlug}}. */
        private String pathPrefix = "/r";
    }

    @Getter
    @Setter
    public static class RateLimit {
        private int requestsPerWindow = 120;
        private int windowSeconds = 60;
    }

    @Getter
    @Setter
    public static class LinkCache {
        private long ttlSeconds = 3600;
    }

    @Getter
    @Setter
    public static class OAuth2 {
        /** Frontend or deep-link URL to receive JWT after social login (query param: token). */
        private String postLoginRedirectUri = "http://localhost:3000/auth/callback";
    }

    /** Public guest link creation ({@code POST /api/public/links}) and cleanup. */
    @Getter
    @Setter
    public static class GuestPublic {
        private int createRequestsPerWindow = 5;
        private int createWindowSeconds = 10;
        private int linkTtlDays = 30;
        /** Spring {@code @Scheduled} cron (default: daily 03:00 UTC). */
        private String cleanupCron = "0 0 3 * * *";
    }

    @Getter
    @Setter
    public static class Turnstile {
        private String secretKey = "";
        private String verifyUrl = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    }
}
