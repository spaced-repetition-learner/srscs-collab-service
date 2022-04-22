package de.danielkoellgen.srscscollabservice.domain.collaboration.application;

import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.CreateDeck;
import de.danielkoellgen.srscscollabservice.commands.producer.deckcards.dto.CreateDeckDto;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.CollaborationStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscscollabservice.events.producer.KafkaProducer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;

    private final KafkaProducer kafkaProducer;

    private final Logger logger = LoggerFactory.getLogger(CollaborationService.class);

    @Autowired
    public CollaborationService(CollaborationRepository collaborationRepository, UserRepository userRepository,
            KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public Collaboration startNewCollaboration(@NotNull UUID transactionId, @NotNull DeckName collaborationName,
            @NotNull List<Username> usernames) throws NoSuchElementException {
        List<User> invitedUsers = usernames.stream()
                .map(username -> userRepository.findUserByUsername(username).get())
                .toList();
        Collaboration collaboration = Collaboration.startNewCollaboration(transactionId, collaborationName, invitedUsers);
        collaborationRepository.saveNewCollaboration(collaboration);
        logger.info("New Collaboration '{}' started with {} invited User(s). [tid={}]",
                collaborationName.getName(),
                invitedUsers.size(),
                transactionId
        );
        logger.trace("New Collaboration started: [tid={}, {}]",
                transactionId,
                collaboration
        );
        return collaboration;
    }

    public Participant inviteUserToCollaboration(@NotNull UUID transactionId, @NotNull UUID collaborationId,
            @NotNull Username username) throws NoSuchElementException, CollaborationStateException {
        User user = userRepository
                .findUserByUsername(username).get();
        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).get();
        Participant newParticipant = collaboration
                .inviteParticipant(transactionId, user);
        collaborationRepository
                .saveNewParticipant(collaboration, newParticipant);
        logger.info("Invited '{}' to participate in '{}'. [tid={}]",
                user.getUsername().getUsername(),
                collaboration.getName().getName(),
                transactionId
        );
        logger.trace("'{}' invited to collaborate: [tid={}, {}]",
                user.getUsername().getUsername(),
                transactionId,
                collaboration
        );
        return newParticipant;
    }

    public void acceptParticipation(@NotNull UUID transactionId, @NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        Collaboration collaboration = collaborationRepository.findCollaborationById(collaborationId).get();
        Participant updatedParticipant = collaboration.acceptInvitation(transactionId, userId);
        collaborationRepository.updateParticipant(collaboration, updatedParticipant);
        logger.info("'{}' accepted to participate in '{}'. [tid={}]",
                updatedParticipant.getUser().getUsername().getUsername(),
                collaboration.getName().getName(),
                transactionId
        );
        kafkaProducer.send(new CreateDeck(
                transactionId, transactionId, new CreateDeckDto(userId, collaboration.getName().getName()))
        );
    }

    public void endParticipation(@NotNull UUID transactionId, @NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).get();
        Participant updatedParticipant = collaboration
                .endParticipation(transactionId, userId);
        collaborationRepository
                .updateParticipant(collaboration, updatedParticipant);
        logger.info("'{}' ended his participation in '{}'. [tid={}]",
                updatedParticipant.getUser().getUsername().getUsername(),
                collaboration.getName().getName(),
                transactionId
        );
        //TODO: CMD?
    }

    public void addCorrespondingDeckToParticipant(@NotNull UUID transactionId, @NotNull UUID correlationId,
            @NotNull UUID deckId, @NotNull UUID userId) {
        Optional<Collaboration> optCcollaboration = collaborationRepository.findCollaborationByDeckCorrelationId(correlationId);
        if (optCcollaboration.isPresent()){
            Collaboration collaboration = optCcollaboration.get();
            Participant updatedParticipant = collaboration.setDeck(
                    userId, correlationId, new Deck(deckId, null)
            );
            collaborationRepository.updateParticipant(
                    collaboration, updatedParticipant
            );
        }
    }
}
