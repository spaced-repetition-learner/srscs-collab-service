package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.card.domain.Card;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@AllArgsConstructor
public class CardVersion {

    @NotNull
    private final UUID cardVersionId;

    @Nullable
    private Card rootCard;

    @NotNull
    private Set<Correlation> correlations;

    public CardVersion(@NotNull UUID cardVersionId) {
        this.cardVersionId = cardVersionId;
    }

    public CardVersion(@NotNull UUID cardVersionId, @NotNull Set<Correlation> correlations) {
        this.cardVersionId = cardVersionId;
        this.correlations = correlations;
    }

    public CardVersion(@NotNull UUID cardVersionId, @NotNull Card rootCard) {
        this.cardVersionId = cardVersionId;
        this.rootCard = rootCard;
    }

    public static @NotNull CardVersion createNewCardVersion(@NotNull Collaboration collaboration,
            @NotNull UUID transactionId, @NotNull Card card) {
        assert collaboration.getParticipants() != null;
        assert card.getDeck() != null;

        List<Correlation> correlations = new ArrayList<>();
        for (Participant participant : collaboration.getParticipants().values()) {
            if (participant.getCurrentState().status() == ParticipantStatus.INVITATION_ACCEPTED) {
                if (participant.getDeck().getDeckId() != card.getDeck().getDeckId()) {
                    correlations.add(new Correlation(null, UUID.randomUUID()));
                }
            }
        }
        correlations.add(new Correlation(card, transactionId));
        return new CardVersion(UUID.randomUUID(), card, new HashSet<>(correlations));
    }

    public @NotNull Correlation appendCardByTransactionId(UUID transactionId, Card card)
            throws NoSuchElementException {
        for (Correlation correlation : correlations) {
            if (correlation.transactionId().equals(transactionId)) {
                Correlation updatedCorrelation = new Correlation(card, transactionId);
                correlations.remove(correlation);
                correlations.add(updatedCorrelation);
                return updatedCorrelation;
            }
        }
        throw new NoSuchElementException("No matching Correlation was found.");
    }
}
