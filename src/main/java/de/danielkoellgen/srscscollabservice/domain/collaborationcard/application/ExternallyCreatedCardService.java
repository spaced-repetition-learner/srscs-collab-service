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

        log.trace("Creating new collaboration-card...");
        Optional<Collaboration> collaboration = collaborationRepository
                .findCollaborationByDeckId(deckId);
        if (collaboration.isEmpty()) {
            log.trace("No matching collaboration found.");
            return;
        }
        log.debug("Collaboration fetched by deck-id: {}", collaboration.get());

        Pair<CollaborationCard, List<Correlation>> response = CollaborationCard.createNew(
                collaboration.get(), cardId, userId, deckId);
        CollaborationCard newCollaborationCard = response.getValue0();
        List<Correlation> newCorrelations = response.getValue1();
        log.debug("New collaboration-card created with {} unpublished correlations. {}",
                newCorrelations.size(), newCollaborationCard);

        newCorrelations.forEach(x -> collaborationCardRepository.setLock(
                newCollaborationCard.getCollaborationCardId(), x.userId()));

        collaborationCardRepository.saveNewCollaborationCard(newCollaborationCard);

        newCorrelations.forEach(x -> kafkaProducer.send(
                new CloneCard(
                        getTraceIdOrEmptyString(),
                        x.correlationId(),
                        new CloneCardDto(x.rootCardId(), x.deckId()))));
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
