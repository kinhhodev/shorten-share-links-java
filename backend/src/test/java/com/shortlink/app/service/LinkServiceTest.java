package com.shortlink.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shortlink.app.api.dto.request.CreateLinkRequest;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.api.mapper.LinkMapper;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.repository.LinkRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkMapper linkMapper;

    @Mock
    private LinkRedisCache linkRedisCache;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TopicService topicService;

    private LinkService linkService;

    @BeforeEach
    void setUp() {
        linkService = new LinkService(linkRepository, linkMapper, linkRedisCache, currentUserService, topicService);
        when(linkMapper.toResponse(any(Link.class)))
                .thenAnswer(
                        inv -> {
                            Link link = inv.getArgument(0);
                            return LinkResponse.builder()
                                    .publicId(link.getPublicId())
                                    .topic(link.getTopic())
                                    .slug(link.getSlug())
                                    .shortUrl("http://localhost:8080/r/" + link.getTopic() + "/" + link.getSlug())
                                    .originalUrl(link.getOriginalUrl())
                                    .createdAt(link.getCreatedAt())
                                    .build();
                        });
    }

    @Test
    void createRetriesWithNumericSuffixWhenSlugTakenInSameTopic() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).publicId(UUID.randomUUID()).email("u@example.com").enabled(true).build();
        when(currentUserService.requireCurrentUser()).thenReturn(user);

        CreateLinkRequest req = new CreateLinkRequest();
        req.setTopic("toeic");
        req.setSlug("thanh-ngu");
        req.setOriginalUrl("https://google.com");

        when(linkRepository.existsByTopicAndSlugAndStatus("toeic", "thanh-ngu", LinkStatus.ACTIVE)).thenReturn(true);
        when(linkRepository.existsByTopicAndSlugAndStatus("toeic", "thanh-ngu-1", LinkStatus.ACTIVE))
                .thenReturn(false);
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenAnswer(
                        inv -> {
                            Link link = inv.getArgument(0);
                            if (link.getPublicId() == null) {
                                link.setPublicId(UUID.randomUUID());
                            }
                            return link;
                        });

        LinkResponse res = linkService.create(req);

        assertThat(res.getTopic()).isEqualTo("toeic");
        assertThat(res.getSlug()).isEqualTo("thanh-ngu-1");
        verify(linkRedisCache).put(eq("toeic"), eq("thanh-ngu-1"), eq("https://google.com"));
    }
}
