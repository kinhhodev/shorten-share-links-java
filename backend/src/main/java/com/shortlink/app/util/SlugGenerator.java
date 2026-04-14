package com.shortlink.app.util;

import java.security.SecureRandom;
import java.util.Locale;

public final class SlugGenerator {

    private static final char[] ALPHANUM = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int DEFAULT_LENGTH = 9;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SlugGenerator() {}

    public static String randomSlug() {
        return randomSlug(DEFAULT_LENGTH);
    }

    public static String randomSlug(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM[RANDOM.nextInt(ALPHANUM.length)]);
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }
}
