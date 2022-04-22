package de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record OverrideCardDto(

    @NotNull UUID deckId,

    @NotNull UUID overriddenCardId,

    @NotNull UUID referencedCardId

) {
}
