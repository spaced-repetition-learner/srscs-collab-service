package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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


    public static @NotNull Pair<CollaborationCard, List<Correlation>> createNew(@NotNull Collaboration collaboration,
            @NotNull UUID rootCardId, @NotNull UUID cardOwnerUserId, @NotNull UUID cardOwnerDeckId) {
        Correlation cardOwnerCorrelation = Correlation.makeNewWithCard(
                cardOwnerUserId, rootCardId, cardOwnerDeckId);
        List<Correlation> pendingCorrelations = collaboration.getParticipants().values().stream()
                .filter(x -> x.isActive())
                .filter(x -> !x.getUser().getUserId().equals(cardOwnerUserId))
                .map(x -> Correlation.makeNew(rootCardId, x.getUserId(), x.getDeck().getDeckId()))
                .toList();
        List<Correlation> allCorrelations = Stream.concat(
                pendingCorrelations.stream(), Stream.of(cardOwnerCorrelation)
                ).toList();
        CollaborationCard collaborationCard =  new CollaborationCard(
                UUID.randomUUID(), collaboration.getCollaborationId(), rootCardId, allCorrelations
        );
        return Pair.with(collaborationCard, pendingCorrelations);
    }

    public @NotNull Correlation addCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        Correlation updatedCorrelation = correlations.stream()
                .filter(x -> x.correlationId().equals(correlationId))
                .map(x -> x.addCard(correlationId, cardId))
                .findFirst()
                .get();
        correlations = Stream.concat(
                correlations.stream()
                        .filter(x -> !x.correlationId().equals(correlationId)),
                Stream.of(updatedCorrelation)
        ).toList();
        return updatedCorrelation;
    }

    public @NotNull Pair<List<Correlation>, List<Correlation>> addNewCardVersion(@NotNull Collaboration collaboration,
            @NotNull UUID rootCardId, @NotNull UUID newCardId, @NotNull UUID cardOwnerUserId, @NotNull UUID cardOwnerDeckId) {
        Correlation cardOwnerCorrelation = Correlation.makeNewWithCard(
                cardOwnerUserId, cardOwnerDeckId, newCardId
        );
        List<Correlation> newOverrideCorrelations = correlations.stream()
                .filter(x -> x.rootCardId().equals(rootCardId))
                .filter(x -> !x.userId().equals(cardOwnerUserId))
                .filter(x -> collaboration.getParticipants().get(x.userId()).isActive())
                .map(x -> Correlation.makeNew(newCardId, x.userId(), x.deckId()))
                .toList();
        List<Correlation> newCloneCorrelations = collaboration.getParticipants().values().stream()
                .filter(x -> x.isActive())
                .filter(x -> correlations.stream()
                        .filter(y -> y.rootCardId().equals(rootCardId))
                        .noneMatch(y -> y.userId().equals(x.getUserId())))
                .map(x -> Correlation.makeNew(newCardId, x.getUserId(), x.getDeck().getDeckId()))
                .toList();
        this.correlations = Stream.of(
                correlations, List.of(cardOwnerCorrelation), newOverrideCorrelations, newCloneCorrelations)
                .flatMap(Collection::stream)
                .toList();
        return Pair.with(newOverrideCorrelations, newCloneCorrelations);
    }
}