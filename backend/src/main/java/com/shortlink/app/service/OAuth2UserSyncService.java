package com.shortlink.app.service;

import com.shortlink.app.domain.entity.AuthProvider;
import com.shortlink.app.domain.entity.User;
import com.shortlink.app.repository.UserRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("oauth2")
@RequiredArgsConstructor
public class OAuth2UserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public User syncFromGoogle(OAuth2User oauth2User) {
        Map<String, Object> attrs = oauth2User.getAttributes();
        String subject = Optional.ofNullable((String) attrs.get("sub")).filter(s -> !s.isBlank()).orElse(oauth2User.getName());
        String email = (String) attrs.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("OAuth2 provider did not return an email");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        String name = attrs.get("name") instanceof String s ? s : null;
        String fallbackDisplayName = (name != null && !name.isBlank()) ? name.trim() : email;

        Optional<User> byProvider = userRepository.findByAuthProviderAndProviderSubject(AuthProvider.GOOGLE, subject);
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
            existing.setAuthProvider(AuthProvider.GOOGLE);
            existing.setProviderSubject(subject);
            if (existing.getDisplayName() == null || existing.getDisplayName().isBlank()) {
                existing.setDisplayName(fallbackDisplayName);
            }
            User saved = userRepository.save(existing);
            log.info("Linked Google account to existing user {}", saved.getPublicId());
            return saved;
        }

        User created =
                User.builder()
                        .email(email)
                        .displayName(fallbackDisplayName)
                        .authProvider(AuthProvider.GOOGLE)
                        .providerSubject(subject)
                        .enabled(true)
                        .build();
        User saved = userRepository.save(created);
        log.info("Created user from Google OAuth {}", saved.getPublicId());
        return saved;
    }
}
