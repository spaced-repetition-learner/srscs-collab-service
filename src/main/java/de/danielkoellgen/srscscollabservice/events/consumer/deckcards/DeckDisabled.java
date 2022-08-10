package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto.DeckDisabledDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class DeckDisabled extends AbstractConsumerEvent {

    private final @NotNull CollaborationService collaborationService;

    @Getter
    private final @NotNull DeckDisabledDto payload;

    public DeckDisabled(@NotNull CollaborationService collaborationService,
            @NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        super(event);
        this.collaborationService = collaborationService;
        this.payload = DeckDisabledDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        try {
            collaborationService.endParticipationViaDeck(payload.deckId(), payload.userId());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        return "DeckDisabled{" +
                "payload=" + payload +
                ", " + super.toString() +
                '}';
    }
}
