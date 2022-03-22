package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record State(
    UUID transactionId,

    ParticipantStatus status,

    LocalDateTime createdAt
){

}
