package com.shortlink.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shortlink.app.api.dto.request.CreateGuestLinkRequest;
import com.shortlink.app.api.dto.response.GuestLinkCreatedResponse;
import com.shortlink.app.config.AppProperties;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.repository.LinkRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GuestLinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkRedisCache linkRedisCache;

    @Mock
    private ShortLinkUrlComposer shortLinkUrlComposer;

    private final AppProperties appProperties = new AppProperties();

    private GuestLinkService guestLinkService;

    @BeforeEach
    void setUp() {
        guestLinkService = new GuestLinkService(linkRepository, linkRedisCache, appProperties, shortLinkUrlComposer);
        when(shortLinkUrlComposer.toAbsoluteUrl(any(), any())).thenReturn("https://go.example/r/t/slug");
    }

    @Test
    void createPersistsGuestLinkAndCaches() {
        CreateGuestLinkRequest req = new CreateGuestLinkRequest();
        req.setSlug("abc");
        req.setOriginalUrl("https://example.com/page");
        req.setTopic(null);

        when(linkRepository.existsByTopicAndSlug("_", "abc")).thenReturn(false);
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenAnswer(
                        inv -> {
                            Link link = inv.getArgument(0);
                            if (link.getPublicId() == null) {
                                link.setPublicId(UUID.randomUUID());
                            }
                            return link;
                        });

        GuestLinkCreatedResponse res = guestLinkService.create(req);

        assertThat(res.getShortUrl()).isEqualTo("https://go.example/r/t/slug");
        verify(linkRedisCache).put(eq("_"), eq("abc"), eq("https://example.com/page"), any());
    }

    @Test
    void createRetriesWithNumericSuffixWhenSlugTaken() {
        CreateGuestLinkRequest req = new CreateGuestLinkRequest();
        req.setSlug("name");
        req.setOriginalUrl("https://example.com");
        req.setTopic("blog");

        when(linkRepository.existsByTopicAndSlug("blog", "name")).thenReturn(true);
        when(linkRepository.existsByTopicAndSlug("blog", "name-1")).thenReturn(false);
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenAnswer(
                        inv -> {
                            Link link = inv.getArgument(0);
                            if (link.getPublicId() == null) {
                                link.setPublicId(UUID.randomUUID());
                            }
                            return link;
                        });

        guestLinkService.create(req);

        verify(linkRedisCache).put(eq("blog"), eq("name-1"), eq("https://example.com"), any());
    }
}
