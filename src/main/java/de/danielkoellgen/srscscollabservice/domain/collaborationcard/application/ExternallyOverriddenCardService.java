package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.CloneCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.OverrideCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.CloneCardDto;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.OverrideCardDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.CollaborationCardRepository;
import de.danielkoellgen.srscscollabservice.events.producer.KafkaProducer;
import org.javatuples.Pair;
import org.javatuples.Triplet;
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

        log.trace("Updating collaboration-card with new card-version...");
        Optional<CollaborationCard> collaborationCard = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCardIdOnRootCardId(parentCardId);
        if (collaborationCard.isEmpty()) {
            log.trace("No matching collaboration-card found.");
            return;
        }
        Optional<Collaboration> collaboration = collaborationRepository.findCollaborationById(
                collaborationCard.get().getCollaborationId());
        if (collaboration.isEmpty()) {
            log.error("No matching collaboration to collaboration-card found.");
            throw new RuntimeException();
        }
        log.debug("CollaborationCard: {}", collaborationCard.get());

        List<Participant> acquiredLocks = collaboration.get().getParticipants().values()
                .stream()
                .filter(Participant::isActive)
                .filter(x -> x.getUserId() != cardOwnerUserId)
                .filter(x -> !collaborationCardRepository.checkLock(
                        collaborationCard.get().getCollaborationCardId(), x.getUserId()))
                .toList();
        log.debug("{} Acquired locks: {}", acquiredLocks.size(), acquiredLocks);

        Pair<List<Correlation>, List<Correlation>> response = collaborationCard.get()
                .addNewCardVersion(newCardId, parentCardId, acquiredLocks, deckId, cardOwnerUserId);
        List<Correlation> unpublishedCorrelations = response.getValue0();
        List<Correlation> allNewCorrelations = response.getValue1();
        log.debug("{} new unpublished correlations. {}", unpublishedCorrelations.size(),
                unpublishedCorrelations);

        unpublishedCorrelations.forEach(x -> collaborationCardRepository
                .setLock(collaborationCard.get().getCollaborationCardId(), x.userId()));

        collaborationCardRepository.saveNewCardVersion(collaborationCard.get(),
                    allNewCorrelations);
        log.debug("Updated CollaborationCard: {}", collaborationCard.get());

        unpublishedCorrelations.forEach(x -> kafkaProducer.send(
                    new OverrideCard(getTraceIdOrEmptyString(), x.correlationId(),
                            new OverrideCardDto(x.deckId(), x.parentCardId(), x.rootCardId()))));


//        log.trace("Updating CollaborationCard to a new CardVersion...");
//        log.trace("Fetching CollaborationCard joined on root-card-id by parent-card-id {}.",
//                parentCardId);
//        Optional<CollaborationCard> collaborationCardJoinedOnRootCardIdByParentCardId =
//                collaborationCardRepository
//                        .findCollaborationCardWithCorrelation_byCardIdOnRootCardId(parentCardId);
//
//        if (collaborationCardJoinedOnRootCardIdByParentCardId.isPresent()) {
//            CollaborationCard fullCollaborationCardOnRootCardId =
//                    collaborationCardJoinedOnRootCardIdByParentCardId.get();
//            log.debug("Fetched CollaborationCard: {}", fullCollaborationCardOnRootCardId);
//
//            log.trace("Fetching Collaboration by id {}...", fullCollaborationCardOnRootCardId
//                    .getCollaborationId());
//            Collaboration collaboration = collaborationRepository
//                    .findCollaborationById(fullCollaborationCardOnRootCardId.getCollaborationId())
//                    .orElseThrow();
//            log.debug("{}", collaboration);
//
//            Triplet<List<Correlation>, List<Correlation>, List<Correlation>> response =
//                    fullCollaborationCardOnRootCardId.addNewCardVersion(
//                            collaboration, parentCardId, newCardId, userId, deckId);
//            List<Correlation> overrideCorrelations = response.getValue0();
//            List<Correlation> cloneCorrelations = response.getValue1();
//            List<Correlation> newCorrelations = response.getValue2();
//            log.trace("New CardVersion added to CollaborationCard.");
//            log.debug("{}", fullCollaborationCardOnRootCardId);
//
//            //TODO: possible miss of new correlation while saving
//            collaborationCardRepository.saveNewCardVersion(fullCollaborationCardOnRootCardId,
//                    newCorrelations);
//            log.trace("Updated CollaborationCard saved.");
//            log.info("New CardVersion added to CollaborationCard for Card.");
//            log.info("Publishing {} Commands to clone Card...", cloneCorrelations.size());
//
//            cloneCorrelations.forEach(x -> kafkaProducer.send(
//                    new CloneCard(getTraceIdOrEmptyString(), x.correlationId(),
//                            new CloneCardDto(x.rootCardId(),x.deckId()))));
//            log.info("Publishing {} Commands to override with Card...",
//                    overrideCorrelations.size());
//            overrideCorrelations.forEach(x -> kafkaProducer.send(
//                    new OverrideCard(getTraceIdOrEmptyString(), x.correlationId(),
//                            new OverrideCardDto(x.deckId(), x.parentCardId(), x.rootCardId()))));
//        } else {
//            log.debug("No CollaborationCard was found.");
//            log.trace("No new CardVersion was created.");
//        }
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
