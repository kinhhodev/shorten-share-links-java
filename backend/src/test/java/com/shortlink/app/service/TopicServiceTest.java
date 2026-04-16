package com.shortlink.app.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shortlink.app.domain.entity.Link;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.Topic;
import com.shortlink.app.domain.entity.TopicStatus;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.repository.LinkRepository;
import com.shortlink.app.repository.TopicRepository;
import java.util.List;
import java.util.UUID;
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
    private CurrentUserService currentUserService;

    @Mock
    private LinkRedisCache linkRedisCache;

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
}
