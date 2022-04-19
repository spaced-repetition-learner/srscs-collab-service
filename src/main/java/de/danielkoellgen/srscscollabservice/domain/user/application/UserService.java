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

    public @NotNull User addExternallyCreatedUser(@NotNull UUID transactionId, @NotNull UUID userId, @NotNull Username username) {
        User user = User.makeNewUser(userId, username);
        userRepository.save(user);

        logger.info("New User '{}' added. [tid={}, userId={}]",
                user.getUsername().getUsername(), transactionId, user.getUserId());
        logger.trace("New User added. [{}]", user);

        return user;
    }

    public void disableExternallyDisabledUser(UUID transactionId, UUID userId) throws NoSuchElementException {
        User user = userRepository.findUserByUserId(userId).get();
        user.disableUser();
        userRepository.save(user);

        logger.info("User '{}' disabled. [tid={}, userId={}]",
                user.getUsername().getUsername(), transactionId, user.getUserId());
    }
}
