package de.danielkoellgen.srscscollabservice.domain.collaboration.application;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.CollaborationStateException;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import de.danielkoellgen.srscscollabservice.domain.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;

    @Autowired
    public CollaborationService(CollaborationRepository collaborationRepository, UserRepository userRepository) {
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
    }

    public UUID startNewCollaboration(@NotNull UUID transactionId, @NotNull DeckName name,
            @NotNull List<Username> usernames) throws NoSuchElementException {
        List<User> users = usernames.stream().map(username ->
                userRepository.findUserByUsername(username).get()
        ).toList();

        Collaboration collaboration = Collaboration.startNewCollaboration(transactionId, name, users);
        collaborationRepository.saveNewCollaboration(collaboration);
        return collaboration.getCollaborationId();
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
