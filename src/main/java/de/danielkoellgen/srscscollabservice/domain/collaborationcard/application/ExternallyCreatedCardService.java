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

    private final Logger log = LoggerFactory.getLogger(ExternallyCreatedCardService.class);

    @Autowired
    public ExternallyCreatedCardService(CollaborationRepository collaborationRepository,
            CollaborationCardRepository collaborationCardRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void createNewCollaborationCard(@NotNull UUID cardId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        log.trace("Creating new CollaborationCard...");
        log.trace("Fetching Collaboration by deck-id {}...", deckId);
        Optional<Collaboration> collaborationByDeckId = collaborationRepository
                .findCollaborationByDeckId(deckId);

        if (collaborationByDeckId.isPresent()) {
            Collaboration fullCollaboration = collaborationByDeckId.get();
            log.debug("Collaboration fetched by deck-id: {}", fullCollaboration);

            Pair<CollaborationCard, List<Correlation>> response = CollaborationCard
                    .createNew(fullCollaboration, cardId, userId, deckId);
            CollaborationCard newCollaborationCard = response.getValue0();
            List<Correlation> newCorrelations = response.getValue1();
            log.debug("New CollaborationCard created with {} unmatched correlations. " +
                            "New CollaborationCard: {}",
                    newCorrelations.size(), newCollaborationCard);

            collaborationCardRepository.saveNewCollaborationCard(newCollaborationCard);
            log.info("New CollaborationCard successfully created for Card.");
            log.info("Publishing {} Commands to clone Card into Decks...",
                    newCorrelations.size());
            newCorrelations.forEach(x -> kafkaProducer.send(
                    new CloneCard(getTraceIdOrEmptyString(), x.correlationId(),
                            new CloneCardDto(x.rootCardId(), x.deckId()))));
        } else {
            log.debug("No Collaboration was found. No CorrelationCard was created.");
        }
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
