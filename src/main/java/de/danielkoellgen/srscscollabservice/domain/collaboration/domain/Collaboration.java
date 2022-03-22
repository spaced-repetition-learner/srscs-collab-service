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

@AllArgsConstructor
public class Collaboration {

    @Getter
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

    public void inviteParticipant(@NotNull UUID transactionId, @NotNull User user) throws CollaborationStateException{
        if (getCollaborationStatus() == CollaborationStatus.TERMINATED) {
            throw new CollaborationStateException("Collaboration has already ended.");
        }
        if (getParticipants().containsKey(user.getUserId())) {
            throw new CollaborationStateException("User is already participating.");
        }
        getParticipants().put(user.getUserId(), Participant.createNewParticipant(transactionId, user));
        //TODO clone deck somehow
    }

    public Boolean acceptInvitation(@NotNull UUID transactionId, @NotNull UUID userId) throws ParticipantStateException {
        Participant participant = getParticipants().get(userId);
        participant.acceptParticipation(transactionId);
        return true;
    }

    public void endParticipation(@NotNull UUID transactionId, @NotNull UUID userId) throws ParticipantStateException {
        getParticipants().get(userId).endParticipation(transactionId);
    }

    public void setDeck(@NotNull UUID userId, @NotNull UUID deckCorrelationId, @NotNull Deck deck) {
        getParticipants().get(userId).setDeck(deckCorrelationId, deck);
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

    public @NotNull DeckName getName() {
        if (name == null) {
            throw new IllegalEntityPersistenceState("[name] not instantiated while trying to access it.");
        }
        return name;
    }

    public @NotNull Map<UUID, Participant> getParticipants() {
        if (participants == null) {
            throw new IllegalEntityPersistenceState("[participants] not instantiated while trying to access it.");
        }
        return participants;
    }
}
