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

import java.util.*;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    private Tracer tracer;

    private final Logger log = LoggerFactory.getLogger(CollaborationService.class);

    @Autowired
    public CollaborationService(CollaborationRepository collaborationRepository,
            UserRepository userRepository, KafkaProducer kafkaProducer) {
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public Collaboration startNewCollaboration(@NotNull DeckName collaborationName,
            @NotNull List<Username> usernames) throws NoSuchElementException {
        log.trace("Starting a new Collaboration with {} Users...", usernames.size());
        log.trace("Fetching each User by id...");
        List<User> invitedUsers = usernames
                .stream()
                .map(username -> userRepository.findUserByUsername(username).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        log.debug("{} / {} Users fetched. {}", invitedUsers.size(), usernames.size(), invitedUsers);
        if (invitedUsers.size() != usernames.size()) {
            log.info("New Collaboration cancelled. {} User(s) not found.",
                    usernames.size()-invitedUsers.size());
            throw new NoSuchElementException("User not found.");
        }

        Collaboration collaboration = Collaboration.startNewCollaboration(collaborationName,
                invitedUsers);
        collaborationRepository.saveNewCollaboration(collaboration);
        log.info("New Collaboration '{}' successfully started with {} invited User(s).",
                collaborationName.getName(), invitedUsers.size());
        log.debug("New Collaboration: {}", collaboration);
        return collaboration;
    }

    public Participant inviteUserToCollaboration(@NotNull UUID collaborationId,
            @NotNull Username username) throws NoSuchElementException, CollaborationStateException {
        log.trace("Inviting '{}' to join to collaborate...", username.getUsername());

        log.trace("Fetching User by username '{}'...", username);
        User user = userRepository.findUserByUsername(username).orElseThrow();
        log.debug("Fetched User: {}", user);
        log.trace("Fetching Collaboration by id '{}'...", collaborationId);
        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Fetched Collaboration: {}", collaboration);

        Participant newParticipant = collaboration.inviteParticipant(user);
        collaborationRepository.saveNewParticipant(collaboration, newParticipant);
        log.info("User '{}' successfully invited to participate in Collaboration '{}'.",
                username, collaboration.getName());
        log.debug("New Participant: {}", newParticipant);
        return newParticipant;
    }

    public void acceptParticipation(@NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        log.trace("Accepting Participation in Collaboration '{}' for User '{}'...",
                collaborationId, userId);

        log.trace("Fetching Collaboration by id '{}'...", collaborationId);
        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Fetched Collaboration: {}", collaboration);

        Participant updatedParticipant = collaboration.acceptInvitation(userId);
        collaborationRepository.updateAcceptedParticipant(collaboration, updatedParticipant);
        log.info("User '{}' successfully accepted to participate in '{}'.",
                updatedParticipant.getUser().getUsername(), collaboration.getName());
        log.debug("Updated Participant: {}", updatedParticipant);

        log.info("Sending 'CreateDeck-Command' for Participant.");
        kafkaProducer.send(new CreateDeck(
                getTraceIdOrEmptyString(), updatedParticipant.getDeckCorrelationId(),
                new CreateDeckDto(userId, collaboration.getName().getName())));
    }

    public void endParticipation(@NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        log.trace("Ending Participation in Collaboration '{}' for User '{}'...",
                collaborationId, userId);

        log.trace("Fetching Collaboration by id '{}'...", collaborationId);
        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Fetched Collaboration: {}", collaboration);

        Participant updatedParticipant = collaboration.endParticipation(userId);
        collaborationRepository.updateTerminatedParticipant(collaboration, updatedParticipant);
        log.info("User '{}' successfully ended his participation in '{}'.",
                updatedParticipant.getUser().getUsername(), collaboration.getName());
        log.debug("Updated Participant: {}", updatedParticipant);
    }

    public void endParticipationViaDeck(@NotNull UUID deckId, @NotNull UUID userId) throws ParticipantStateException {
        log.trace("Ending Participation via Deck '{}' and for User '{}'...", deckId, userId);

        log.trace("Fetching Collaboration by deck-id '{}'...", deckId);
        Optional<Collaboration> optCollaboration = collaborationRepository
                .findCollaborationByDeckId(deckId);
        if (optCollaboration.isEmpty()) {
            log.info("No matching Collaboration found. Deck does not belong to any Participant.");
            return;
        }
        UUID collaborationId = optCollaboration.get().getCollaborationId();
        log.trace("Fetching Collaboration by collaboration-id '{}' from previous Collaboration...",
                collaborationId);
        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Fetched Collaboration: {}", collaboration);
        Participant updatedParticipant = collaboration.endParticipation(userId);
        collaborationRepository.updateTerminatedParticipant(collaboration, updatedParticipant);
        log.info("Participation in Collaboration successfully ended.");
        log.debug("Updated Participant: {}", updatedParticipant);
    }

    public void addCorrespondingDeckToParticipant(@NotNull UUID correlationId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        log.trace("Adding corresponding Deck '{}' to Participant by correlation-id '{}'...",
                deckId, correlationId);

        log.trace("Fetching Collaboration by deck-correlation-id '{}'...", correlationId);
        Optional<Collaboration> optCollaboration = collaborationRepository
                .findCollaborationByDeckCorrelationId(correlationId);
        if (optCollaboration.isEmpty()) {
            log.info("No matching Collaboration found. Deck does not belong to any Participant.");
            return;
        }
        Collaboration collaboration = optCollaboration.get();
        log.debug("Fetched Collaboration: {}", collaboration);

        Participant updatedParticipant = collaboration.setDeck(userId, correlationId,
                new Deck(deckId, null));
        collaborationRepository.updateDeckAddedParticipant(collaboration, updatedParticipant);
        log.info("Deck successfully added to Participant.");
        log.debug("Updated Participant: {}", updatedParticipant);

        kafkaProducer.send(new DeckAdded(getTraceIdOrEmptyString(),
                new DeckAddedDto(optCollaboration.get().getCollaborationId(), userId, deckId)));
    }

    private String getTraceIdOrEmptyString() {
        try {
            return tracer.currentSpan().context().traceId();
        } catch (Exception e) {
            return "";
        }
    }
}
