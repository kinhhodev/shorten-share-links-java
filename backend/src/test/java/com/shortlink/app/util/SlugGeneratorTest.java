package com.shortlink.app.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.RepeatedTest;

class SlugGeneratorTest {

    @RepeatedTest(20)
    void randomSlugHasExpectedLengthAndCharset() {
        String s = SlugGenerator.randomSlug(12);
        assertThat(s).hasSize(12).matches("[a-z0-9]+");
    }
}
