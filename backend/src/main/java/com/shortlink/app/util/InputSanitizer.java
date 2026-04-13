package com.shortlink.app.util;

import java.util.regex.Pattern;

public final class InputSanitizer {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    private InputSanitizer() {}

    /** Strip angle brackets and control characters to reduce XSS risk in stored text fields. */
    public static String safePlainText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String noMarkup = input.replace('<', ' ').replace('>', ' ');
        return CONTROL_CHARS.matcher(noMarkup).replaceAll("").trim();
    }

    public static String normalizeSlugSegment(String slug) {
        if (slug == null) {
            return null;
        }
        return slug.trim();
    }
}
