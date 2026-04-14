package com.shortlink.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.shortlink.app.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShortLinkUrlComposerTest {

    private ShortLinkUrlComposer composer;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setPublicBaseUrl("https://go.example.com/");
        props.getRedirect().setPathPrefix("/r");
        composer = new ShortLinkUrlComposer(props);
    }

    @Test
    void buildsAbsoluteUrl() {
        assertThat(composer.toAbsoluteUrl("mytopic", "my-slug"))
                .isEqualTo("https://go.example.com/r/mytopic/my-slug");
    }

    @Test
    void normalizesCase() {
        assertThat(composer.toAbsoluteUrl("  TOP  ", " AbC ")).isEqualTo("https://go.example.com/r/top/abc");
    }

    @Test
    void addsLeadingSlashWhenPathPrefixMissing() {
        AppProperties props = new AppProperties();
        props.setPublicBaseUrl("http://localhost:8080");
        props.getRedirect().setPathPrefix("r");
        composer = new ShortLinkUrlComposer(props);
        assertThat(composer.toAbsoluteUrl("t", "s")).isEqualTo("http://localhost:8080/r/t/s");
    }
}
