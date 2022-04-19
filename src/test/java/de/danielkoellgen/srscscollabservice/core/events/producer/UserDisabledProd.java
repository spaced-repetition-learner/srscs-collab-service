package de.danielkoellgen.srscscollabservice.core.events.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.EventDateTime;
import de.danielkoellgen.srscscollabservice.events.consumer.user.dto.UserDisabledDto;
import de.danielkoellgen.srscscollabservice.events.producer.AbstractProducerEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDisabledProd extends AbstractProducerEvent {

    private final @NotNull UserDisabledDto payloadDto;

    public static final String eventName = "user-disabled";

    public static final String eventTopic = "cdc.users.0";

    public UserDisabledProd(@NotNull UUID transactionId, @NotNull UserDisabledDto payloadDto) {
        super(UUID.randomUUID(), transactionId, eventName, eventTopic,
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
