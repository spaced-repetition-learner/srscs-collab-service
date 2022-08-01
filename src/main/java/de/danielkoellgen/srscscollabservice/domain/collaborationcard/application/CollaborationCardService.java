package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.CollaborationCardRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CollaborationCardService {

    private final ExternallyCreatedCardService externallyCreatedCardService;
    private final ExternallyOverriddenCardService externallyOverriddenCardService;

    private final CollaborationCardRepository collaborationCardRepository;

    private final Logger logger = LoggerFactory.getLogger(CollaborationCardService.class);

    @Autowired
    public CollaborationCardService(ExternallyCreatedCardService externallyCreatedCardService,
            ExternallyOverriddenCardService externallyOverriddenCardService,
            CollaborationCardRepository collaborationCardRepository) {
        this.externallyCreatedCardService = externallyCreatedCardService;
        this.externallyOverriddenCardService = externallyOverriddenCardService;
        this.collaborationCardRepository = collaborationCardRepository;
    }

    public void processNewlyExternallyCreatedCard(@Nullable UUID correlationId, @NotNull UUID cardId,
            @NotNull UUID deckId, @NotNull UUID userId) {
        logger.trace("Processing externally created Card...");
        if (correlationId != null) {
            logger.debug("With correlation-id {}.", correlationId);
            if (updateCorrelationWithCard(correlationId, cardId)) {
                return;
            }
        }
        externallyCreatedCardService.createNewCollaborationCard(cardId, deckId, userId);
    }

    public void processExternallyOverriddenCard(@Nullable UUID correlationId,
            @NotNull UUID parentCardId, @NotNull UUID newCardId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        logger.trace("Processing externally overriding Card.");
        if (correlationId != null) {
            logger.debug("With correlation-id {}.", correlationId);
            if (updateCorrelationWithCard(correlationId, newCardId)) {
                return;
            }
        }
        externallyOverriddenCardService.updateToNewCardVersion(parentCardId, newCardId, deckId,
                userId);
    }

    private Boolean updateCorrelationWithCard(@NotNull UUID correlationId, @NotNull UUID cardId) {
        logger.trace("Updating CollaborationsCard's Correlation with Card...");
        logger.trace("Fetching CollaborationCard by correlation-id {}...", correlationId);
        Optional<CollaborationCard> collaborationCardByCorrelation = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCorrelationId(correlationId);

        if (collaborationCardByCorrelation.isPresent()) {
            CollaborationCard partialCollaboration = collaborationCardByCorrelation.get();
            logger.debug("Matching CollaborationCard fetched.");
            logger.debug("{}", partialCollaboration);

            Correlation updatedCorrelation = partialCollaboration.addCard(correlationId, cardId);
            logger.debug("Card added to CollaborationCard.");
            logger.debug("{}", updatedCorrelation);

            collaborationCardRepository.saveUpdatedCorrelation(partialCollaboration, updatedCorrelation);
            logger.trace("Updated CollaborationCard was saved.");
            logger.info("Correlation updated with Card.");
            return true;
        }

        logger.debug("No matching CollaborationCard was found.");
        return false;
    }
}