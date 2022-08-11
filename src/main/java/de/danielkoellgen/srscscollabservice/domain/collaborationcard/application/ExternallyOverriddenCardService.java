package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.OverrideCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.OverrideCardDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
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
public class ExternallyOverriddenCardService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationCardRepository collaborationCardRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    private Tracer tracer;

    private final Logger log = LoggerFactory.getLogger(ExternallyOverriddenCardService.class);

    @Autowired
    public ExternallyOverriddenCardService(CollaborationRepository collaborationRepository,
            CollaborationCardRepository collaborationCardRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void updateToNewCardVersion(@NotNull UUID parentCardId, @NotNull UUID newCardId,
            @NotNull UUID deckId, @NotNull UUID cardOwnerUserId) {
        log.trace("Fetching CollaborationCard by card-id on root-card-id with parent-card-id '{}'...",
                parentCardId);
        Optional<CollaborationCard> collaborationCard = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCardIdOnRootCardId(parentCardId);
        if (collaborationCard.isEmpty()) {
            log.info("No matching CollaborationCard found. Card does not belong to any.");
            return;
        }
        UUID collaborationId = collaborationCard.get().getCollaborationId();
        log.trace("Fetching Collaboration by id '{}'...", collaborationId);
        Optional<Collaboration> collaboration = collaborationRepository.findCollaborationById(
                collaborationId);
        if (collaboration.isEmpty()) {
            log.error("No matching Collaboration found even though it should.");
            throw new RuntimeException("No CollaborationCard found even though a CollaborationCard" +
                    " exists.");
        }
        log.debug("Fetched CollaborationCard: {}", collaborationCard.get());

        log.trace("Checking available event-locks for active participants...");
        List<Participant> acquiredLocks = collaboration.get().getParticipants().values()
                .stream()
                .filter(Participant::isActive)
                .filter(x -> x.getUserId() != cardOwnerUserId)
                .filter(x -> !collaborationCardRepository.checkLock(
                        collaborationCard.get().getCollaborationCardId(), x.getUserId()))
                .toList();
        log.debug("{} event-locks available. {}", acquiredLocks.size(),
                acquiredLocks.stream().map(Participant::getUserId).toList());

        Pair<List<Correlation>, List<Correlation>> response = collaborationCard.get()
                .addNewCardVersion(newCardId, parentCardId, acquiredLocks, deckId, cardOwnerUserId);
        List<Correlation> unpublishedCorrelations = response.getValue0();
        List<Correlation> allNewCorrelations = response.getValue1();

        unpublishedCorrelations.forEach(x -> collaborationCardRepository
                .setLock(collaborationCard.get().getCollaborationCardId(), x.userId()));
        log.trace("{} event-locks acquired.", unpublishedCorrelations.size());
        collaborationCardRepository.saveNewCardVersion(collaborationCard.get(),
                    allNewCorrelations);
        log.info("CollaborationCard updated to a newer version.");
        log.debug("Updated CollaborationCard: {}", collaborationCard.get());

        unpublishedCorrelations.forEach(x -> kafkaProducer.send(
                    new OverrideCard(getTraceIdOrEmptyString(), x.correlationId(),
                            new OverrideCardDto(x.deckId(), x.parentCardId(), x.rootCardId()))));
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
