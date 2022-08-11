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
        log.trace("Fetch Collaboration by deck-id '{}'...", deckId);
        Optional<Collaboration> collaboration = collaborationRepository
                .findCollaborationByDeckId(deckId);
        if (collaboration.isEmpty()) {
            log.info("No matching Collaboration found. Deck and Card don't belong to any.");
            return;
        }
        log.debug("Fetched Collaboration: {}", collaboration.get());

        Pair<CollaborationCard, List<Correlation>> response = CollaborationCard.createNew(
                collaboration.get(), cardId, userId, deckId);
        CollaborationCard newCollaborationCard = response.getValue0();
        List<Correlation> newCorrelations = response.getValue1();

        newCorrelations.forEach(x -> collaborationCardRepository.setLock(
                newCollaborationCard.getCollaborationCardId(), x.userId()));
        log.trace("{} event-locks acquired.", newCorrelations.size());
        collaborationCardRepository.saveNewCollaborationCard(newCollaborationCard);
        log.info("New CollaborationCard created with {} pending Correlations.", newCorrelations.size());
        log.debug("New CollaborationCard: {}", newCollaborationCard);

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
