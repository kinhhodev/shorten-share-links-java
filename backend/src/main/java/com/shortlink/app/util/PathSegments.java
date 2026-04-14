package com.shortlink.app.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** Normalizes URL path segments for {@code /r/{topic}/{linkSlug}}. */
public final class PathSegments {

    private static final Pattern TOPIC_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,100}$");

    private PathSegments() {}

    /**
     * Topic/chủ đề segment; blank input becomes {@code _} (default).
     */
    public static String normalizeTopic(String raw) {
        if (raw == null || raw.isBlank()) {
            return "_";
        }
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (!TOPIC_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(
                    "Topic must be 1–100 characters: letters, digits, underscore, and hyphen only (or leave blank for \"_\")");
        }
        return s;
    }
}
