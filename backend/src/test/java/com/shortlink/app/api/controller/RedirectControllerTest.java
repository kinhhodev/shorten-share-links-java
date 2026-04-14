package com.shortlink.app.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shortlink.app.security.JwtAuthenticationFilter;
import com.shortlink.app.security.RateLimitFilter;
import com.shortlink.app.service.RedirectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RedirectController.class)
@AutoConfigureMockMvc(addFilters = false)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedirectService redirectService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @Test
    void redirectByTopicAndSlug() throws Exception {
        when(redirectService.resolveAndRecordClick("mytopic", "slug")).thenReturn("https://target.example/path");

        mockMvc.perform(get("/r/mytopic/slug"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://target.example/path"));
    }

    @Test
    void legacySingleSegmentUsesDefaultTopic() throws Exception {
        when(redirectService.resolveAndRecordClick("_", "legacy-slug")).thenReturn("https://legacy.example");

        mockMvc.perform(get("/l/legacy-slug"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://legacy.example"));
    }
}
