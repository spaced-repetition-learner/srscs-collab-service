package de.danielkoellgen.srscscollabservice.domain.collaboration.repository;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface CollaborationRepository {

    void saveNewCollaboration(@NotNull Collaboration collaboration);

    void saveNewParticipant(@NotNull Collaboration collaboration, @NotNull Participant newParticipant);

    @NotNull Optional<Collaboration> findCollaborationById(@NotNull UUID collaborationId);
}
