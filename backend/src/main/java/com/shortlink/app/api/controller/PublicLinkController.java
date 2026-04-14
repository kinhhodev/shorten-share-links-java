package com.shortlink.app.api.controller;

import com.shortlink.app.api.dto.request.CreateGuestLinkRequest;
import com.shortlink.app.api.dto.response.GuestLinkCreatedResponse;
import com.shortlink.app.service.GuestLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/links")
@RequiredArgsConstructor
public class PublicLinkController {

    private final GuestLinkService guestLinkService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestLinkCreatedResponse create(@Valid @RequestBody CreateGuestLinkRequest request) {
        return guestLinkService.create(request);
    }
}
