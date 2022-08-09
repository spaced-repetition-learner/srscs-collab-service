package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardEvent(

        @NotNull UUID rootCardId,

        @NotNull LocalDateTime createdAt

) { }
