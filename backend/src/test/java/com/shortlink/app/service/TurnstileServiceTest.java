package com.shortlink.app.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.shortlink.app.config.AppProperties;
import com.shortlink.app.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TurnstileServiceTest {

    private static final String VERIFY_URL = "https://turnstile.test/siteverify";

    private AppProperties appProperties;
    private MockRestServiceServer server;
    private TurnstileService turnstileService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        appProperties = new AppProperties();
        appProperties.getTurnstile().setSecretKey("secret");
        appProperties.getTurnstile().setVerifyUrl(VERIFY_URL);
        turnstileService = new TurnstileService(builder, appProperties);
    }

    @Test
    void verifyPostsTokenToCloudflare() {
        server.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("secret=secret")))
                .andExpect(content().string(containsString("response=token")))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        turnstileService.verify("token");

        server.verify();
    }

    @Test
    void verifyRejectsFailedChallenge() {
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("{\"success\":false}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> turnstileService.verify("bad-token"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Turnstile verification failed");
    }
}
