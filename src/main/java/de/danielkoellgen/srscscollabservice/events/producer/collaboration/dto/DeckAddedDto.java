package de.danielkoellgen.srscscollabservice.events.producer.collaboration.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DeckAddedDto(

    @NotNull UUID collaborationId,

    @NotNull UUID userId,

    @NotNull UUID deckId

) { }