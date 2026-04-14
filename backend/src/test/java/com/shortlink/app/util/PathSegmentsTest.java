package com.shortlink.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PathSegmentsTest {

    @Test
    void blankOrNullBecomesUnderscore() {
        assertThat(PathSegments.normalizeTopic(null)).isEqualTo("_");
        assertThat(PathSegments.normalizeTopic("")).isEqualTo("_");
        assertThat(PathSegments.normalizeTopic("   ")).isEqualTo("_");
    }

    @Test
    void trimsAndLowercases() {
        assertThat(PathSegments.normalizeTopic("  MyTopic  ")).isEqualTo("mytopic");
    }

    @Test
    void acceptsLettersDigitsUnderscoreHyphen() {
        assertThat(PathSegments.normalizeTopic("a-z_9")).isEqualTo("a-z_9");
    }

    @Test
    void rejectsInvalidCharacters() {
        assertThatThrownBy(() -> PathSegments.normalizeTopic("bad space"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Topic");
    }
}
