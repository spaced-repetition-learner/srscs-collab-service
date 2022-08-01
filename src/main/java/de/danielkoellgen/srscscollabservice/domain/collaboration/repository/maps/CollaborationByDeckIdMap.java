package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(value = "collaboration_by_deckid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationByDeckIdMap {
    @PrimaryKeyColumn(name = "deck_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private @NotNull UUID deckId;

    @PrimaryKeyColumn(name = "collaboration_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private @NotNull UUID collaborationId;

    @PrimaryKeyColumn(name = "participant_user_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private @NotNull UUID participantUserId;

    @Column("participant_deck_id")
    private @Nullable UUID participantDeckId;

    @Column("participant_status")
    private @NotNull List<ParticipantStateMap> participantStatus;


    public static @NotNull List<CollaborationByDeckIdMap> mapFromEntity(@NotNull UUID deckId,
            @NotNull Collaboration collaboration) {
        return collaboration.getParticipants().values().stream()
                .map(x -> new CollaborationByDeckIdMap(
                        deckId,
                        collaboration.getCollaborationId(),
                        x.getUserId(),
                        (x.getDeck() != null
                                ? x.getDeck().getDeckId() : null),
                        x.getStatus().stream()
                                .map(ParticipantStateMap::new)
                                .toList()))
                .toList();
    }

    public static @NotNull CollaborationByDeckIdMap mapFromEntity(@NotNull UUID deckId,
            @NotNull Collaboration collaboration, @NotNull Participant participant) {
        return new CollaborationByDeckIdMap(
                deckId,
                collaboration.getCollaborationId(),
                participant.getUserId(),
                (participant.getDeck() != null
                        ? participant.getDeck().getDeckId() : null),
                participant.getStatus().stream()
                        .map(ParticipantStateMap::new)
                        .toList());
    }

    public static @NotNull Collaboration mapToEntityFromDatabase(
            @NotNull List<CollaborationByDeckIdMap> mappedParticipants) {
        UUID collaborationId = mappedParticipants.get(0).collaborationId;
        Map<UUID, Participant> participants = mappedParticipants.stream()
                .map(x -> {
                    User user = new User(x.participantUserId, null, null);
                    Deck deck = x.participantDeckId != null
                            ? new Deck(x.participantDeckId, user) : null;
                    return new Participant(
                            user,
                            deck,
                            null,
                            x.participantStatus.stream()
                                .map(ParticipantStateMap::mapToEntityFromDatabase)
                                .toList());
                }).collect(Collectors.toMap(Participant::getUserId, x -> x));
        return new Collaboration(collaborationId, null, participants);
    }
}
