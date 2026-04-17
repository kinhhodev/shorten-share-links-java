package com.shortlink.app.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.api.dto.response.TopicShareResponse;
import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicShare;
import com.shortlink.app.domain.entity.TopicStatus;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.api.mapper.LinkMapper;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.repository.TopicShareRepository;
import com.shortlink.app.repository.TopicRepository;
import com.shortlink.app.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicShareRepository topicShareRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private LinkRedisCache linkRedisCache;

    @Mock
    private LinkMapper linkMapper;

    @InjectMocks
    private TopicService topicService;

    @Test
    void softDeleteTopicMarksLinksDeletedAndEvictsCache() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).publicId(UUID.randomUUID()).email("u@example.com").enabled(true).build();
        Topic topic = Topic.builder().owner(user).name("toeic").status(TopicStatus.ACTIVE).build();
        Link link = Link.builder().topic("toeic").slug("idioms").status(LinkStatus.ACTIVE).build();

        when(currentUserService.requireCurrentUser()).thenReturn(user);
        when(topicRepository.findByOwnerIdAndName(userId, "toeic")).thenReturn(java.util.Optional.of(topic));
        when(linkRepository.findByCreatedByIdAndTopicAndStatus(userId, "toeic", LinkStatus.ACTIVE)).thenReturn(List.of(link));

        topicService.softDeleteMineByName("toeic");

        verify(linkRedisCache).evict(eq("toeic"), eq("idioms"));
        verify(linkRepository).updateStatusByOwnerIdAndTopic(userId, "toeic", LinkStatus.ACTIVE, LinkStatus.DELETED);
        verify(topicRepository).save(topic);
    }

    @Test
    void restoreTopicWithoutConflictsRestoresLinks() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).publicId(UUID.randomUUID()).email("u@example.com").enabled(true).build();
        Topic topic = Topic.builder().owner(user).name("toeic").status(TopicStatus.DELETED).build();
        Link link = Link.builder()
                .topic("toeic")
                .slug("idioms")
                .status(LinkStatus.DELETED)
                .originalUrl("https://example.com")
                .build();

        when(currentUserService.requireCurrentUser()).thenReturn(user);
        when(topicRepository.findByOwnerIdAndName(userId, "toeic")).thenReturn(java.util.Optional.of(topic));
        when(linkRepository.findByCreatedByIdAndTopicAndStatus(userId, "toeic", LinkStatus.DELETED))
                .thenReturn(java.util.List.of(link));
        when(linkRepository.existsByTopicAndSlugAndStatus("toeic", "idioms", LinkStatus.ACTIVE)).thenReturn(false);

        topicService.restoreMineByName("toeic");

        verify(linkRedisCache).put(eq("toeic"), eq("idioms"), eq("https://example.com"), any());
        verify(topicRepository).save(topic);
    }

    @Test
    void listMineTopicLinksByStatusDoesNotRequireTopicRow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).publicId(UUID.randomUUID()).email("u@example.com").enabled(true).build();
        Link link = Link.builder()
                .topic("toeic")
                .slug("idioms")
                .status(LinkStatus.DELETED)
                .originalUrl("https://example.com")
                .isGuest(false)
                .publicId(UUID.randomUUID())
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
        LinkResponse mapped = LinkResponse.builder()
                .publicId(link.getPublicId())
                .topic("toeic")
                .slug("idioms")
                .shortUrl("https://go.example/r/toeic/idioms")
                .originalUrl("https://example.com")
                .createdAt(link.getCreatedAt())
                .build();

        when(currentUserService.requireCurrentUser()).thenReturn(user);
        when(linkRepository.findByCreatedByIdAndTopicIgnoreCaseAndStatusOrderByCreatedAtDesc(
                        userId, "toeic", LinkStatus.DELETED))
                .thenReturn(List.of(link));
        when(linkMapper.toResponse(link)).thenReturn(mapped);

        Assertions.assertThat(topicService.listMineTopicLinksByStatus("toeic", LinkStatus.DELETED))
                .containsExactly(mapped);
        verify(topicRepository, never()).findByOwnerIdAndName(any(), any());
    }

    @Test
    void shareTopicClonesLinksForRecipientAndStoresShareHistory() {
        UUID ownerId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        User owner =
                User.builder().id(ownerId).publicId(UUID.randomUUID()).email("owner@example.com").enabled(true).build();
        User recipient =
                User.builder().id(recipientId).publicId(UUID.randomUUID()).email("friend@example.com").enabled(true).build();
        Topic topic = Topic.builder().owner(owner).name("toeic").status(TopicStatus.ACTIVE).build();
        Link source = Link.builder()
                .topic("toeic")
                .slug("idioms")
                .status(LinkStatus.ACTIVE)
                .originalUrl("https://example.com/idioms")
                .isGuest(false)
                .build();
        Link cloned = Link.builder()
                .topic("toeic")
                .slug("idioms")
                .status(LinkStatus.ACTIVE)
                .originalUrl("https://example.com/idioms")
                .createdBy(recipient)
                .build();

        when(currentUserService.requireCurrentUser()).thenReturn(owner);
        when(userRepository.findByEmailIgnoreCase("friend@example.com")).thenReturn(java.util.Optional.of(recipient));
        when(topicRepository.findByOwnerIdAndName(ownerId, "toeic")).thenReturn(java.util.Optional.of(topic));
        when(topicRepository.findByOwnerIdAndName(recipientId, "toeic")).thenReturn(java.util.Optional.empty());
        when(linkRepository.findByCreatedByIdAndTopicAndStatus(ownerId, "toeic", LinkStatus.ACTIVE)).thenReturn(List.of(source));
        when(linkRepository.existsByTopicAndSlugAndStatus("toeic", "idioms", LinkStatus.ACTIVE)).thenReturn(false);
        when(linkRepository.saveAndFlush(any(Link.class))).thenReturn(cloned);
        when(topicShareRepository.save(any(TopicShare.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TopicShareResponse result = topicService.shareMineByNameToEmail("toeic", "friend@example.com");

        Assertions.assertThat(result.getRecipientEmail()).isEqualTo("friend@example.com");
        Assertions.assertThat(result.getTopic()).isEqualTo("toeic");
        Assertions.assertThat(result.getSharedLinksCount()).isEqualTo(1);
        verify(linkRedisCache).put(eq("toeic"), eq("idioms"), eq("https://example.com/idioms"), any());
        verify(topicShareRepository).save(any(TopicShare.class));
    }
}
