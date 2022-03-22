package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class Collaboration {

    @NotNull
    private final UUID collaborationId;

    @Nullable
    private String name;

    @Nullable
    private Map<UUID, Participant> participants;

    public Collaboration(UUID collaborationId) {
        this.collaborationId = collaborationId;
    }

    public static Collaboration startNewCollaboration(UUID transactionId, String name, List<User> invitedUsers) {
        Map<UUID, Participant> participants = invitedUsers.stream()
                .collect(Collectors.toMap(User::getUserId,
                        user -> Participant.createParticipationAsInvited(transactionId, user)));
        return new Collaboration(UUID.randomUUID(), name, participants);
    }


    public void inviteParticipant(UUID transactionId, User user) throws CollaborationStateException{
        if (getCollaborationStatus() == CollaborationStatus.TERMINATED) {
            throw new CollaborationStateException("Collaboration has already ended.");
        }
        if (participants.containsKey(user.getUserId())) {
            throw new CollaborationStateException("User is already participating.");
        }
        participants.put(user.getUserId(), Participant.createParticipationAsInvited(transactionId, user));
    }

    public Boolean acceptInvitation(UUID transactionId, UUID userId) throws ParticipantStateException {
        participants.get(userId).acceptParticipation(transactionId);
        participants.get(userId).setDeckTransactionId(transactionId);
        return true;
    }

    public void endParticipation(UUID transactionId, UUID userId) throws ParticipantStateException {
        participants.get(userId).endParticipation(transactionId);
    }

    public void setDeck(UUID transactionId, UUID userId, Deck deck) {
        participants.get(userId).setDeck(transactionId, deck);
    }

    public CollaborationStatus getCollaborationStatus() {
        AtomicReference<Integer> participants = new AtomicReference<>(0);
        this.participants.forEach((userId, participant) -> {
            switch(participant.getCurrentState().status()) {
                case INVITED -> participants.set(participants.get() + 1);
                case INVITATION_ACCEPTED -> participants.getAndSet(participants.get() + 1);
            }
        });
        if (participants.get() >= 2) {
            return CollaborationStatus.ACTIVE;
        } else {
            return CollaborationStatus.TERMINATED;
        }
    }
}
