package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto.DeckDisabledDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class DeckDisabled extends AbstractConsumerEvent {

    @Getter
    private final @NotNull DeckDisabledDto payload;

    public DeckDisabled(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        super(event);
        this.payload = DeckDisabledDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        //NO IMPLEMENTATION
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
