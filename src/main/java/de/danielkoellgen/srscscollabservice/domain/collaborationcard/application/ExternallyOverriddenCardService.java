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
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ExternallyOverriddenCardService(CollaborationRepository collaborationRepository,
            CollaborationCardRepository collaborationCardRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void processCard(@NotNull UUID transactionId, @NotNull UUID correlationId, @NotNull UUID parentCardId,
            @NotNull UUID newCardId, @NotNull UUID deckId, @NotNull UUID userId) {
        Optional<CollaborationCard> collaborationCardByCorrelation = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCorrelationId(correlationId);
        if (collaborationCardByCorrelation.isPresent()) {
            CollaborationCard partialCollaboration = collaborationCardByCorrelation.get();
            Correlation updatedCorrelation = partialCollaboration.addCard(correlationId, newCardId);
            collaborationCardRepository.saveUpdatedCorrelation(
                    partialCollaboration, updatedCorrelation
            );
            return;
        }
        Optional<CollaborationCard> collaborationCardByParentCardId = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCardIdOnRootCardId(parentCardId);
        if (collaborationCardByParentCardId.isPresent()) {
            CollaborationCard fullCollaborationCardOnRootCard = collaborationCardByParentCardId.get();
            UUID rootCardId = fullCollaborationCardOnRootCard.getCorrelations().get(0).rootCardId();
            Collaboration collaboration = collaborationRepository
                    .findCollaborationById(fullCollaborationCardOnRootCard.getCollaborationId()).get();
            Pair<List<Correlation>, List<Correlation>> response =  fullCollaborationCardOnRootCard.addNewCardVersion(
                    collaboration, parentCardId, newCardId, userId, deckId);
            List<Correlation> overrideCorrelations = response.getValue0();
            List<Correlation> cloneCorrelations = response.getValue1();
            collaborationCardRepository.saveNewCardVersion(
                    fullCollaborationCardOnRootCard,
                    Streams.concat(overrideCorrelations.stream(), cloneCorrelations.stream()).toList()
            );
            cloneCorrelations.forEach(x ->
                    kafkaProducer.send(
                            new CloneCard(transactionId, x.correlationId(), new CloneCardDto(x.rootCardId(),x.deckId()))
                    ));
            overrideCorrelations.forEach(x ->
                    kafkaProducer.send(
                            new OverrideCard(transactionId, x.correlationId(), new OverrideCardDto(
                                    x.deckId(),
                                    x.parentCardId(),
                                    x.rootCardId())) //TODO
                    ));
        }

        // 1. check card has matching correlation
            // yes: update correlation

        // 2. check parentCard has matching correlation
            // yes: fetch collaboration
            //      add new card-version

        // END
    }
}
