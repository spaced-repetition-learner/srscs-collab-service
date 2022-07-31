package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.application.CollaborationCardService;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto.CardOverriddenDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class CardOverridden extends AbstractConsumerEvent {

    private final CollaborationCardService collaborationCardService;

    @Getter
    private final @NotNull CardOverriddenDto payload;

    public CardOverridden(@NotNull CollaborationCardService collaborationCardService,
            @NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        super(event);
        this.collaborationCardService = collaborationCardService;
        this.payload = CardOverriddenDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        collaborationCardService.processExternallyOverriddenCard(
                correlationId, payload.parentCardId(), payload.newCardId(), payload.deckId(), payload.userId()
        );
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
        return "CardOverridden{" +
                "payload=" + payload +
                ", " + super.toString() +
                '}';
    }
}
