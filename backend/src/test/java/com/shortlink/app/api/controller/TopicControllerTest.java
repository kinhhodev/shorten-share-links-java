package com.shortlink.app.api.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shortlink.app.api.dto.response.LinkResponse;
import com.shortlink.app.api.dto.response.TopicShareResponse;
import com.shortlink.app.api.dto.response.TopicSummaryResponse;
import com.shortlink.app.domain.entity.TopicStatus;
import java.time.Instant;
import java.util.UUID;
import com.shortlink.app.security.JwtAuthenticationFilter;
import com.shortlink.app.security.RateLimitFilter;
import com.shortlink.app.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TopicController.class)
@AutoConfigureMockMvc(addFilters = false)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser
    void deleteTopicReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/topics/{topicName}", "toeic")).andExpect(status().isNoContent());
        verify(topicService).softDeleteMineByName(eq("toeic"));
    }

    @Test
    @WithMockUser
    void restoreTopicReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/topics/{topicName}/restore", "toeic")).andExpect(status().isNoContent());
        verify(topicService).restoreMineByName(eq("toeic"));
    }

    @Test
    @WithMockUser
    void shareTopicReturnsJson() throws Exception {
        TopicShareResponse response = TopicShareResponse.builder()
                .recipientEmail("friend@example.com")
                .topic("toeic")
                .sharedLinksCount(2)
                .build();
        when(topicService.shareMineByNameToEmail("toeic", "friend@example.com")).thenReturn(response);

        mockMvc.perform(post("/api/v1/topics/{topicName}/share", "toeic")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"friend@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientEmail").value("friend@example.com"))
                .andExpect(jsonPath("$.topic").value("toeic"))
                .andExpect(jsonPath("$.sharedLinksCount").value(2));
    }

    @Test
    @WithMockUser
    void listTopicsByStatusReturnsJsonArray() throws Exception {
        TopicSummaryResponse t =
                TopicSummaryResponse.builder().name("toeic").status(TopicStatus.DELETED).build();
        when(topicService.listMineSummariesByStatus(TopicStatus.DELETED)).thenReturn(java.util.List.of(t));

        mockMvc.perform(get("/api/v1/topics").param("status", "DELETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("toeic"))
                .andExpect(jsonPath("$[0].status").value("DELETED"));
    }

    @Test
    @WithMockUser
    void listTopicLinksByStatusReturnsJsonArray() throws Exception {
        LinkResponse link =
                LinkResponse.builder()
                        .publicId(UUID.randomUUID())
                        .topic("toeic")
                        .slug("idioms")
                        .shortUrl("https://go.example/r/toeic/idioms")
                        .originalUrl("https://example.com")
                        .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                        .build();
        when(topicService.listMineTopicLinksByStatus("toeic", com.shortlink.app.domain.entity.LinkStatus.DELETED))
                .thenReturn(java.util.List.of(link));

        mockMvc.perform(get("/api/v1/topics/{topicName}/links", "toeic").param("status", "DELETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].topic").value("toeic"))
                .andExpect(jsonPath("$[0].slug").value("idioms"));
    }
}
