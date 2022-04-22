package de.danielkoellgen.srscscollabservice.events.consumer;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ConsumerEvent {

    @NotNull UUID getEventId();

    @NotNull UUID getTransactionId();

    @Nullable UUID getCorrelationId();

    @NotNull String getEventName();

    @NotNull EventDateTime getOccurredAt();

    @NotNull EventDateTime getReceivedAt();

    @NotNull String getTopic();

    @NotNull String getSerializedContent();
}
