package de.danielkoellgen.srscscollabservice.domain.deck.domain;

import de.danielkoellgen.srscscollabservice.domain.core.IllegalEntityPersistenceState;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Deck {

    @NotNull
    private final UUID deckId;

    @Nullable
    private User user;

    @Override
    public String toString() {
        return "Deck{" +
                "deckId=" + deckId +
                ", user=" + user +
                '}';
    }
}
