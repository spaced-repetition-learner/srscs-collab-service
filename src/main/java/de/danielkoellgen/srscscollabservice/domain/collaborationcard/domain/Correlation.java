package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map.CorrelationByRootCardId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public record Correlation (

        @NotNull UUID rootCardId,

        @NotNull UUID correlationId,

        @NotNull UUID userId,

        @NotNull UUID deckId,

        @Nullable UUID cardId
) {
    public Correlation(@NotNull CorrelationByRootCardId map) {
        this(map.getRootCardId(), map.getCorrelationId(), map.getUserId(), map.getDeckId(), map.getCardId());
    }

    public static @NotNull Correlation makeNew(@NotNull UUID rootCardId, @NotNull UUID userId, @NotNull UUID deckId) {
        return new Correlation(rootCardId, UUID.randomUUID(), userId, deckId, null);
    }

    public static @NotNull Correlation makeNewWithCard(@NotNull UUID userId, @NotNull UUID deckId, @NotNull UUID cardId) {
        return new Correlation(cardId, UUID.randomUUID(), userId, deckId, cardId);
    }

    public Correlation addCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        if (!this.correlationId.equals(correlationId)) {
            throw new RuntimeException("Correlation-ids do not match.");
        }
        return new Correlation(rootCardId, correlationId, userId, deckId, cardId);
    }

    public Boolean isPending() {
        return cardId == null;
    }
}
