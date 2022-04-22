package de.danielkoellgen.srscscollabservice.events.consumer.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import de.danielkoellgen.srscscollabservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscscollabservice.events.consumer.user.dto.UserCreatedDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class UserCreated extends AbstractConsumerEvent {

    private final UserService userService;

    @Getter
    private final @NotNull UserCreatedDto payload;

    public UserCreated(@NotNull UserService userService, @NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        super(event);
        this.userService = userService;
        this.payload = UserCreatedDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        userService.addExternallyCreatedUser(transactionId, payload.userId(), payload.getMappedUsername());
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
