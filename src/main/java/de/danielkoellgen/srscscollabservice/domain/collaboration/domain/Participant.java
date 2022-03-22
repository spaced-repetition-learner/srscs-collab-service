package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import de.danielkoellgen.srscscollabservice.domain.core.IllegalEntityPersistenceState;
import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class Participant {

    @Getter
    @NotNull
    private User user;

    @Nullable
    private Deck deck;

    @Getter
    @NotNull
    private UUID deckCorrelationId;

    @Getter
    @NotNull
    private List<State> status;


    public static @NotNull Participant createNewParticipant(@NotNull UUID transactionId, @NotNull User user) {
        return new Participant(user, null, UUID.randomUUID(), new ArrayList<>(List.of(
                new State(transactionId, ParticipantStatus.INVITED, LocalDateTime.now())
        )));
    }

    public void acceptParticipation(@NotNull UUID transactionId) throws ParticipantStateException {
        if (getCurrentState().status() != ParticipantStatus.INVITED) {
            throw new ParticipantStateException("Accepting a participation whose status is "+getCurrentState().status()+
                    " is not allowed.");
        }
        status.add(new State(transactionId, ParticipantStatus.INVITATION_ACCEPTED, LocalDateTime.now()));
    }

    public void endParticipation(@NotNull UUID transactionId) throws ParticipantStateException {
        if (getCurrentState().status() == ParticipantStatus.INVITED) {
            status.add(new State(transactionId, ParticipantStatus.INVITATION_DECLINED, LocalDateTime.now()));
            return;
        }
        if (getCurrentState().status() == ParticipantStatus.INVITATION_ACCEPTED) {
            status.add(new State(transactionId, ParticipantStatus.TERMINATED, LocalDateTime.now()));
            return;
        }
        throw new ParticipantStateException("Ending a participation whose status is "+getCurrentState().status()+
                " is not allowed.");
    }

    public void setDeck(@NotNull UUID deckCorrelationId, @NotNull Deck deck) {
        if (deckCorrelationId != this.deckCorrelationId) {
            throw new IllegalArgumentException("Deck does not match transaction-id");
        }
        if (deck.getUser().getUserId() != this.user.getUserId()) {
            throw new IllegalArgumentException("Deck does not match user.");
        }
        this.deck = deck;
    }

    public @NotNull State getCurrentState() {
        return status.stream().max((x, y) -> x.createdAt().compareTo(y.createdAt())).get();
    }

    public @NotNull Deck getDeck() {
        if (deck == null) {
            throw new IllegalEntityPersistenceState("[deck] not instantiated while trying to access it.");
        }
        return deck;
    }
}
