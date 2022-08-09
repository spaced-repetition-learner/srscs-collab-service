package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.OverrideCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.OverrideCardDto;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.CollaborationCardRepository;
import de.danielkoellgen.srscscollabservice.events.producer.KafkaProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CollaborationCardService {

    private final ExternallyCreatedCardService externallyCreatedCardService;
    private final ExternallyOverriddenCardService externallyOverriddenCardService;

    private final CollaborationCardRepository collaborationCardRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    private Tracer tracer;

    private final Logger log = LoggerFactory.getLogger(CollaborationCardService.class);

    @Autowired
    public CollaborationCardService(ExternallyCreatedCardService externallyCreatedCardService,
            ExternallyOverriddenCardService externallyOverriddenCardService,
            CollaborationCardRepository collaborationCardRepository,
            KafkaProducer kafkaProducer) {
        this.externallyCreatedCardService = externallyCreatedCardService;
        this.externallyOverriddenCardService = externallyOverriddenCardService;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void processNewlyExternallyCreatedCard(@Nullable UUID correlationId, @NotNull UUID cardId,
            @NotNull UUID deckId, @NotNull UUID userId) {
        log.trace("Processing externally created Card...");
        if (correlationId != null) {
            log.trace("Correlation-id is '{}'.", correlationId);
            if (updateCorrelationWithCard(correlationId, cardId)) {
                return;
            }
        }
        externallyCreatedCardService.createNewCollaborationCard(cardId, deckId, userId);
    }

    public void processExternallyOverriddenCard(@Nullable UUID correlationId,
            @NotNull UUID parentCardId, @NotNull UUID newCardId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        log.trace("Processing externally overriding Card.");
        if (correlationId != null) {
            log.trace("Correlation-id is '{}'.", correlationId);
            if (updateCorrelationWithCard(correlationId, newCardId)) {
                return;
            }
        }
        externallyOverriddenCardService.updateToNewCardVersion(parentCardId, newCardId, deckId,
                userId);
    }

    private Boolean updateCorrelationWithCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        log.trace("Updating correlation with card...");

        Optional<CollaborationCard> collaborationCardByCorrelation = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCorrelationId(correlationId);
        if (collaborationCardByCorrelation.isEmpty()) {
            log.trace("No matching collaboration-card by correlation-id found.");
            return false;
        }
        log.debug("CollaborationCard fetched by correlation-id: {}",
                collaborationCardByCorrelation.get());
        CollaborationCard partialCollaborationCard = collaborationCardByCorrelation.get();
        Correlation updatedCorrelation = partialCollaborationCard.addCard(correlationId, cardId);
        collaborationCardRepository.saveUpdatedCorrelation(partialCollaborationCard,
                updatedCorrelation);
        log.debug("Updated correlation: {}", updatedCorrelation);

        Optional<Correlation> latestCardUpdate = partialCollaborationCard
                .updateToLatestCardEvent(updatedCorrelation);
        if (latestCardUpdate.isPresent()) {
            log.trace("Card update available. Sending command...");
            log.debug("Latest update is {}", latestCardUpdate.get());
            kafkaProducer.send(new OverrideCard(
                    getTraceIdOrEmptyString(),
                    latestCardUpdate.get().correlationId(),
                    new OverrideCardDto(
                            latestCardUpdate.get().deckId(),
                            latestCardUpdate.get().parentCardId(),
                            latestCardUpdate.get().rootCardId())));
        } else {
            log.trace("Card is the newest version.");
            collaborationCardRepository.releaseLock(
                    partialCollaborationCard.getCollaborationCardId(),
                    updatedCorrelation.userId());
        }

        return true;
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}