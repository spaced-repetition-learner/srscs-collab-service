package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public class CollaborationCard {

    private final @NotNull UUID collaborationCardId;

    private final @Nullable UUID collaborationId;

    private final @Nullable UUID currentCardId;

    private @NotNull List<Correlation> correlations;

    private @NotNull List<CardEvent> cardEvents;

    private static final Logger log = LoggerFactory.getLogger(CollaborationCard.class);


    public static @NotNull Pair<CollaborationCard, List<Correlation>> createNew(
            @NotNull Collaboration collaboration,
            @NotNull UUID rootCardId,
            @NotNull UUID cardOwnerUserId,
            @NotNull UUID cardOwnerDeckId
    ) {
        log.trace("Creating new CollaborationCard...");
        CardEvent newCardEvent = new CardEvent(rootCardId, LocalDateTime.now());
        List<Correlation> correlationsWithOutOwner = collaboration.getParticipants().values()
                .stream()
                .filter(x -> x.isActive() && x.getDeck() != null)
                .filter(x -> !x.getUser().getUserId().equals(cardOwnerUserId))
                .map(x -> Correlation.makeNew(rootCardId, x.getUserId(), x.getDeck().getDeckId()))
                .toList();
        Correlation cardOwnerCorrelation = Correlation.makeNewWithCard(
                cardOwnerUserId, cardOwnerDeckId, rootCardId);
        List<Correlation> mergedCorrelations = Stream.of(correlationsWithOutOwner,
                        List.of(cardOwnerCorrelation))
                .flatMap(Collection::stream)
                .toList();

        log.debug("Root-Card-id is '{}'.", rootCardId);
        log.debug("New CardEvent: {}", newCardEvent);
        log.debug("Owner-Correlation: {}", cardOwnerCorrelation);
        log.debug("Other-Participant-Correlations: {}", correlationsWithOutOwner);

        return Pair.with(
                new CollaborationCard(UUID.randomUUID(),
                        collaboration.getCollaborationId(),
                        rootCardId,
                        mergedCorrelations,
                        List.of(newCardEvent)),
                correlationsWithOutOwner
        );
    }

    public Pair<List<Correlation>, List<Correlation>> addNewCardVersion(
            @NotNull UUID rootCardId,
            @NotNull UUID parentCardId,
            @NotNull List<Participant> availableLocks,
            @NotNull UUID cardOwnerDeckId,
            @NotNull UUID cardOwnerUserId
    ) {
        log.trace("Updating CollaborationCard to newest Version...");
        UUID oldRootCardId = this.correlations.stream()
                .filter(x -> x.cardId() != null)
                .filter(x -> x.cardId().equals(parentCardId))
                .map(Correlation::rootCardId)
                .findFirst()
                .get();
        Correlation newCardOwnerCorrelation = Correlation.makeNewWithCard(
                cardOwnerUserId,
                cardOwnerDeckId,
                rootCardId);
        Function<UUID, Optional<Participant>> findParticipant = (UUID userId) -> availableLocks
                .stream()
                .filter(x -> x.getUserId().equals(userId))
                .findFirst();
        List<Correlation> newOverrideCorrelations = correlations.stream()
                .filter(x -> x.rootCardId().equals(oldRootCardId))
                .filter(x -> !x.userId().equals(cardOwnerUserId))
                .filter(x -> findParticipant.apply(x.userId()).isPresent())
                .filter(x -> x.cardId() != null)
                .map(x -> Correlation
                        .makeNewAsOverride(rootCardId, x.userId(), x.deckId(), x.cardId()))
                .toList();
        CardEvent newCardEvent = new CardEvent(rootCardId, LocalDateTime.now());

        log.debug("OldRootCardId is '{}'.", oldRootCardId);
        log.debug("New card-owner Correlation: {}", newCardOwnerCorrelation);
        log.debug("New Other-Participants Correlations: {}", newOverrideCorrelations);
        log.debug("New CardEvent: {}", newCardEvent);

        cardEvents = Stream.of(cardEvents, List.of(newCardEvent))
                .flatMap(Collection::stream)
                .toList();
        correlations = Stream.of(correlations, List.of(newCardOwnerCorrelation),
                        newOverrideCorrelations)
                .flatMap(Collection::stream)
                .toList();
        return Pair.with(newOverrideCorrelations, Stream.of(
                List.of(newCardOwnerCorrelation), newOverrideCorrelations)
                        .flatMap(Collection::stream)
                        .toList());
    }

    public @NotNull Correlation addCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        log.trace("Adding Card to Correlation...");
        Correlation updatedCorrelation = correlations.stream()
                .filter(x -> x.correlationId().equals(correlationId))
                .map(x -> x.addCard(correlationId, cardId))
                .findFirst()
                .get();
        log.debug("Updated Correlation: {}", updatedCorrelation);

        correlations = Stream.concat(
                        correlations.stream()
                                .filter(x -> !x.correlationId().equals(correlationId)),
                        Stream.of(updatedCorrelation))
                .toList();
        return updatedCorrelation;
    }

    public Optional<Correlation> updateToLatestCardEvent(Correlation currentCorrelation) {
        CardEvent currentCardEvent = cardEvents
                .stream()
                .filter(x -> x.rootCardId().equals(currentCorrelation.rootCardId()))
                .findFirst()
                .orElseThrow();
        CardEvent latestCardEvent = cardEvents
                .stream()
                .max((val0, val1) -> val0.createdAt().compareTo(val1.createdAt()))
                .get();

        log.debug("CardEvents: {}", cardEvents);
        log.debug("CurrentEvent: {}", currentCardEvent);
        log.debug("LatestEvent: {}", latestCardEvent);

        if (currentCardEvent.createdAt().isBefore(latestCardEvent.createdAt())) {
            log.trace("Newer card-event exists.");
            Correlation newCorrelation = Correlation.makeNewAsOverride(
                    latestCardEvent.rootCardId(),
                    currentCorrelation.userId(),
                    currentCorrelation.deckId(),
                    currentCorrelation.cardId());
            correlations = Stream.of(correlations, List.of(newCorrelation))
                    .flatMap(Collection::stream)
                    .toList();
            log.debug("Newest Correlation: {}", newCorrelation);
            return Optional.of(newCorrelation);
        } else {
            log.trace("No newer card-event exists.");
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "CollaborationCard{" +
                "collaborationCardId=" + collaborationCardId +
                ", collaborationId=" + collaborationId +
                ", currentCardId=" + currentCardId +
                ", correlations=" + correlations +
                ", cardEvents=" + cardEvents +
                '}';
    }
}