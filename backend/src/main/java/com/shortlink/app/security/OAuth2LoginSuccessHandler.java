package com.shortlink.app.security;

import com.shortlink.app.config.AppProperties;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.service.OAuth2UserSyncService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Profile("oauth2")
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserSyncService oauth2UserSyncService;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        User user = oauth2UserSyncService.syncFromGoogle(oauth2User);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        String base = appProperties.getOauth2().getPostLoginRedirectUri();
        String sep = base.contains("?") ? "&" : "?";
        String redirect = base + sep + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }
}
