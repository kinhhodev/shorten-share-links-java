package com.shortlink.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.event.LinkClickedEvent;
import com.shortlink.app.exception.ApiException;
import com.shortlink.app.repository.LinkRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RedirectServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkRedisCache linkRedisCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RedirectService redirectService;

    @Test
    void returnsUrlFromCacheAndPublishesClickWhenLinkPresent() {
        UUID linkId = UUID.randomUUID();
        Link link =
                Link.builder()
                        .id(linkId)
                        .publicId(UUID.randomUUID())
                        .topic("t")
                        .slug("s")
                        .originalUrl("https://cached.example")
                        .isGuest(false)
                        .expireAt(null)
                        .clickCount(0)
                        .build();

        when(linkRedisCache.getTargetUrl("t", "s")).thenReturn(Optional.of("https://cached.example"));
        when(linkRepository.findByTopicAndSlugAndStatus("t", "s", LinkStatus.ACTIVE)).thenReturn(Optional.of(link));

        String url = redirectService.resolveAndRecordClick("T", "S");

        assertThat(url).isEqualTo("https://cached.example");
        verify(eventPublisher).publishEvent(new LinkClickedEvent(linkId));
    }

    @Test
    void loadsFromDbOnCacheMissAndWarmsCache() {
        UUID linkId = UUID.randomUUID();
        Link link =
                Link.builder()
                        .id(linkId)
                        .publicId(UUID.randomUUID())
                        .topic("t")
                        .slug("s")
                        .originalUrl("https://db.example")
                        .isGuest(false)
                        .expireAt(null)
                        .clickCount(0)
                        .build();

        when(linkRedisCache.getTargetUrl("t", "s")).thenReturn(Optional.empty());
        when(linkRepository.findByTopicAndSlugAndStatus("t", "s", LinkStatus.ACTIVE)).thenReturn(Optional.of(link));

        String url = redirectService.resolveAndRecordClick("t", "s");

        assertThat(url).isEqualTo("https://db.example");
        verify(linkRedisCache).put(eq("t"), eq("s"), eq("https://db.example"), eq(Optional.empty()));
        ArgumentCaptor<LinkClickedEvent> captor = ArgumentCaptor.forClass(LinkClickedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().linkId()).isEqualTo(linkId);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        when(linkRedisCache.getTargetUrl("x", "y")).thenReturn(Optional.empty());
        when(linkRepository.findByTopicAndSlugAndStatus("x", "y", LinkStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> redirectService.resolveAndRecordClick("x", "y"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", org.springframework.http.HttpStatus.NOT_FOUND);
    }

    @Test
    void throwsGoneWhenExpired() {
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        Link expired =
                Link.builder()
                        .id(UUID.randomUUID())
                        .publicId(UUID.randomUUID())
                        .topic("t")
                        .slug("e")
                        .originalUrl("https://old.example")
                        .isGuest(true)
                        .expireAt(past)
                        .clickCount(0)
                        .build();

        when(linkRedisCache.getTargetUrl("t", "e")).thenReturn(Optional.empty());
        when(linkRepository.findByTopicAndSlugAndStatus("t", "e", LinkStatus.ACTIVE)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> redirectService.resolveAndRecordClick("t", "e"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", org.springframework.http.HttpStatus.GONE);
    }
}
