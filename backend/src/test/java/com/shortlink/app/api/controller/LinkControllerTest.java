package com.shortlink.app.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortlink.app.api.dto.request.CreateLinkRequest;
import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.security.JwtAuthenticationFilter;
import com.shortlink.app.security.RateLimitFilter;
import com.shortlink.app.service.LinkService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LinkController.class)
@AutoConfigureMockMvc(addFilters = false)
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LinkService linkService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser
    void listMineReturnsJsonArray() throws Exception {
        when(linkService.listMine()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/links")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void createReturnsCreated() throws Exception {
        LinkResponse created =
                LinkResponse.builder()
                        .publicId(UUID.randomUUID())
                        .topic("_")
                        .slug("mylink")
                        .shortUrl("https://go.example/r/_/mylink")
                        .originalUrl("https://example.com/page")
                        .createdAt(Instant.parse("2024-06-01T12:00:00Z"))
                        .build();
        when(linkService.create(any(CreateLinkRequest.class))).thenReturn(created);

        CreateLinkRequest body = new CreateLinkRequest();
        body.setSlug("mylink");
        body.setOriginalUrl("https://example.com/page");

        mockMvc.perform(
                        post("/api/v1/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("mylink"))
                .andExpect(jsonPath("$.topic").value("_"));
    }

    @Test
    @WithMockUser
    void deleteReturnsNoContent() throws Exception {
        UUID publicId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        mockMvc.perform(delete("/api/v1/links/{id}", publicId)).andExpect(status().isNoContent());

        verify(linkService).deleteByPublicId(eq(publicId));
    }
}
