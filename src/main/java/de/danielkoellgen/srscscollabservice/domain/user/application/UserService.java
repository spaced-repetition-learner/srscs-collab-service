package de.danielkoellgen.srscscollabservice.domain.user.application;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public @NotNull User addExternallyCreatedUser(@NotNull UUID userId, @NotNull Username username) {
        log.trace("Adding externally created User '{}' with id '{}'...", username, userId);
        User user = User.makeNewUser(userId, username);

        userRepository.save(user);
        log.info("Externally created User '{}' successfully added.", user.getUsername().getUsername());
        log.debug("New User: {}", user);
        return user;
    }

    public void disableExternallyDisabledUser(@NotNull UUID userId) throws NoSuchElementException {
        log.trace("Disabling externally disabled User '{}'...", userId);
        log.trace("Fetching User by id '{}'.", userId);
        User user = userRepository.findUserByUserId(userId).orElseThrow();
        log.debug("Fetched User: {}", user);

        user.disableUser();
        userRepository.save(user);
        log.info("User '{}' successfully disabled.", user.getUsername());
    }
}
