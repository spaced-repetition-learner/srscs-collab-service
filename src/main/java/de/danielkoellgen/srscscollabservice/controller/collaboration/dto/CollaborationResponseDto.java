package de.danielkoellgen.srscscollabservice.controller.collaboration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CollaborationResponseDto(

        @NotNull UUID collaborationId,

        @NotNull String collaborationName,

        @NotNull List<ParticipantResponseDto> participants

) {
    public CollaborationResponseDto(Collaboration collaboration) {
        this(collaboration.getCollaborationId(),
                collaboration.getName().getName(),
                collaboration.getParticipants().values().stream()
                        .map(ParticipantResponseDto::new)
                        .toList()
        );
    }

    @JsonIgnore
    public @NotNull DeckName getMappedCollaborationName() {
        try {
            return new DeckName(collaborationName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DeckName from DTO.");
        }
    }
}
