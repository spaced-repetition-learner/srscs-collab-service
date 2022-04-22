package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CollaborationCardService {

    private final ExternallyCreatedCardService externallyCreatedCardService;
    private final ExternallyOverriddenCardService externallyOverriddenCardService;

    private final Logger logger = LoggerFactory.getLogger(CollaborationCardService.class);

    @Autowired
    public CollaborationCardService(ExternallyCreatedCardService externallyCreatedCardService,
            ExternallyOverriddenCardService externallyOverriddenCardService) {
        this.externallyCreatedCardService = externallyCreatedCardService;
        this.externallyOverriddenCardService = externallyOverriddenCardService;
    }

    public void processNewlyExternallyCreatedCard(@NotNull UUID transactionId, @NotNull UUID correlationId, @NotNull UUID cardId,
            @NotNull UUID deckId, @NotNull UUID userId) {
        externallyCreatedCardService.processCard(transactionId, correlationId, cardId, deckId, userId);
    }

    public void processExternallyOverriddenCard(@NotNull UUID transactionId, @NotNull UUID correlationId,
            @NotNull UUID parentCardId, @NotNull UUID newCardId, @NotNull UUID deckId, @NotNull UUID userId) {
        externallyOverriddenCardService.processCard(transactionId, correlationId, parentCardId, newCardId, deckId, userId);
    }
}