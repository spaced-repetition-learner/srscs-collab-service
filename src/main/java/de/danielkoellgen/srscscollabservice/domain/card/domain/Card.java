package de.danielkoellgen.srscscollabservice.domain.card.domain;

import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor
public class Card {

    @Getter
    @NotNull
    private final UUID cardId;

    @Getter
    @Nullable
    private Deck deck;
}
