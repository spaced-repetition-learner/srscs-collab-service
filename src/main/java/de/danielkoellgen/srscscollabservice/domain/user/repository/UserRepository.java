package de.danielkoellgen.srscscollabservice.domain.user.repository;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findUserByUsername(Username username);

    Optional<User> findUserByUserId(UUID userId);

    void save(User user);
}
