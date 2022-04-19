package de.danielkoellgen.srscscollabservice.events.producer;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ProducerEvent {

    @NotNull UUID getEventId();

    @NotNull String getEventName();

    @NotNull UUID getTransactionId();

    @NotNull EventDateTime getOccurredAt();

    @NotNull String getTopic();

    @NotNull String getSerializedContent();
}
