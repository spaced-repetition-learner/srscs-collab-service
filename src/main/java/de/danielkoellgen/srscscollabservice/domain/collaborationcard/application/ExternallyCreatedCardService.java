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
import org.springframework.beans.factory.annotation.Autowired;
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
    public ExternallyCreatedCardService(CollaborationRepository collaborationRepository,
            CollaborationCardRepository collaborationCardRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.collaborationCardRepository = collaborationCardRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void processCard(@NotNull UUID transactionId, @NotNull UUID correlationId, @NotNull UUID cardId,
            @NotNull UUID deckId, @NotNull UUID userId) {
        Optional<CollaborationCard> collaborationCardByCorrelation = collaborationCardRepository
                .findCollaborationCardWithCorrelation_byCorrelationId(correlationId);
        if (collaborationCardByCorrelation.isPresent()) {
            CollaborationCard partialCollaboration = collaborationCardByCorrelation.get();
            Correlation updatedCorrelation = partialCollaboration.addCard(correlationId, cardId);
            collaborationCardRepository.saveUpdatedCorrelation(
                    partialCollaboration, updatedCorrelation
            );
            return;
        }
        Optional<Collaboration> collaborationByDeckId = collaborationRepository.findCollaborationByDeckId(deckId);
        if (collaborationByDeckId.isPresent()) {
            Collaboration fullCollaboration = collaborationByDeckId.get();
            Pair<CollaborationCard, List<Correlation>> response = CollaborationCard
                    .createNew(fullCollaboration, cardId, userId, deckId);
            CollaborationCard newCollaborationCard = response.getValue0();
            List<Correlation> newCorrelations = response.getValue1();
            collaborationCardRepository.saveNewCollaborationCard(
                    newCollaborationCard
            );
            newCorrelations.forEach(x ->
                    kafkaProducer.send(
                            new CloneCard(transactionId, x.correlationId(), new CloneCardDto(x.rootCardId(), x.deckId()))
                    ));
        }

        // 1. check card has matching correlation
        //      yes: update correlation
        //           return;

        // 2. check card belongs to collaboration via deck
        //      yes: create new collaboration-card
        //           publish commands

        // END
    }
}
