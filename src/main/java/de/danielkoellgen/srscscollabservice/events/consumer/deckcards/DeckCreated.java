package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto.DeckCreatedDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class DeckCreated extends AbstractConsumerEvent {

    private final @NotNull CollaborationService collaborationService;

    @Getter
    private final @NotNull DeckCreatedDto payload;

    public DeckCreated(@NotNull CollaborationService collaborationService, @NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        super(event);
        this.collaborationService = collaborationService;
        this.payload = DeckCreatedDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        if (correlationId != null) {
            collaborationService.addCorrespondingDeckToParticipant(
                    transactionId, correlationId, payload.deckId(), payload.userId());
        }
    }

    @Override
    public @NotNull String getSerializedContent() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("ObjectMapper conversion failed.");
        }
    }

    @Override
    public String toString() {
        return "UserCreated{" +
                "eventId=" + eventId +
                ", transactionId=" + transactionId +
                ", eventName='" + eventName + '\'' +
                ", occurredAt=" + occurredAt +
                ", receivedAt=" + receivedAt +
                ", topic='" + topic + '\'' +
                ", payload=" + payload +
                '}';
    }
}
