package com.shortlink.app.api.controller;

import com.shortlink.app.service.RedirectService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final RedirectService redirectService;

    @GetMapping("/r/{topic}/{linkSlug}")
    public ResponseEntity<Void> redirectByTopicAndSlug(@PathVariable String topic, @PathVariable String linkSlug) {
        String target = redirectService.resolveAndRecordClick(topic, linkSlug);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, URI.create(target).toString()).build();
    }

    /** Legacy single-segment URLs ({@code /l/{slug}}) resolve as {@code /r/_/{slug}}. */
    @GetMapping("/l/{linkSlug}")
    public ResponseEntity<Void> redirectLegacy(@PathVariable String linkSlug) {
        String target = redirectService.resolveAndRecordClick("_", linkSlug);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, URI.create(target).toString()).build();
    }
}
