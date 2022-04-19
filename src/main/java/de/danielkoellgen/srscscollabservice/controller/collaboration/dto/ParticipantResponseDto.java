package de.danielkoellgen.srscscollabservice.controller.collaboration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ParticipantResponseDto(

    @NotNull UUID userId,

    @NotNull String participantStatus,

    @Nullable DeckDto deck

) {
    public ParticipantResponseDto(Participant participant) {
        this(participant.getUserId(),
                ParticipantStatus.toStringFromEnum(participant.getCurrentState().status()),
                participant.getDeck() != null ? new DeckDto(participant.getDeck()) : null
        );
    }

    @JsonIgnore
    public @NotNull ParticipantStatus getMappedParticipantStatus() {
        return ParticipantStatus.toEnumFromString(participantStatus);
    }
}
