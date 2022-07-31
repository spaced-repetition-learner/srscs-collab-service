package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto.CardDisabledDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class CardDisabled extends AbstractConsumerEvent {

    @Getter
    private final @NotNull CardDisabledDto payload;

    public CardDisabled(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        super(event);
        this.payload = CardDisabledDto.makeFromSerialization(event.value());
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
        return "CardDisabled{" +
                "payload=" + payload +
                ", " + super.toString() +
                '}';
    }
}
