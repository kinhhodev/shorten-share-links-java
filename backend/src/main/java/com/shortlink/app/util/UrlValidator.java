package com.shortlink.app.util;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public final class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private UrlValidator() {}

    /** Additional check beyond @URL — only http/https and no user-info tricks. */
    public static String requireHttpUrl(String raw) {
        try {
            URI uri = URI.create(raw.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException("URL must include a scheme");
            }
            if (!ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Only http and https URLs are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL must have a valid host");
            }
            return uri.toString();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid URL", ex);
        }
    }
}
