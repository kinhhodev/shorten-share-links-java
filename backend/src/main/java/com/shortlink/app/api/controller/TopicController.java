package com.shortlink.app.api.controller;

import com.shortlink.app.api.dto.response.TopicSummaryResponse;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.domain.entity.LinkStatus;
import com.shortlink.app.domain.entity.TopicStatus;
import com.shortlink.app.service.TopicService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @DeleteMapping("/{topicName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable String topicName) {
        topicService.softDeleteMineByName(topicName);
    }

    @PostMapping("/{topicName}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@PathVariable String topicName) {
        topicService.restoreMineByName(topicName);
    }

    @GetMapping
    public List<TopicSummaryResponse> listByStatus(
            @RequestParam(name = "status", defaultValue = "ACTIVE") TopicStatus status) {
        return topicService.listMineSummariesByStatus(status);
    }

    @GetMapping("/{topicName}/links")
    public List<LinkResponse> listTopicLinksByStatus(
            @PathVariable String topicName,
            @RequestParam(name = "status", defaultValue = "DELETED") LinkStatus status) {
        return topicService.listMineTopicLinksByStatus(topicName, status);
    }
}
