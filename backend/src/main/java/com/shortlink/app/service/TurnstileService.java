package com.shortlink.app.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shortlink.app.config.AppProperties;
import com.shortlink.app.exception.ApiException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class TurnstileService {

    private final RestClient.Builder restClientBuilder;
    private final AppProperties appProperties;

    public void verify(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "turnstile_required", "Turnstile verification is required");
        }

        String secretKey = appProperties.getTurnstile().getSecretKey();
        if (!StringUtils.hasText(secretKey)) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "turnstile_not_configured", "Turnstile is not configured");
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("secret", secretKey);
        form.add("response", token);

        TurnstileVerifyResponse response;
        try {
            response = restClientBuilder
                    .build()
                    .post()
                    .uri(appProperties.getTurnstile().getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TurnstileVerifyResponse.class);
        } catch (RestClientException e) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "turnstile_unavailable",
                    "Turnstile verification is temporarily unavailable");
        }

        if (response == null || !response.success()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "turnstile_failed", "Turnstile verification failed");
        }
    }

    record TurnstileVerifyResponse(boolean success, @JsonProperty("error-codes") List<String> errorCodes) {}
}
