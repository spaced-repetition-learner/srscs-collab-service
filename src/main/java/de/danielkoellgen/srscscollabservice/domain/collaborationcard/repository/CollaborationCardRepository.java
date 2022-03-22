package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardVersion;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollaborationCardRepository {

    @NotNull Optional<CollaborationCard> findCollaborationCardByTransactionId(@NotNull UUID transactionId);

    @NotNull Optional<CollaborationCard> findCollaborationCardByCardId(@NotNull UUID cardId);

    @NotNull List<CollaborationCard> findCollaborationCardsByCollaborationId(@NotNull UUID collaborationId);

    void saveAppendedCard(@NotNull CollaborationCard collaborationCard, @NotNull CardVersion cardVersion,
            @NotNull Correlation correlation);

    void saveNewCollaborationCard(@NotNull CollaborationCard collaborationCard);
}
