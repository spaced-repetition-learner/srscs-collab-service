package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.CloneCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.CloneCardDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.CollaborationCardRepository;
import de.danielkoellgen.srscscollabservice.events.producer.KafkaProducer;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExternallyCreatedCardService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationCardRepository collaborationCardRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    private Tracer tracer;

    private final Logger logger = LoggerFactory.getLogger(ExternallyCreatedCardService.class);

    @Autowired
    public ExternallyCreatedCardService(CollaborationRepository collaborationRepository,
            CollaborationCardRepository collaborationCardRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void createNewCollaborationCard(@NotNull UUID cardId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        logger.trace("Creating new CollaborationCard...");
        logger.trace("Fetching Collaboration by deck-id {}...", deckId);
        Optional<Collaboration> collaborationByDeckId = collaborationRepository
                .findCollaborationByDeckId(deckId);

        if (collaborationByDeckId.isPresent()) {
            Collaboration fullCollaboration = collaborationByDeckId.get();
            logger.debug("Matching Collaboration fetched.");
            logger.debug("{}", fullCollaboration);

            Pair<CollaborationCard, List<Correlation>> response = CollaborationCard
                    .createNew(fullCollaboration, cardId, userId, deckId);
            CollaborationCard newCollaborationCard = response.getValue0();
            List<Correlation> newCorrelations = response.getValue1();
            logger.debug("New CollaborationCard created with {} unmatched correlations.",
                    newCorrelations.size());
            logger.debug("{}", newCollaborationCard);

            collaborationCardRepository.saveNewCollaborationCard(newCollaborationCard);
            logger.trace("New CollaborationCard saved.");

            logger.info("New CollaborationCard created for Card.");
            logger.info("Publishing {} Commands to clone Card into Decks...",
                    newCorrelations.size());
            newCorrelations.forEach(x -> kafkaProducer.send(
                    new CloneCard(getTraceIdOrEmptyString(), x.correlationId(),
                            new CloneCardDto(x.rootCardId(), x.deckId()))
            ));
            return;
        }

        logger.debug("No Collaboration was found.");
        logger.trace("No new CollaborationCard was created.");
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
