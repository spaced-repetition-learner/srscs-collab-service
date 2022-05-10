package de.danielkoellgen.srscscollabservice.domain.user.repository;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    @NewSpan("find user by username")
    Optional<User> findUserByUsername(Username username);

    @NewSpan("find user by user-id")
    Optional<User> findUserByUserId(UUID userId);

    @NewSpan("save user")
    void save(User user);
}
