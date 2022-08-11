package de.danielkoellgen.srscscollabservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.application.CollaborationCardService;
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
public class KafkaDeckCardsEventConsumer {

    private final CollaborationService collaborationService;
    private final CollaborationCardService collaborationCardService;

    @Autowired
    private Tracer tracer;

    private final Logger log = LoggerFactory.getLogger(KafkaDeckCardsEventConsumer.class);

    @Autowired
    public KafkaDeckCardsEventConsumer(CollaborationService collaborationService,
            CollaborationCardService collaborationCardService) {
        this.collaborationService = collaborationService;
        this.collaborationCardService = collaborationCardService;
    }

    @KafkaListener(topics = {"${kafka.topic.deckscards}"}, id = "${kafka.groupId.deckscards}")
    public void receive(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        String eventName = getHeaderValue(event, "type");
        switch (eventName) {
            case "deck-created"     -> processDeckCreatedEvent(event);
            case "deck-disabled"    -> processDeckDisabledEvent(event);
            case "card-created"     -> processCardCreatedEvent(event);
            case "card-overridden"  -> processCardOverriddenEvent(event);
            case "card-disabled"    -> processCardDisabledEvent(event);
            default -> {
                log.warn("Received event on 'cdc.decks-cards.0' of unknown type '{}'.", eventName);
                throw new RuntimeException("Received event on 'cdc.decks-cards.0' of unknown type '" +
                        eventName+"'.");
            }
        }
    }

    private void processDeckCreatedEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-deck-created");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            DeckCreated deckCreated = new DeckCreated(collaborationService, event);
            log.info("Received 'DeckCreatedEvent' {}.", deckCreated);
            deckCreated.execute();
            log.trace("Event processing completed.");

        } finally {
            newSpan.end();
        }
    }

    private void processDeckDisabledEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-deck-disabled");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            DeckDisabled deckDisabled = new DeckDisabled(collaborationService, event);
            log.info("Received 'DeckDisabledEvent' {}.", deckDisabled);
            deckDisabled.execute();
            log.trace("Event processing completed.");

        } finally {
            newSpan.end();
        }
    }

    private void processCardCreatedEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-card-created");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            CardCreated cardCreated = new CardCreated(collaborationCardService, event);
            log.info("Received 'CardCreatedEvent' {}.", cardCreated);
            cardCreated.execute();
            log.trace("Event processing completed.");

        } finally {
            newSpan.end();
        }
    }

    private void processCardOverriddenEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-card-overridden");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            CardOverridden cardOverridden = new CardOverridden(collaborationCardService, event);
            log.info("Received 'CardOverriddenEvent'. {}", cardOverridden);
            cardOverridden.execute();
            log.trace("Event processing completed.");

        } finally {
            newSpan.end();
        }
    }

    private void processCardDisabledEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-card-disabled");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            CardDisabled cardDisabled = new CardDisabled(event);
            log.info("Received 'CardOverriddenEven'. {}", cardDisabled);
            cardDisabled.execute();
            log.trace("Event processing completed.");

        } finally {
            newSpan.end();
        }
    }

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }
}
