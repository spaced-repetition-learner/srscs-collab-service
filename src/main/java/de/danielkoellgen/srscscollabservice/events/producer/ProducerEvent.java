package de.danielkoellgen.srscscollabservice.events.producer;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ProducerEvent {

    @NotNull UUID getEventId();

    @NotNull String getEventName();

    @NotNull String getTransactionId();

    @Nullable UUID getCorrelationId();

    @NotNull EventDateTime getOccurredAt();

    @NotNull String getTopic();

    @NotNull String getSerializedContent();
}
