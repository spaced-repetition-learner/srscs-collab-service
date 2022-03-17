package de.danielkoellgen.srscscollabservice.domain.user.domain;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class User {

    @Getter
    private final UUID userId;

    @Getter
    private Username username;

    @Getter
    private Boolean isActive;


    public Boolean disableUser() {
        isActive = false;
        return true;
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
