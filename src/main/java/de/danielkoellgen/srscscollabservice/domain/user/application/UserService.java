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

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public @NotNull User addExternallyCreatedUser(@NotNull UUID userId, @NotNull Username username) {
        logger.trace("Adding externally created User...");
        User user = User.makeNewUser(userId, username);
        logger.debug("{}", user);

        userRepository.save(user);
        logger.trace("User saved.");
        logger.info("Externally created User '{}' added.", user.getUsername().getUsername());
        return user;
    }

    public void disableExternallyDisabledUser(@NotNull UUID userId) throws NoSuchElementException {
        logger.trace("Disabling externally disabled User...");
        logger.trace("Fetching User by id {}.", userId);
        User user = userRepository.findUserByUserId(userId).get();
        logger.debug("{}", user);

        user.disableUser();
        logger.debug("User disabled. isActive={}", user.getIsActive());
        userRepository.save(user);
        logger.trace("User saved.");
        logger.info("User '{}' disabled.", user.getUsername().getUsername());
    }
}
