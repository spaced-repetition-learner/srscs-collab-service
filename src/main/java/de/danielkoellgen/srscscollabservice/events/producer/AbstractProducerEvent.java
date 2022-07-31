package de.danielkoellgen.srscscollabservice.events.producer;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

abstract public class AbstractProducerEvent implements ProducerEvent {

    @NotNull
    private final UUID eventId;

    @NotNull
    private final String transactionId;

    @Nullable
    private final UUID correlationId;

    @NotNull
    private final String eventName;

    @NotNull
    private final String topic;

    @NotNull
    private final EventDateTime occurredAt;

    public AbstractProducerEvent(
            @NotNull UUID eventId, @NotNull String transactionId, @Nullable UUID correlationId, @NotNull String eventName,
            @NotNull String topic, @NotNull EventDateTime occurredAt
    ) {
        this.eventId = eventId;
        this.transactionId = transactionId;
        this.correlationId = correlationId;
        this.eventName = eventName;
        this.topic = topic;
        this.occurredAt = occurredAt;
    }

    @Override
    public @NotNull UUID getEventId() {
        return eventId;
    }

    @Override
    public @NotNull String getTransactionId() {
        return transactionId;
    }

    @Override
    public @Nullable UUID getCorrelationId() {
        return correlationId;
    }

    @Override
    public @NotNull String getEventName() {
        return eventName;
    }

    @Override
    public @NotNull String getTopic() {
        return topic;
    }

    @Override
    public @NotNull EventDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "AbstractProducerEvent{" +
                "eventId=" + eventId +
                ", transactionId='" + transactionId + '\'' +
                ", correlationId=" + correlationId +
                ", eventName='" + eventName + '\'' +
                ", topic='" + topic + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
