package de.danielkoellgen.srscscollabservice.domain.deck.domain;

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

    @Getter
    @Nullable
    private User user;
}
