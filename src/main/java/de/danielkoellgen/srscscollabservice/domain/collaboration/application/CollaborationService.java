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
        log.trace("Starting a new Collaboration...");
        List<User> invitedUsers = usernames
                .stream()
                .map(username -> userRepository.findUserByUsername(username).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        log.debug("{} / {} Users fetched. {}", invitedUsers.size(), usernames.size(), invitedUsers);

        if (invitedUsers.size() != usernames.size()) {
            throw new NoSuchElementException("User not found.");
        }

        Collaboration collaboration = Collaboration.startNewCollaboration(collaborationName,
                invitedUsers);
        log.debug("New Collaboration: {}", collaboration);

        collaborationRepository.saveNewCollaboration(collaboration);
        log.info("New Collaboration '{}' successfully started with {} invited User(s).",
                collaborationName.getName(), invitedUsers.size());
        return collaboration;
    }

    public Participant inviteUserToCollaboration(@NotNull UUID collaborationId,
            @NotNull Username username) throws NoSuchElementException, CollaborationStateException {
        log.trace("Inviting '{}' to join the Collaboration...", username.getUsername());

        User user = userRepository.findUserByUsername(username).orElseThrow();
        log.debug("User fetched by username. {}", user);

        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Collaboration fetched by id: {}", collaboration);

        Participant newParticipant = collaboration.inviteParticipant(user);
        collaborationRepository.saveNewParticipant(collaboration, newParticipant);
        log.info("User '{}' successfully invited to participate in Collaboration '{}'.",
                username.getUsername(), collaboration.getName().getName());
        return newParticipant;
    }

    public void acceptParticipation(@NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        log.trace("Accepting Participation...");

        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Collaboration fetched by id: {}", collaboration);

        Participant updatedParticipant = collaboration.acceptInvitation(userId);
        log.debug("Participation accepted. Updated Participant: {}", updatedParticipant);

        collaborationRepository.updateAcceptedParticipant(collaboration, updatedParticipant);
        log.info("User '{}' successfully accepted to participate in '{}'.",
                updatedParticipant.getUser().getUsername().getUsername(),
                collaboration.getName().getName());

        log.info("Sending 'CreateDeck-Command' for Participant.");
        kafkaProducer.send(new CreateDeck(
                getTraceIdOrEmptyString(), updatedParticipant.getDeckCorrelationId(),
                new CreateDeckDto(userId, collaboration.getName().getName())));
    }

    public void endParticipation(@NotNull UUID collaborationId, @NotNull UUID userId) throws
            NoSuchElementException, ParticipantStateException {
        log.trace("Ending Participation...");

        Collaboration collaboration = collaborationRepository
                .findCollaborationById(collaborationId).orElseThrow();
        log.debug("Collaboration fetched by id: {}", collaboration);

        Participant updatedParticipant = collaboration.endParticipation(userId);
        log.debug("Participation ended. Updated Participant: {}", updatedParticipant);

        collaborationRepository.updateTerminatedParticipant(collaboration, updatedParticipant);
        log.info("User '{}' successfully ended his participation in '{}'.",
                updatedParticipant.getUser().getUsername().getUsername(),
                collaboration.getName().getName());
    }

    public void endParticipationViaDeck(@NotNull UUID deckId, @NotNull UUID userId) throws ParticipantStateException {
        Optional<Collaboration> optCollaboration = collaborationRepository.findCollaborationByDeckId(deckId);
        if (optCollaboration.isEmpty()) {
            // TODO
            return;
        }
        Collaboration collaboration = collaborationRepository.findCollaborationById(optCollaboration.get().getCollaborationId()).orElseThrow();
        Participant updatedParticipant = collaboration.endParticipation(userId);
        collaborationRepository.updateTerminatedParticipant(collaboration, updatedParticipant);
    }

    public void addCorrespondingDeckToParticipant(@NotNull UUID correlationId, @NotNull UUID deckId,
            @NotNull UUID userId) {
        log.trace("Adding corresponding Deck to Participant...");

        Optional<Collaboration> optCollaboration = collaborationRepository
                .findCollaborationByDeckCorrelationId(correlationId);
        if (optCollaboration.isPresent()){
            Collaboration collaboration = optCollaboration.get();
            log.debug("Collaboration fetched by DeckCorrelationId: {}", collaboration);

            Participant updatedParticipant = collaboration.setDeck(userId, correlationId,
                    new Deck(deckId, null));
            log.debug("Deck added to participant. Updated Participant: {}", updatedParticipant);

            collaborationRepository.updateDeckAddedParticipant(collaboration, updatedParticipant);
            log.info("Deck successfully added to Participant.");

            kafkaProducer.send(new DeckAdded(getTraceIdOrEmptyString(),
                    new DeckAddedDto(optCollaboration.get().getCollaborationId(), userId, deckId)));

        } else {
            log.trace("No Collaboration found for DeckCorrelationId '{}'. " +
                    "Deck does not belong to any active Participant.", correlationId);
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
