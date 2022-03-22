package de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain;

import de.danielkoellgen.srscscollabservice.domain.card.domain.Card;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record Correlation (

        @Nullable
        Card card,

        @NotNull
        UUID transactionId
) {
}
