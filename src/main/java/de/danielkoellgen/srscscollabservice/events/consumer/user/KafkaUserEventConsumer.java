package de.danielkoellgen.srscscollabservice.events.consumer.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaUserEventConsumer {

    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(KafkaUserEventConsumer.class);

    @Autowired
    public KafkaUserEventConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(topics = {"${kafka.topic.users}"}, id = "${kafka.groupId.users}")
    public void receive(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        String eventName = getHeaderValue(event, "type");
        switch (eventName) {
            case "user-created"     -> processUserCreatedEvent(event);
            case "user-disabled"    -> processUserDisabledEvent(event);
            default -> {
                logger.trace("Received event on 'cdc.users.0' of unknown type '{}'.", eventName);
                throw new RuntimeException("Received event on 'cdc.users.0' of unknown type '"+eventName+"'.");
            }
        }
    }

    private void processUserCreatedEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        UserCreated userCreated = new UserCreated(userService, event);
        logger.trace("Received 'UserCreated' event. [tid={}, payload={}]",
                userCreated.getTransactionId(), userCreated);
        userCreated.execute();
    }

    private void processUserDisabledEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        UserDisabled userDisabled = new UserDisabled(userService, event);
        logger.trace("Received 'UserDisabled' event. [tid={}, payload={}]",
                userDisabled.getTransactionId(), userDisabled);
        userDisabled.execute();
    }

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }
}
