package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardEvent;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollaborationCardRepository {

    void saveNewCollaborationCard(@NotNull CollaborationCard collaborationCard);

    void saveUpdatedCorrelation(@NotNull CollaborationCard collaborationCard,
            @NotNull Correlation correlation);

    void saveNewCardVersion(@NotNull CollaborationCard collaborationCard,
            @NotNull List<Correlation> newCorrelations);

    Optional<CollaborationCard> findCollaborationCardWithCorrelation_byCorrelationId(
            @NotNull UUID correlationId);

    Optional<CollaborationCard> findCollaborationCardWithCorrelation_byCardIdOnRootCardId(
            @NotNull UUID cardId);

    List<CardEvent> findAllCardEventsByCollaborationCardIdAndRootCardId(
            @NotNull UUID collaborationCardId, @NotNull UUID rootCardId);

    void setLock(UUID collaborationCardId, UUID userId);

    Boolean checkLock(UUID collaborationCardId, UUID userId);

    void releaseLock(UUID collaborationCardId, UUID userId);
}