package de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CreateDeckDto(

    @NotNull UUID userId,

    @NotNull String deckName

) {
}
