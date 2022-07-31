package de.danielkoellgen.srscscollabservice.core.events.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import de.danielkoellgen.srscscollabservice.events.consumer.user.dto.UserCreatedDto;
import de.danielkoellgen.srscscollabservice.events.producer.AbstractProducerEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserCreatedProd extends AbstractProducerEvent {

    private final @NotNull UserCreatedDto payloadDto;

    public static final String eventName = "user-created";

    public static final String eventTopic = "cdc.users.0";

    public UserCreatedProd(@NotNull String transactionId, @NotNull UserCreatedDto payloadDto) {
        super(UUID.randomUUID(), transactionId, null, eventName, eventTopic,
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
