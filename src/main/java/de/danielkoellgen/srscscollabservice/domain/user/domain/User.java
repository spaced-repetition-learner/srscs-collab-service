package de.danielkoellgen.srscscollabservice.domain.user.domain;

import de.danielkoellgen.srscscollabservice.domain.core.IllegalEntityPersistenceState;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor
public class User {

    @Getter
    @NotNull
    private final UUID userId;

    @Nullable
    private Username username;

    @Nullable
    private Boolean isActive;


    public Boolean disableUser() {
        isActive = false;
        return true;
    }

    public @NotNull Username getUsername() {
        if (username == null) {
            throw new IllegalEntityPersistenceState("[username] not instantiated while trying to access it.");
        }
        return username;
    }

    public @NotNull Boolean getIsActive() {
        if (isActive == null) {
            throw new IllegalEntityPersistenceState("[isActive] not instantiated while trying to access it.");
        }
        return isActive;
    }

    @Override
    public String toString() {
        return "User{" +
                "username=" + username +
                ", userId=" + userId +
                ", isActive=" + isActive +
                '}';
    }
}
