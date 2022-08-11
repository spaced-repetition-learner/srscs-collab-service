package de.danielkoellgen.srscscollabservice.domain.user.domain;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.util.UUID;

@Getter
public class User {

    @NotNull
    private final UUID userId;

    @NotNull
    private final Username username;

    @Nullable
    private Boolean isActive;

    @Transient
    private final static Logger log = LoggerFactory.getLogger(User.class);

    public User(@NotNull UUID userId, @NotNull Username username, @Nullable Boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.isActive = isActive;
    }

    public static @NotNull User makeNewUser(@NotNull UUID userId, @NotNull Username username) {
        return new User(userId, username, true);
    }

    public void disableUser() {
        isActive = false;
        log.debug("User.isActive set to '{}'.", isActive);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username=" + username +
                ", isActive=" + isActive +
                '}';
    }
}
