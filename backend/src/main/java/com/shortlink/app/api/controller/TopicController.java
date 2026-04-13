package com.shortlink.app.api.controller;

import com.shortlink.app.api.dto.request.CreateTopicRequest;
import com.shortlink.app.api.dto.request.ShareTopicRequest;
import com.shortlink.app.api.dto.response.TopicResponse;
import com.shortlink.app.api.dto.response.TopicShareResponse;
import com.shortlink.app.service.TopicService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponse create(@Valid @RequestBody CreateTopicRequest request) {
        return topicService.create(request);
    }

    @GetMapping
    public List<TopicResponse> list() {
        return topicService.listMine();
    }

    @PostMapping("/{topicPublicId}/shares")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicShareResponse share(
            @PathVariable UUID topicPublicId, @Valid @RequestBody ShareTopicRequest request) {
        return topicService.shareTopic(topicPublicId, request);
    }

    @GetMapping("/{topicPublicId}/shares")
    public List<TopicShareResponse> listShares(@PathVariable UUID topicPublicId) {
        return topicService.listShares(topicPublicId);
    }

    @DeleteMapping("/{topicPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID topicPublicId) {
        topicService.deleteByPublicId(topicPublicId);
    }
}
