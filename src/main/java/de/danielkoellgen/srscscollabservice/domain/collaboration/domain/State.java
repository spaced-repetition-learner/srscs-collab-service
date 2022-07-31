package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record State(
    @NotNull
    ParticipantStatus status,

    @NotNull
    LocalDateTime createdAt
){
}
