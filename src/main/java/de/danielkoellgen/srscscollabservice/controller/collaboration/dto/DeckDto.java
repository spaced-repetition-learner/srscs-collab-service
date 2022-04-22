package de.danielkoellgen.srscscollabservice.controller.collaboration.dto;

import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DeckDto(

    @NotNull UUID deckId

) {
    public DeckDto(Deck deck) {
        this(deck.getDeckId());
    }
}
