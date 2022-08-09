package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.tomcat.jni.Local;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private static final Logger logger = LoggerFactory.getLogger(CollaborationCard.class);


    public static @NotNull Pair<CollaborationCard, List<Correlation>> createNew(
            @NotNull Collaboration collaboration,
            @NotNull UUID rootCardId,
            @NotNull UUID cardOwnerUserId,
            @NotNull UUID cardOwnerDeckId
    ) {
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
        logger.trace("Updating Correlation with Card...");
        Correlation updatedCorrelation = correlations.stream()
                .filter(x -> x.correlationId().equals(correlationId))
                .map(x -> x.addCard(correlationId, cardId))
                .findFirst()
                .get();
        logger.debug("Updated Correlation: {}", updatedCorrelation);

        correlations = Stream.concat(
                        correlations.stream()
                                .filter(x -> !x.correlationId().equals(correlationId)),
                        Stream.of(updatedCorrelation))
                .toList();
        logger.trace("Added updated Correlation to list of Correlations.");
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

        logger.debug("CardEvents: {}", cardEvents);
        logger.debug("CurrentEvent: {}", currentCardEvent);
        logger.debug("LatestEvent: {}", latestCardEvent);

        if (currentCardEvent.createdAt().isBefore(latestCardEvent.createdAt())) {
            Correlation newCorrelation = Correlation.makeNewAsOverride(
                    latestCardEvent.rootCardId(),
                    currentCorrelation.userId(),
                    currentCorrelation.deckId(),
                    currentCorrelation.cardId());
            correlations = Stream.of(correlations, List.of(newCorrelation))
                    .flatMap(Collection::stream)
                    .toList();
            return Optional.of(newCorrelation);
        } else {
            return Optional.empty();
        }
    }

//    public @NotNull Triplet<List<Correlation>, List<Correlation>, List<Correlation>> addNewCardVersion(
//            @NotNull Collaboration collaboration, @NotNull UUID parentCardId,
//            @NotNull UUID newCardId, @NotNull UUID cardOwnerUserId, @NotNull UUID cardOwnerDeckId) {
//        logger.trace("Creating new CardVersion...");
//        UUID newRootCardId = newCardId;
//        UUID oldRootCardId = this.correlations.stream()
//                .filter(x -> x.cardId() != null)
//                .filter(x -> x.cardId().equals(parentCardId))
//                .map(Correlation::rootCardId)
//                .findFirst().get();
//        logger.debug("Old root-card-id is {}.", oldRootCardId);
//        logger.debug("New root-card-id is {}.", newRootCardId);
//
//        Correlation cardOwnerCorrelation = Correlation.makeNewWithCard(
//                cardOwnerUserId, cardOwnerDeckId, newRootCardId);
//        logger.debug("Card-Owner Correlation: {}", cardOwnerCorrelation);
//
//        List<Correlation> newOverrideCorrelations = correlations.stream()
//                .filter(x -> x.rootCardId().equals(oldRootCardId))
//                .filter(x -> !x.userId().equals(cardOwnerUserId))
//                .filter(x -> collaboration.getParticipants().get(x.userId()).isActive())
//                .filter(x -> x.cardId() != null)
//                .map(x -> Correlation
//                        .makeNewAsOverride(newRootCardId, x.userId(), x.deckId(), x.cardId()))
//                .toList();
//        logger.debug("Created {} new Override-Correlations.", newOverrideCorrelations.size());
//        logger.debug("{}", newOverrideCorrelations);
//
//        List<Correlation> newCloneCorrelations = collaboration.getParticipants().values().stream()
//                .filter(x -> x.isActive() && x.getDeck() != null)
//                .filter(x -> !x.getUserId().equals(cardOwnerUserId))
//                .filter(x -> newOverrideCorrelations.stream()
//                        .noneMatch(y -> x.getUserId().equals(y.userId())))
//                .map(x -> Correlation.makeNew(newRootCardId, x.getUserId(), x.getDeck().getDeckId()))
//                .toList();
//
//        logger.debug("Created {} new Clone-Correlations.", newCloneCorrelations.size());
//        logger.debug("{}", newCloneCorrelations);
//
//        this.correlations = Stream.of(correlations, List.of(cardOwnerCorrelation),
//                        newOverrideCorrelations, newCloneCorrelations)
//                .flatMap(Collection::stream)
//                .toList();
//
//        return Triplet.with(newOverrideCorrelations, newCloneCorrelations,
//                Stream.of(List.of(cardOwnerCorrelation), newOverrideCorrelations, newCloneCorrelations)
//                        .flatMap(Collection::stream)
//                        .toList());
//    }

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