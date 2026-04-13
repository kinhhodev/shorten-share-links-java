package com.shortlink.app.api.controller;

import com.shortlink.app.api.dto.request.CreateLinkRequest;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.service.LinkService;
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
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse create(@Valid @RequestBody CreateLinkRequest request) {
        return linkService.create(request);
    }

    @GetMapping("/by-topic/{topicPublicId}")
    public List<LinkResponse> listByTopic(@PathVariable UUID topicPublicId) {
        return linkService.listByTopic(topicPublicId);
    }

    @DeleteMapping("/{linkPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID linkPublicId) {
        linkService.deleteByPublicId(linkPublicId);
    }
}
