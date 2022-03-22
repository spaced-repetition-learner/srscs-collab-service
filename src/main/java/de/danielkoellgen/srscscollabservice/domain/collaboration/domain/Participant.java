package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

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
    private UUID deckTransactionId;

    @Getter
    @NotNull
    private List<State> status;


    public static Participant createParticipationAsInvited(UUID transactionId, User user) {
        return new Participant(user, null, null, new ArrayList<>(List.of(
                new State(transactionId, ParticipantStatus.INVITED, LocalDateTime.now())
        )));
    }

    public void acceptParticipation(UUID transactionId) throws ParticipantStateException {
        if (getCurrentState().status() == ParticipantStatus.INVITED) {
            status.add(new State(transactionId, ParticipantStatus.INVITATION_ACCEPTED, LocalDateTime.now()));
            return;
        }
        throw new ParticipantStateException("Accepting a participation whose status is "+getCurrentState().status()+
                " is not allowed.");
    }

    public void endParticipation(UUID transactionId) throws ParticipantStateException {
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

    public void setDeckTransactionId(UUID deckTransactionId) {
        if (deckTransactionId != null) {
            throw new IllegalStateException("Deck-TransactionId has already been set.");
        }
        this.deckTransactionId = deckTransactionId;
    }

    public void setDeck(UUID deckTransactionId, Deck deck) {
        if (deckTransactionId != this.deckTransactionId) {
            throw new IllegalArgumentException("Deck does not match transaction-id");
        }
        if (deck.getUser().getUserId() != this.user.getUserId()) {
            throw new IllegalArgumentException("Deck does not match user.");
        }
        this.deck = deck;
    }

    public @NotNull Deck getDeck() throws IllegalStateException {
        if (deck == null) {
            throw new IllegalStateException("Deck has not been added yet. Status is "+getCurrentState().status()+".");
        }
        return deck;
    }

    public State getCurrentState() {
        return status.stream().max((x, y) -> x.createdAt().compareTo(y.createdAt())).get();
    }
}
