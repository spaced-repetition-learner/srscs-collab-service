package de.danielkoellgen.srscscollabservice.domain.user.application;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addExternallyCreatedUser(@NotNull UUID transactionId, @NotNull UUID userId, @NotNull Username username,
            @NotNull Boolean isActive) {
        User user = new User(userId, username, isActive);
        userRepository.save(user);
    }

    public void changeExternallyChangedUsername(UUID transactionId) {
        //TODO
    }

    public void updateExternallyUpdatedUserToDisabled(UUID transactionId) {
        //TODO
    }
}
