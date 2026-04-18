package com.shortlink.app.config;

import com.shortlink.app.security.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Activates OAuth2 login routes when OAuth2 client env vars are configured.
 */
@Configuration
public class OAuth2LoginSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain oauth2LoginSecurityFilterChain(HttpSecurity http, OAuth2LoginSuccessHandler successHandler)
            throws Exception {
        http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2Login(o -> o.successHandler(successHandler).userInfoEndpoint(Customizer.withDefaults()));
        return http.build();
    }
}
