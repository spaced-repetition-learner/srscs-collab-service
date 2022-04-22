package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import de.danielkoellgen.srscscollabservice.domain.core.IllegalEntityPersistenceState;
import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
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
    private DeckName name;

    @Nullable
    private Map<UUID, Participant> participants;

    public Collaboration(@NotNull UUID collaborationId) {
        this.collaborationId = collaborationId;
    }

    public static @NotNull Collaboration startNewCollaboration(@NotNull UUID transactionId, @NotNull DeckName name,
            @NotNull List<User> invitedUsers) {
        Map<UUID, Participant> participants = invitedUsers.stream()
                .collect(Collectors.toMap(User::getUserId,
                        user -> Participant.createNewParticipant(transactionId, user))
                );
        return new Collaboration(UUID.randomUUID(), name, participants);
    }

    public @NotNull Participant inviteParticipant(@NotNull UUID transactionId, @NotNull User user)
            throws CollaborationStateException{
        if (getCollaborationStatus() == CollaborationStatus.TERMINATED) {
            throw new CollaborationStateException("Collaboration has already ended.");
        }
        if (getParticipants().containsKey(user.getUserId())) {
            throw new CollaborationStateException("User is already participating.");
        }
        Participant newParticipant = Participant.createNewParticipant(transactionId, user);
        getParticipants().put(user.getUserId(), newParticipant);
        return newParticipant;
    }

    public @NotNull Participant acceptInvitation(@NotNull UUID transactionId, @NotNull UUID userId) throws
            ParticipantStateException {
        Participant participant = getParticipants().get(userId);
        participant.acceptParticipation(transactionId);
        return participant;
    }

    public @NotNull Participant endParticipation(@NotNull UUID transactionId, @NotNull UUID userId) throws
            ParticipantStateException {
        Participant participant = getParticipants().get(userId);
        participant.endParticipation(transactionId);
        return participant;
    }

    public @NotNull Participant setDeck(@NotNull UUID userId, @NotNull UUID deckCorrelationId, @NotNull Deck deck) {
        Participant participant = getParticipants().get(userId);
        participant.setDeck(deckCorrelationId, deck);
        return participant;
    }

    public CollaborationStatus getCollaborationStatus() {
        AtomicReference<Integer> activeOrInvitedParticipantCount = new AtomicReference<>(0);
        getParticipants().forEach((userId, participant) -> {
            switch(participant.getCurrentState().status()) {
                case INVITED -> activeOrInvitedParticipantCount.set(activeOrInvitedParticipantCount.get() + 1);
                case INVITATION_ACCEPTED -> activeOrInvitedParticipantCount.getAndSet(
                        activeOrInvitedParticipantCount.get() + 1);
            }
        });
        return activeOrInvitedParticipantCount.get() >= 2 ? CollaborationStatus.ACTIVE : CollaborationStatus.TERMINATED;
    }

    @Override
    public String toString() {
        return "Collaboration{" +
                "collaborationId=" + collaborationId +
                ", name=" + name +
                ", participants=" + participants +
                '}';
    }
}
