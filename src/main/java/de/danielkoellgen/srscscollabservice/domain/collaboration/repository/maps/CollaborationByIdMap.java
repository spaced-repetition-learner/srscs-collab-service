package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import de.danielkoellgen.srscscollabservice.domain.core.IllegalRepositoryMappingException;
import de.danielkoellgen.srscscollabservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(value = "collaboration_by_id")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationByIdMap{

    @PrimaryKeyColumn(name = "collaboration_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @NotNull
    private UUID collaborationId;

    @PrimaryKeyColumn(name = "participant_user_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    @NotNull
    private UUID participantUserId;

    @Column(value = "collaboration_name", isStatic = true)
    @NotNull
    private String collaborationName;

    @Column("participant_username")
    @NotNull
    private String participantUsername;

    @Column("participant_deck_id")
    @Nullable
    private UUID participantDeckId;

    @Column("participant_deck_correlation_id")
    @Nullable
    private UUID participantDeckCorrelationId;

    @Column("participant_status")
    @NotNull
    List<ParticipantStateMap> participantStatus;


    public static @NotNull CollaborationByIdMap mapFromEntity(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        return new CollaborationByIdMap(
                collaboration.getCollaborationId(),
                participant.getUserId(),
                collaboration.getName().getName(),
                participant.getUser().getUsername().getUsername(),
                (participant.getDeck() != null
                        ? participant.getDeck().getDeckId() : null),
                (participant.getDeckCorrelationId() != null
                        ? participant.getDeckCorrelationId() : null),
                participant.getStatus().stream()
                        .map(ParticipantStateMap::new)
                        .toList()
        );
    }

    public static @NotNull Collaboration mapToEntityFromDatabase(
            @NotNull List<CollaborationByIdMap> mappedParticipants) {
        List<Participant> participants = new ArrayList<>();

        try {
            for (CollaborationByIdMap mappedParticipant : mappedParticipants) {
                User user = new User(mappedParticipant.participantUserId,
                        new Username(mappedParticipant.participantUsername), null);
                Deck deck = mappedParticipant.participantDeckId != null
                        ? new Deck(mappedParticipant.participantDeckId, user) : null;
                Participant participant = new Participant(
                        user,
                        deck,
                        mappedParticipant.participantDeckCorrelationId,
                        mappedParticipant.participantStatus.stream()
                                .map(ParticipantStateMap::mapToEntityFromDatabase).toList());
                participants.add(participant);
            }
            return new Collaboration(mappedParticipants.get(0).collaborationId,
                    new DeckName(mappedParticipants.get(0).collaborationName),
                    participants.stream()
                            .collect(Collectors.toMap(Participant::getUserId,
                                    participant -> participant))
            );
        } catch (Exception e) {
            throw new IllegalRepositoryMappingException("Failed mapping between entity and database. "
                    + e.getMessage());
        }
    }
}
