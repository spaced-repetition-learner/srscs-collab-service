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
import de.danielkoellgen.srscscollabservice.events.producer.collaboration.DeckAdded;
import de.danielkoellgen.srscscollabservice.events.producer.collaboration.dto.DeckAddedDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
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

    @Autowired
    private Tracer tracer;

    private final Logger logger = LoggerFactory.getLogger(CollaborationService.class);

    @Autowired
    public CollaborationService(CollaborationRepository collaborationRepository, UserRepository userRepository,
            KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public Collaboration startNewCollaboration(@NotNull DeckName collaborationName,
            @NotNull List<Username> usernames) throws NoSuchElementException {
        logger.trace("Starting a new Collaboration...");
        logger.trace("Fetching invited Users...");
        List<User> invitedUsers = usernames.stream()
                .map(username -> userRepository.findUserByUsername(username).get())
                .toList();
        logger.debug("{} / {} Users fetched.", usernames.size(), invitedUsers.size());
        logger.debug("{}", invitedUsers);

        Collaboration collaboration = Collaboration.startNewCollaboration(collaborationName, invitedUsers);
        logger.debug("{}", collaboration);

        collaborationRepository.saveNewCollaboration(collaboration);
        logger.trace("New Collaboration '{}' saved.", collaboration.getName().getName());
        logger.info("New Collaboration '{}' started with {} invited User(s).", collaborationName.getName(), invitedUsers.size());
        return collaboration;
    }

    public Participant inviteUserToCollaboration(@NotNull UUID collaborationId,
            @NotNull Username username) throws NoSuchElementException, CollaborationStateException {
        logger.trace("Inviting User '{}' to join the Collaboration...", username.getUsername());
        logger.trace("Fetching User by username {}...", username.getUsername());
        User user = userRepository.findUserByUsername(username).get();
        logger.debug("{}", user);

        logger.trace("Fetching Collaboration by id {}...", collaborationId);
        Collaboration collaboration = collaborationRepository.findCollaborationById(collaborationId).get();

        Participant newParticipant = collaboration.inviteParticipant(user);
        logger.debug("User invited as new Participant.");
        logger.debug("{}", newParticipant);

        collaborationRepository.saveNewParticipant(collaboration, newParticipant);
        logger.trace("Participant saved as 'saveNewParticipant'.");
        logger.info("User '{}' invited to participante in Collaboration '{}'.", username.getUsername(), collaboration.getName().getName());
        return newParticipant;
    }

    public void acceptParticipation(@NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        logger.trace("Accepting Participation...");
        logger.trace("Fetching Collaboration by id {}...", collaborationId);
        Collaboration collaboration = collaborationRepository.findCollaborationById(collaborationId).get();
        logger.debug("{}", collaboration);

        Participant updatedParticipant = collaboration.acceptInvitation(userId);
        logger.debug("Participation accepted.");
        logger.debug("{}", updatedParticipant);

        collaborationRepository.updateParticipant(collaboration, updatedParticipant);
        logger.trace("Saved Participant as 'updateAcceptedParticipant'.");

        logger.info("User '{}' accepted to participate in '{}'.",
                updatedParticipant.getUser().getUsername().getUsername(), collaboration.getName().getName()
        );
        logger.info("Sending CreateDeck Command to create a new Deck.");
        kafkaProducer.send(new CreateDeck(
                getTraceIdOrEmptyString(), updatedParticipant.getDeckCorrelationId(), new CreateDeckDto(
                        userId, collaboration.getName().getName())
                )
        );
    }

    public void endParticipation(@NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        logger.trace("Ending Participation...");
        logger.trace("Fetching Collaboration by id {}.", collaborationId);
        Collaboration collaboration = collaborationRepository.findCollaborationById(collaborationId).get();

        Participant updatedParticipant = collaboration.endParticipation(userId);
        logger.debug("Participation ended.");
        logger.debug("{}", updatedParticipant);

        collaborationRepository
                .updateParticipant(collaboration, updatedParticipant);
        logger.trace("Saved Participant as 'updateTerminatedParticipant'.");
        logger.info("User '{}' ended his participation in '{}'.",
                updatedParticipant.getUser().getUsername().getUsername(), collaboration.getName().getName()
        );
    }

    public void addCorrespondingDeckToParticipant(@NotNull UUID correlationId, @NotNull UUID deckId, @NotNull UUID userId) {
        logger.trace("Adding corresponding Deck to Participant...");
        logger.trace("Fetching Collaboration by correlation-id {}.", correlationId);
        Optional<Collaboration> optCollaboration = collaborationRepository.findCollaborationByDeckCorrelationId(correlationId);

        if (optCollaboration.isPresent()){
            logger.debug("Collaboration found.");
            Collaboration collaboration = optCollaboration.get();
            logger.debug("{}", collaboration);

            Participant updatedParticipant = collaboration.setDeck(userId, correlationId, new Deck(deckId, null));
            logger.debug("Deck added to Participant.");
            logger.debug("{}", updatedParticipant);
            collaborationRepository.updateParticipant(
                    collaboration, updatedParticipant);
            logger.trace("Saved Participant as 'updateDeckAddedParticipant'.");
            logger.info("Deck added to Participant.");

            kafkaProducer.send(new DeckAdded(getTraceIdOrEmptyString(),
                    new DeckAddedDto(optCollaboration.get().getCollaborationId(), userId, deckId))
            );

        } else {
            logger.trace("Deck does not belong to any active Participant.");
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
