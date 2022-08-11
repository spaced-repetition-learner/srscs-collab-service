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
        log.trace("Processing externally created Card '{}' in Deck '{}'...", cardId, deckId);
        log.debug("Correlation-id is '{}'...", correlationId);
        if (correlationId != null) {
            if (updateCorrelationWithCard(correlationId, cardId)) {
                return;
            }
        }
        externallyCreatedCardService.createNewCollaborationCard(cardId, deckId, userId);
    }

    public void processExternallyOverriddenCard(@Nullable UUID correlationId,
            @NotNull UUID parentCardId, @NotNull UUID newCardId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        log.trace("Processing externally overriding Card...");
        log.debug("Correlation-id is '{}'...", correlationId);
        if (correlationId != null) {
            if (updateCorrelationWithCard(correlationId, newCardId)) {
                return;
            }
        }
        externallyOverriddenCardService.updateToNewCardVersion(parentCardId, newCardId,
                deckId, userId);
    }

    private Boolean updateCorrelationWithCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        log.trace("Updating matching CollaborationCard-Correlation with Card by correlation-id...");

        log.trace("Fetching CollaborationCard by correlation-id '{}'...", correlationId);
        Optional<CollaborationCard> collaborationCardByCorrelation = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCorrelationId(correlationId);
        if (collaborationCardByCorrelation.isEmpty()) {
            log.info("No matching CollaborationCard was found. Card does not belong to any Collaboration.");
            return false;
        }
        CollaborationCard partialCollaborationCard = collaborationCardByCorrelation.get();
        log.debug("Fetched CollaborationCard: {}", partialCollaborationCard);

        Correlation updatedCorrelation = partialCollaborationCard.addCard(correlationId, cardId);
        collaborationCardRepository.saveUpdatedCorrelation(partialCollaborationCard,
                updatedCorrelation);
        log.info("Card added to matching Correlation.");
        log.debug("Updated Correlation: {}", updatedCorrelation);

        log.trace("Checking if a newer update exists...");
        Optional<Correlation> latestCardUpdate = partialCollaborationCard.updateToLatestCardEvent(
                updatedCorrelation);
        if (latestCardUpdate.isPresent()) {
            log.info("Newer Card-Version available. Starting update-process.");
            log.debug("Latest Update-Correlation is {}", latestCardUpdate.get());
            kafkaProducer.send(new OverrideCard(
                    getTraceIdOrEmptyString(),
                    latestCardUpdate.get().correlationId(),
                    new OverrideCardDto(
                            latestCardUpdate.get().deckId(),
                            latestCardUpdate.get().parentCardId(),
                            latestCardUpdate.get().rootCardId())));
        } else {
            log.info("No Update available. Card has the newest version.");
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