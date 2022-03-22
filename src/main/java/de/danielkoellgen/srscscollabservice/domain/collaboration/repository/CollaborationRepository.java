package de.danielkoellgen.srscscollabservice.domain.collaboration.repository;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import org.jetbrains.annotations.NotNull;

public interface CollaborationRepository {

    void saveNewCollaboration(@NotNull Collaboration collaboration);
}
