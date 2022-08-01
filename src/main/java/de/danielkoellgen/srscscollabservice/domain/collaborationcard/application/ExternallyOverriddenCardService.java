package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import com.datastax.oss.driver.shaded.guava.common.collect.Streams;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.CloneCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.OverrideCard;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.CloneCardDto;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.OverrideCardDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.CollaborationCardRepository;
import de.danielkoellgen.srscscollabservice.events.producer.KafkaProducer;
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

    private final Logger logger = LoggerFactory.getLogger(ExternallyOverriddenCardService.class);

    @Autowired
    public ExternallyOverriddenCardService(CollaborationRepository collaborationRepository,
            CollaborationCardRepository collaborationCardRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void updateToNewCardVersion(@NotNull UUID parentCardId, @NotNull UUID newCardId,
            @NotNull UUID deckId, @NotNull UUID userId) {
        logger.trace("Updating CollaborationCard to a new CardVersion...");
        logger.trace("Fetching CollaborationCard joined on root-card-id by parent-card-id {}.",
                parentCardId);
        Optional<CollaborationCard> collaborationCardJoinedOnRootCardIdByParentCardId =
                collaborationCardRepository
                        .findCollaborationCardWithCorrelation_byCardIdOnRootCardId(parentCardId);

        if (collaborationCardJoinedOnRootCardIdByParentCardId.isPresent()) {
            CollaborationCard fullCollaborationCardOnRootCardId =
                    collaborationCardJoinedOnRootCardIdByParentCardId.get();
            logger.debug("Matching CollaborationCard fetched.");
            logger.debug("{}", fullCollaborationCardOnRootCardId);

            logger.trace("Fetching Collaboration by id {}...", fullCollaborationCardOnRootCardId
                    .getCollaborationId());
            Collaboration collaboration = collaborationRepository
                    .findCollaborationById(fullCollaborationCardOnRootCardId.getCollaborationId()).get();
            logger.debug("{}", collaboration);

            Triplet<List<Correlation>, List<Correlation>, List<Correlation>> response =
                    fullCollaborationCardOnRootCardId.addNewCardVersion(
                            collaboration, parentCardId, newCardId, userId, deckId);
            List<Correlation> overrideCorrelations = response.getValue0();
            List<Correlation> cloneCorrelations = response.getValue1();
            List<Correlation> newCorrelations = response.getValue2();
            logger.trace("New CardVersion added to CollaborationCard.");
            logger.debug("{}", fullCollaborationCardOnRootCardId);

            //TODO: possible miss of new correlation while saving
            collaborationCardRepository.saveNewCardVersion(fullCollaborationCardOnRootCardId,
                    newCorrelations);
            logger.trace("Updated CollaborationCard saved.");
            logger.info("New CardVersion added to CollaborationCard for Card.");
            logger.info("Publishing {} Commands to clone Card...", cloneCorrelations.size());

            cloneCorrelations.forEach(x -> kafkaProducer.send(
                    new CloneCard(getTraceIdOrEmptyString(), x.correlationId(),
                            new CloneCardDto(x.rootCardId(),x.deckId()))));
            logger.info("Publishing {} Commands to override with Card...",
                    overrideCorrelations.size());
            overrideCorrelations.forEach(x -> kafkaProducer.send(
                    new OverrideCard(getTraceIdOrEmptyString(), x.correlationId(),
                            new OverrideCardDto(x.deckId(), x.parentCardId(), x.rootCardId()))));
            return;
        }

        logger.debug("No CollaborationCard was found.");
        logger.trace("No new CardVersion was created.");
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
