package de.danielkoellgen.srscscollabservice.events.consumer.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.danielkoellgen.srscscollabservice.domain.user.application.UserService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaUserEventConsumer {

    private final UserService userService;

    @Autowired
    private Tracer tracer;

    private final Logger log = LoggerFactory.getLogger(KafkaUserEventConsumer.class);

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
                log.debug("Received event on 'cdc.users.0' of unknown type '{}'.", eventName);
                throw new RuntimeException("Received event on 'cdc.users.0' of unknown type '" +
                        eventName+"'.");
            }
        }
    }

    private void processUserCreatedEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-user-created");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            UserCreated userCreated = new UserCreated(userService, event);
            log.info("Received 'UserCreatedEvent'. {}", userCreated);
            userCreated.execute();

        } finally {
            newSpan.end();
        }
    }

    private void processUserDisabledEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-user-disabled");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            UserDisabled userDisabled = new UserDisabled(userService, event);
            log.info("Received 'UserDisabledEvent'. {}", userDisabled);
            userDisabled.execute();

        } finally {
            newSpan.end();
        }
    }

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }
}
