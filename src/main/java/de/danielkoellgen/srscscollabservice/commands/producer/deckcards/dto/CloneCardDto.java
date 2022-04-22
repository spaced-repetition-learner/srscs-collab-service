package de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CloneCardDto(

    @NotNull UUID referencedCardId,

    @NotNull UUID targetDeckId

) {
}
