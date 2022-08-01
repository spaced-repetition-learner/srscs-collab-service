package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.application.CollaborationCardService;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto.CardCreatedDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class CardCreated extends AbstractConsumerEvent {

    private final CollaborationCardService collaborationCardService;

    @Getter
    private final @NotNull CardCreatedDto payload;

    public CardCreated(CollaborationCardService collaborationCardService,
            @NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        super(event);
        this.collaborationCardService = collaborationCardService;
        this.payload = CardCreatedDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        collaborationCardService.processNewlyExternallyCreatedCard(
                correlationId, payload.cardId(), payload.deckId(), payload.userId());
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
        return "CardCreated{" +
                "payload=" + payload +
                ", " + super.toString() +
                '}';
    }
}
