package de.danielkoellgen.srscscollabservice.events.consumer;

import de.danielkoellgen.srscsdeckservice.domain.domainprimitive.EventDateTime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

abstract public class AbstractConsumerEvent implements ConsumerEvent {

    protected final @NotNull UUID eventId;

    protected final @NotNull UUID transactionId;

    protected final @NotNull String eventName;

    protected final @NotNull EventDateTime occurredAt;

    protected final @NotNull EventDateTime receivedAt;

    protected final @NotNull String topic;

    public AbstractConsumerEvent(@NotNull ConsumerRecord<String, String> event) {
        this.eventId = UUID.fromString(getHeaderValue(event, "eventId"));
        this.transactionId = UUID.fromString(getHeaderValue(event, "transactionId"));
        this.eventName = getHeaderValue(event, "type");
        this.occurredAt = EventDateTime.makeFromFormattedString(getHeaderValue(event, "timestamp"));
        this.receivedAt = new EventDateTime(LocalDateTime.now());
        this.topic = event.topic();
    }

    abstract public void execute();

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }

    @Override
    public @NotNull UUID getEventId() {
        return eventId;
    }

    @Override
    public @NotNull UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public @NotNull String getEventName() {
        return eventName;
    }

    @Override
    public @NotNull EventDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public @NotNull EventDateTime getReceivedAt() {
        return receivedAt;
    }

    @Override
    public @NotNull String getTopic() {
        return topic;
    }
}
