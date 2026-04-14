package com.shortlink.app.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortlink.app.api.dto.request.CreateGuestLinkRequest;
import com.shortlink.app.api.dto.response.GuestLinkCreatedResponse;
import com.shortlink.app.security.JwtAuthenticationFilter;
import com.shortlink.app.security.RateLimitFilter;
import com.shortlink.app.service.GuestLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicLinkController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GuestLinkService guestLinkService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @Test
    void postCreatesGuestLink() throws Exception {
        when(guestLinkService.create(any(CreateGuestLinkRequest.class)))
                .thenReturn(GuestLinkCreatedResponse.builder().shortUrl("https://go.example/r/_/abc").build());

        CreateGuestLinkRequest body = new CreateGuestLinkRequest();
        body.setSlug("abc");
        body.setOriginalUrl("https://example.com");

        mockMvc.perform(
                        post("/api/public/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value("https://go.example/r/_/abc"));
    }

    @Test
    void postRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/public/links").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
