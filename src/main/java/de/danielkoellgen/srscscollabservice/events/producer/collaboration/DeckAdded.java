package de.danielkoellgen.srscscollabservice.events.producer.collaboration;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import de.danielkoellgen.srscscollabservice.events.producer.AbstractProducerEvent;
import de.danielkoellgen.srscscollabservice.events.producer.collaboration.dto.DeckAddedDto;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeckAdded extends AbstractProducerEvent {

    private final @NotNull DeckAddedDto payloadDto;

    public static final String eventName = "deck-added";

    public static final String eventTopic = "cdc.collaboration.0";

    public DeckAdded(@NotNull String transactionId, @NotNull DeckAddedDto payloadDto) {
        super(UUID.randomUUID(), transactionId, null, eventName, eventTopic,
                EventDateTime.makeFromLocalDateTime(LocalDateTime.now()));
        this.payloadDto = payloadDto;
    }

    @Override
    public @NotNull String getSerializedContent() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            return objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            throw new RuntimeException("ObjectMapper conversion failed.");
        }
    }

    @Override
    public String toString() {
        return "DeckAdded{" +
                "payloadDto=" + payloadDto +
                ", " + super.toString() +
                '}';
    }
}