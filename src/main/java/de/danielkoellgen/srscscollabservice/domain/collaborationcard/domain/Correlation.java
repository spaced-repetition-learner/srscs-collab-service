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

        @Nullable UUID cardId,

        @Nullable UUID parentCardId
) {
    public Correlation(@NotNull CorrelationByRootCardId map) {
        this(map.getRootCardId(), map.getCorrelationId(), map.getUserId(), map.getDeckId(), map.getCardId(), null);
    }

    public static @NotNull Correlation makeNew(@NotNull UUID rootCardId, @NotNull UUID userId, @NotNull UUID deckId) {
        return new Correlation(rootCardId, UUID.randomUUID(), userId, deckId, null, null);
    }

    public static @NotNull Correlation makeNewAsOverride(@NotNull UUID rootCardId, @NotNull UUID userId,
            @NotNull UUID deckId, @NotNull UUID parentCardId) {
        return new Correlation(rootCardId, UUID.randomUUID(), userId, deckId, null, parentCardId);
    }

    public static @NotNull Correlation makeNewWithCard(@NotNull UUID userId, @NotNull UUID deckId, @NotNull UUID cardId) {
        return new Correlation(cardId, UUID.randomUUID(), userId, deckId, cardId, null);
    }

    public static @NotNull Correlation makeNewAsOverrideWithCard(@NotNull UUID userId, @NotNull UUID deckId,
            @NotNull UUID cardId, @NotNull UUID parentCardId) {
        return new Correlation(cardId, UUID.randomUUID(), userId, deckId, cardId, parentCardId);
    }

    public Correlation addCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        if (!this.correlationId.equals(correlationId)) {
            throw new RuntimeException("Correlation-ids do not match.");
        }
        return new Correlation(rootCardId, correlationId, userId, deckId, cardId, null);
    }

    public Boolean isPending() {
        return cardId == null;
    }
}
