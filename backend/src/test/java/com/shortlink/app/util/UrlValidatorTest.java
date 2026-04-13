package com.shortlink.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UrlValidatorTest {

    @Test
    void acceptsHttpHttps() {
        assertThat(UrlValidator.requireHttpUrl("https://example.com/path?q=1")).isEqualTo("https://example.com/path?q=1");
        assertThat(UrlValidator.requireHttpUrl("http://localhost:8080/")).contains("http://localhost:8080");
    }

    @Test
    void rejectsJavascript() {
        assertThatThrownBy(() -> UrlValidator.requireHttpUrl("javascript:alert(1)")).isInstanceOf(IllegalArgumentException.class);
    }
}
