package de.danielkoellgen.srscscollabservice.domain.deck.domain;

import de.danielkoellgen.srscscollabservice.domain.core.IllegalEntityPersistenceState;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor
public class Deck {

    @Getter
    @NotNull
    private final UUID deckId;

    @Nullable
    private User user;

    public @NotNull User getUser() {
        if (user == null) {
            throw new IllegalEntityPersistenceState("[user] not instantiated while trying to access it.");
        }
        return user;
    }
}
