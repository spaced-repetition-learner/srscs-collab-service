package de.danielkoellgen.srscscollabservice.events.consumer;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ConsumerEvent {

    @NotNull UUID getEventId();

    @NotNull UUID getTransactionId();

    @NotNull String getEventName();

    @NotNull EventDateTime getOccurredAt();

    @NotNull EventDateTime getReceivedAt();

    @NotNull String getTopic();

    @NotNull String getSerializedContent();
}
