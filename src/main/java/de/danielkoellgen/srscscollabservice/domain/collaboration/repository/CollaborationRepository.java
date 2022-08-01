package de.danielkoellgen.srscscollabservice.domain.collaboration.repository;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollaborationRepository {

    void saveNewCollaboration(@NotNull Collaboration collaboration);

    void saveNewParticipant(@NotNull Collaboration collaboration, @NotNull Participant newParticipant);

    void updateAcceptedParticipant(@NotNull Collaboration collaboration, @NotNull Participant participant);

    void updateTerminatedParticipant(@NotNull Collaboration collaboration, @NotNull Participant participant);

    void updateDeckAddedParticipant(@NotNull Collaboration collaboration, @NotNull Participant participant);

    @NotNull Optional<Collaboration> findCollaborationById(@NotNull UUID collaborationId);

    @NotNull Optional<UUID> findCollaborationIdByDeckCorrelationId(@NotNull UUID deckCorrelationId);

    @NotNull Optional<Collaboration> findCollaborationByDeckCorrelationId(@NotNull UUID deckCorrelationId);

    @NotNull List<Collaboration> findCollaborationsByUserId(@NotNull UUID userId);

    @NotNull Optional<Collaboration> findCollaborationByDeckId(@NotNull UUID deckId);
}
