package de.danielkoellgen.srscscollabservice.domain.collaborationcard.application;

import de.danielkoellgen.srscscollabservice.domain.card.domain.Card;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardVersion;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.CollaborationCardRepository;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AppendingCreatedCardService {

    private final CollaborationCardRepository collaborationCardRepository;

    @Autowired
    public AppendingCreatedCardService(CollaborationCardRepository collaborationCardRepository) {
        this.collaborationCardRepository = collaborationCardRepository;
    }

    public void appendCreatedCard(UUID transactionId, UUID userId, UUID deckId, Card card) {
        Optional<CollaborationCard> collaborationCard = collaborationCardRepository
                .findCollaborationCardByTransactionId(transactionId);

        if (collaborationCard.isPresent()) {
            appendCardToCardVersion(collaborationCard.get(), transactionId, card);
            return;
        }

        Optional<Collaboration> collaboration = Optional.empty(); //TODO REPOSITORY COLLABORATION FETCH BY DECK

        if (collaboration.isPresent()) {
            createNewCollaborationCard(collaboration.get(), transactionId, card);
        }
    }

    private void appendCardToCardVersion(CollaborationCard collaborationCard, UUID transactionId, Card card) {
        Pair<CardVersion, Correlation> result = collaborationCard.appendCard(transactionId, card);
        collaborationCardRepository.saveAppendedCard(collaborationCard, result.getValue0(), result.getValue1());
    }

    private void createNewCollaborationCard(Collaboration collaboration, UUID transactionId, Card card) {
        CollaborationCard collaborationCard = CollaborationCard.createNewCollaborationCard(
                collaboration, transactionId, card);
        collaborationCardRepository.saveNewCollaborationCard(collaborationCard);
    }
}
