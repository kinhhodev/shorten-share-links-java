package com.shortlink.app.repository;

import com.shortlink.app.domain.entity.AuthProvider;
import com.shortlink.app.domain.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByAuthProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    Optional<User> findByPublicId(UUID publicId);
}
