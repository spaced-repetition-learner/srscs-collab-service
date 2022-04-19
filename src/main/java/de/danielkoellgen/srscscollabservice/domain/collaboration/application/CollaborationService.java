package de.danielkoellgen.srscscollabservice.domain.collaboration.application;

import de.danielkoellgen.srscscollabservice.controller.collaboration.CollaborationController;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.CollaborationStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(CollaborationService.class);

    @Autowired
    public CollaborationService(CollaborationRepository collaborationRepository, UserRepository userRepository) {
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
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

    public void inviteUserToCollaboration(@NotNull UUID transactionId, @NotNull UUID collaborationId,
            @NotNull Username username) throws NoSuchElementException, CollaborationStateException {
        User user = userRepository.findUserByUsername(username).get();

        Collaboration collaboration = collaborationRepository.findCollaborationById(collaborationId).get();
        Participant newParticipant = collaboration.inviteParticipant(transactionId, user);
        collaborationRepository.saveNewParticipant(collaboration, newParticipant);
    }

    public void acceptParticipation(@NotNull UUID transactionId, @NotNull UUID collaborationId, @NotNull UUID userId)
            throws NoSuchElementException {

    }

    public void endParticipation(@NotNull UUID transactionId, @NotNull UUID collaborationId, @NotNull UUID userId)
            throws NoSuchElementException {

    }

    public void addCorrespondingDeckToParticipant(@NotNull UUID transactionId) {
        //TODO populate with existing cards
    }
}
