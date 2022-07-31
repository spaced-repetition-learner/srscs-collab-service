package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import de.danielkoellgen.srscscollabservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public class Participant {

    @NotNull
    private User user;

    @Nullable
    private Deck deck;

    @Nullable
    private UUID deckCorrelationId;

    @NotNull
    private List<State> status;

    private static final Logger logger = LoggerFactory.getLogger(Participant.class);


    public static @NotNull Participant createNewParticipant(@NotNull User user) {
        return new Participant(user, null, null, new ArrayList<>(List.of(
                new State(UUID.randomUUID(), ParticipantStatus.INVITED, LocalDateTime.now())
        )));
    }

    public void acceptParticipation() throws ParticipantStateException {
        if (getCurrentState().status() != ParticipantStatus.INVITED) {
            logger.debug("Failed to accept the Participation. Status is {} but should be {}.",
                    getCurrentState().status(), ParticipantStatus.INVITED
            );
            throw new ParticipantStateException("Accepting a participation whose status is "+getCurrentState().status()+
                    " is not allowed.");
        }
        status = Stream.concat(status.stream(), Stream.of(
                new State(UUID.randomUUID(), ParticipantStatus.INVITATION_ACCEPTED, LocalDateTime.now())
        )).toList();
        deckCorrelationId = UUID.randomUUID();


        deckCorrelationId = UUID.randomUUID();
        logger.debug("deck-correlation-id is {}", deckCorrelationId);
    }

    public void endParticipation() throws ParticipantStateException {
        if (getCurrentState().status() == ParticipantStatus.INVITED) {
            status = Stream.concat(status.stream(), Stream.of(
                    new State(UUID.randomUUID(), ParticipantStatus.INVITATION_DECLINED, LocalDateTime.now())
            )).toList();
            return;
        }
        if (getCurrentState().status() == ParticipantStatus.INVITATION_ACCEPTED) {
            status = Stream.concat(status.stream(), Stream.of(
                    new State(UUID.randomUUID(), ParticipantStatus.TERMINATED, LocalDateTime.now())
            )).toList();
            return;
        }

        logger.debug("Failed to end Participation. Status is {}.", getCurrentState().status());
        throw new ParticipantStateException("Ending a participation whose status is "+getCurrentState().status()+
                " is not allowed.");
    }

    public void setDeck(@NotNull UUID deckCorrelationId, @NotNull Deck deck) {
        if (deckCorrelationId != this.deckCorrelationId) {
            throw new IllegalArgumentException("Deck does not match transaction-id");
        }
        this.deck = deck;
        logger.trace("Deck added to Participant.");
    }

    public @NotNull State getCurrentState() {
        return status.stream().max((x, y) -> x.createdAt().compareTo(y.createdAt())).get();
    }

    public @NotNull ParticipantStatus getCurrentParticipantStatus() {
        return getCurrentState().status();
    }

    public @NotNull Boolean isActive() {
        return getCurrentParticipantStatus().equals(ParticipantStatus.INVITATION_ACCEPTED);
    }

    public @NotNull UUID getUserId() {
        return user.getUserId();
    }

    @Override
    public String toString() {
        return "Participant{" +
                "user=" + user +
                ", deck=" + deck +
                ", deckCorrelationId=" + deckCorrelationId +
                ", status=" + status +
                '}';
    }
}
