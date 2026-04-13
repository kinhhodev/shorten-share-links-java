package com.shortlink.app.api.mapper;

import com.shortlink.app.api.dto.response.TopicResponse;
import com.shortlink.app.domain.entity.Topic;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    public TopicResponse toResponse(Topic topic) {
        return TopicResponse.builder()
                .publicId(topic.getPublicId())
                .name(topic.getName())
                .slug(topic.getSlug())
                .description(topic.getDescription())
                .createdAt(topic.getCreatedAt())
                .build();
    }
}
