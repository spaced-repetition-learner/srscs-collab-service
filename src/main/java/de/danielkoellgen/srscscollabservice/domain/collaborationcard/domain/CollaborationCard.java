package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.card.domain.Card;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CollaborationCard {

    @NotNull
    private final UUID collaborationCardId;

    @NotNull
    private Collaboration collaboration;

    @NotNull
    private List<CardVersion> cardVersions;


    public static @NotNull CollaborationCard createNewCollaborationCard(@NotNull Collaboration collaboration,
            @NotNull UUID transactionId, @NotNull Card card) {
        CardVersion cardVersion = CardVersion.createNewCardVersion(collaboration, transactionId, card);
        return new CollaborationCard(
                UUID.randomUUID(), collaboration, List.of(cardVersion));
    }

    public Pair<@NotNull CardVersion, @NotNull Correlation> appendCard(UUID transactionId, Card card)
            throws NoSuchElementException {
        for (CardVersion cardVersion : cardVersions) {
            try {
                Correlation correlation = cardVersion.appendCardByTransactionId(transactionId, card);
                return new Pair<>(cardVersion, correlation);
            } catch (NoSuchElementException ignored) {}
        }
        throw new NoSuchElementException("No matching Correlation for transactionId was found.");
    }

    // create new commands

    // create new Version
}
