package com.shortlink.app.service;

import com.shortlink.app.domain.entity.AuthProvider;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.repository.UserRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public User syncOAuth2User(OAuth2User oauth2User, String registrationId) {
        return switch (registrationId) {
            case "google" -> syncFromGoogle(oauth2User);
            case "github" -> syncFromGitHub(oauth2User);
            default -> throw new IllegalStateException("Unsupported OAuth2 registration: " + registrationId);
        };
    }

    private User syncFromGoogle(OAuth2User oauth2User) {
        Map<String, Object> attrs = oauth2User.getAttributes();
        String subject = Optional.ofNullable((String) attrs.get("sub")).filter(s -> !s.isBlank()).orElse(oauth2User.getName());
        String email = (String) attrs.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("OAuth2 provider did not return an email");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        String name = attrs.get("name") instanceof String s ? s : null;
        String fallbackDisplayName = (name != null && !name.isBlank()) ? name.trim() : email;

        return upsertOAuthUser(AuthProvider.GOOGLE, subject, email, fallbackDisplayName, "Google");
    }

    private User syncFromGitHub(OAuth2User oauth2User) {
        Map<String, Object> attrs = oauth2User.getAttributes();
        Object idObj = attrs.get("id");
        String subject = idObj == null ? oauth2User.getName() : String.valueOf(idObj);

        String email = attrs.get("email") instanceof String s ? s : null;
        String login = attrs.get("login") instanceof String s ? s : null;
        if (email == null || email.isBlank()) {
            if (login == null || login.isBlank()) {
                throw new IllegalStateException("GitHub OAuth did not return email or login");
            }
            email = login.toLowerCase(Locale.ROOT) + "@users.noreply.github.com";
        } else {
            email = email.trim().toLowerCase(Locale.ROOT);
        }

        String name = attrs.get("name") instanceof String s ? s : null;
        String fallbackDisplayName;
        if (name != null && !name.isBlank()) {
            fallbackDisplayName = name.trim();
        } else if (login != null && !login.isBlank()) {
            fallbackDisplayName = login;
        } else {
            fallbackDisplayName = email;
        }

        return upsertOAuthUser(AuthProvider.GITHUB, subject, email, fallbackDisplayName, "GitHub");
    }

    private User upsertOAuthUser(
            AuthProvider provider, String subject, String email, String fallbackDisplayName, String providerLabel) {
        Optional<User> byProvider = userRepository.findByAuthProviderAndProviderSubject(provider, subject);
        if (byProvider.isPresent()) {
            User existing = byProvider.get();
            if (existing.getDisplayName() == null || existing.getDisplayName().isBlank()) {
                existing.setDisplayName(fallbackDisplayName);
            }
            return userRepository.save(existing);
        }

        Optional<User> byEmail = userRepository.findByEmailIgnoreCase(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            existing.setAuthProvider(provider);
            existing.setProviderSubject(subject);
            if (existing.getDisplayName() == null || existing.getDisplayName().isBlank()) {
                existing.setDisplayName(fallbackDisplayName);
            }
            User saved = userRepository.save(existing);
            log.info("Linked {} account to existing user {}", providerLabel, saved.getPublicId());
            return saved;
        }

        User created =
                User.builder()
                        .email(email)
                        .displayName(fallbackDisplayName)
                        .authProvider(provider)
                        .providerSubject(subject)
                        .enabled(true)
                        .build();
        User saved = userRepository.save(created);
        log.info("Created user from {} OAuth {}", providerLabel, saved.getPublicId());
        return saved;
    }
}
