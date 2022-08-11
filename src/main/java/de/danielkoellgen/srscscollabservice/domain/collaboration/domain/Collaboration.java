package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private static final Logger log = LoggerFactory.getLogger(Collaboration.class);

    public Collaboration(@NotNull UUID collaborationId) {
        this.collaborationId = collaborationId;
    }

    public static @NotNull Collaboration startNewCollaboration(@NotNull DeckName name,
            @NotNull List<User> invitedUsers) {
        Map<UUID, Participant> participants = invitedUsers
                .stream()
                .collect(Collectors.toMap(User::getUserId, Participant::createNewParticipant));
        return new Collaboration(UUID.randomUUID(), name, participants);
    }

    public @NotNull Participant inviteParticipant(@NotNull User user)
            throws CollaborationStateException{
        log.trace("Inviting User to Collaboration.");
        if (getCollaborationStatus() == CollaborationStatus.TERMINATED) {
            log.warn("Failed to invite User. Collaboration-Status is {}.", getCollaborationStatus());
            throw new CollaborationStateException("Invite failed. " +
                    "Collaboration is already terminated.");
        }
        if (getParticipants().containsKey(user.getUserId())) {
            log.warn("Failed to invite User. User already participates.");
            throw new CollaborationStateException("Invite failed. " +
                    "User is already participating.");
        }
        Participant newParticipant = Participant.createNewParticipant(user);
        participants.put(user.getUserId(), newParticipant);
        log.trace("New Participant added to Collaboration.");
        return newParticipant;
    }

    public @NotNull Participant acceptInvitation(@NotNull UUID userId)
            throws ParticipantStateException {
        log.trace("Accepting Participation...");
        Participant participant = getParticipants().get(userId);
        participant.acceptParticipation();
        return participant;
    }

    public @NotNull Participant endParticipation(@NotNull UUID userId)
            throws ParticipantStateException {
        log.trace("Ending Participation...");
        Participant participant = getParticipants().get(userId);
        participant.endParticipation();
        return participant;
    }

    public @NotNull Participant setDeck(@NotNull UUID userId, @NotNull UUID deckCorrelationId,
            @NotNull Deck deck) {
        log.trace("Setting Deck for Participant.");
        Participant participant = getParticipants().get(userId);
        participant.setDeck(deckCorrelationId, deck);
        return participant;
    }

    public CollaborationStatus getCollaborationStatus() {
        AtomicReference<Integer> activeOrInvitedParticipantCount = new AtomicReference<>(0);
        getParticipants().forEach((userId, participant) -> {
            switch (participant.getCurrentState().status()) {
                case INVITED -> activeOrInvitedParticipantCount
                        .set(activeOrInvitedParticipantCount.get() + 1);
                case INVITATION_ACCEPTED -> activeOrInvitedParticipantCount
                        .getAndSet(activeOrInvitedParticipantCount.get() + 1);
            }
        });
        return activeOrInvitedParticipantCount.get() >= 2
                ? CollaborationStatus.ACTIVE
                : CollaborationStatus.TERMINATED;
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
