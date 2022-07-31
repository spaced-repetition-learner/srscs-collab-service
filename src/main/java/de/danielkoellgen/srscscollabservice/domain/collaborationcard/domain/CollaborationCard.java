package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.application.ExternallyCreatedCardService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public class CollaborationCard {

    private final @NotNull UUID collaborationCardId;

    private final @Nullable UUID collaborationId;

    private final @Nullable UUID currentCardId;

    private @NotNull List<Correlation> correlations;

    private static final Logger logger = LoggerFactory.getLogger(CollaborationCard.class);


    public static @NotNull Pair<CollaborationCard, List<Correlation>> createNew(@NotNull Collaboration collaboration,
            @NotNull UUID rootCardId, @NotNull UUID cardOwnerUserId, @NotNull UUID cardOwnerDeckId) {
        logger.trace("Creating new CollaborationCard...");
        logger.debug("root-card-id is {}.", rootCardId);

        Correlation cardOwnerCorrelation = Correlation.makeNewWithCard(
                cardOwnerUserId, cardOwnerDeckId, rootCardId
        );
        logger.debug("Card-Owner Correlation: {}", cardOwnerCorrelation);

        List<Correlation> pendingCorrelations = collaboration.getParticipants().values().stream()
                .filter(x -> x.isActive() && x.getDeck() != null)
                .filter(x -> !x.getUser().getUserId().equals(cardOwnerUserId))
                .map(x -> Correlation.makeNew(rootCardId, x.getUserId(), x.getDeck().getDeckId()))
                .toList();
        logger.debug("Created {} Correlations for other Participants which are active and have a Deck.", pendingCorrelations.size());
        logger.debug("{}", pendingCorrelations);

        List<Correlation> allCorrelations = Stream.concat(
                pendingCorrelations.stream(), Stream.of(cardOwnerCorrelation)
        ).toList();
        logger.debug("Resulting in a total of {} new Correlations.", allCorrelations.size());

        CollaborationCard collaborationCard =  new CollaborationCard(
                UUID.randomUUID(), collaboration.getCollaborationId(), rootCardId, allCorrelations
        );
        return Pair.with(collaborationCard, pendingCorrelations);
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
                Stream.of(updatedCorrelation)
        ).toList();
        logger.trace("Added updated Correlation to list of Correlations.");
        return updatedCorrelation;
    }

    public @NotNull Triplet<List<Correlation>, List<Correlation>, List<Correlation>> addNewCardVersion(@NotNull Collaboration collaboration,
            @NotNull UUID parentCardId, @NotNull UUID newCardId, @NotNull UUID cardOwnerUserId, @NotNull UUID cardOwnerDeckId) {
        logger.trace("Creating new CardVersion...");
        UUID newRootCardId = newCardId;
        UUID oldRootCardId = this.correlations.stream()
                .filter(x -> x.cardId() != null)
                .filter(x -> x.cardId().equals(parentCardId))
                .map(Correlation::rootCardId)
                .findFirst().get();
        logger.debug("Old root-card-id is {}.", oldRootCardId);
        logger.debug("New root-card-id is {}.", newRootCardId);

        Correlation cardOwnerCorrelation = Correlation.makeNewWithCard(
                cardOwnerUserId, cardOwnerDeckId, newRootCardId
        );
        logger.debug("Card-Owner Correlation: {}", cardOwnerCorrelation);

        List<Correlation> newOverrideCorrelations = correlations.stream()
                .filter(x -> x.rootCardId().equals(oldRootCardId))
                .filter(x -> !x.userId().equals(cardOwnerUserId))
                .filter(x -> collaboration.getParticipants().get(x.userId()).isActive())
                .filter(x -> x.cardId() != null)
                .map(x -> Correlation.makeNewAsOverride(newRootCardId, x.userId(), x.deckId(), x.cardId()))
                .toList();
        logger.debug("Created {} new Override-Correlations.", newOverrideCorrelations.size());
        logger.debug("{}", newOverrideCorrelations);

        List<Correlation> newCloneCorrelations = collaboration.getParticipants().values().stream()
                .filter(x -> x.isActive() && x.getDeck() != null)
                .filter(x -> !x.getUserId().equals(cardOwnerUserId))
                .filter(x -> newOverrideCorrelations.stream()
                        .noneMatch(y -> x.getUserId().equals(y.userId())))
                .map(x -> Correlation.makeNew(newRootCardId, x.getUserId(), x.getDeck().getDeckId()))
                .toList();

        logger.debug("Created {} new Clone-Correlations.", newCloneCorrelations.size());
        logger.debug("{}", newCloneCorrelations);

        this.correlations = Stream.of(
                correlations, List.of(cardOwnerCorrelation), newOverrideCorrelations, newCloneCorrelations)
                .flatMap(Collection::stream)
                .toList();

        return Triplet.with(newOverrideCorrelations, newCloneCorrelations,
                Stream.of(
                    List.of(cardOwnerCorrelation),
                    newOverrideCorrelations,
                    newCloneCorrelations
                ).flatMap(Collection::stream)
                .toList()
        );
    }

    @Override
    public String toString() {
        return "CollaborationCard{" +
                "collaborationCardId=" + collaborationCardId +
                ", collaborationId=" + collaborationId +
                ", currentCardId=" + currentCardId +
                ", correlations=" + correlations +
                '}';
    }
}