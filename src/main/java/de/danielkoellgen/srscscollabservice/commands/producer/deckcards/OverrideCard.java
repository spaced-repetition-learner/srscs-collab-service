package de.danielkoellgen.srscscollabservice.commands.producer.deckcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.OverrideCardDto;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import de.danielkoellgen.srscscollabservice.events.producer.AbstractProducerEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class OverrideCard extends AbstractProducerEvent {

    @NotNull
    private final OverrideCardDto payloadDto;

    public static final String eventName = "override-card";

    public static final String eventTopic = "cmd.decks-cards.0";

    public OverrideCard(@NotNull UUID transactionId, @NotNull UUID correlationId, @NotNull OverrideCardDto payloadDto) {
        super(UUID.randomUUID(), transactionId, correlationId, eventName, eventTopic,
                EventDateTime.makeFromLocalDateTime(LocalDateTime.now())
        );
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
}
