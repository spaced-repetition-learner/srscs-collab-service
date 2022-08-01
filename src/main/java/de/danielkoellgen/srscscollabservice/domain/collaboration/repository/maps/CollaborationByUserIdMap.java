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
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(value = "collaboration_by_userid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationByUserIdMap {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "collaboration_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID collaborationId;

    @PrimaryKeyColumn(name = "participant_user_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID participantUserId;

    @Column(value = "collaboration_name", isStatic = true)
    private String collaborationName;

    @Column("participant_username")
    private String participantUsername;

    @Column("participant_deck_id")
    private UUID participantDeckId;

    @Column("participant_deck_correlation_id")
    private UUID participantDeckCorrelationId;

    @Column("participant_status")
    private List<ParticipantStateMap> participantStatus;


    public static @NotNull CollaborationByUserIdMap mapFromEntity(@NotNull UUID userId,
            @NotNull Collaboration collaboration, @NotNull Participant participant) {
        return new CollaborationByUserIdMap(
                userId,
                collaboration.getCollaborationId(),
                participant.getUserId(),
                (collaboration.getName() != null
                        ? collaboration.getName().getName() : null),
                participant.getUser().getUsername().getUsername(),
                (participant.getDeck() != null
                        ? participant.getDeck().getDeckId() : null),
                participant.getDeckCorrelationId(),
                participant.getStatus().stream()
                        .map(ParticipantStateMap::new)
                        .toList()
        );
    }

    public static @NotNull Collaboration mapToEntityFromDatabase(
            @NotNull List<CollaborationByUserIdMap> mappedParticipants) {
        List<Participant> participants = new ArrayList<>();
        try {
            for (CollaborationByUserIdMap mappedParticipant : mappedParticipants) {
                User user = new User(
                        mappedParticipant.participantUserId,
                        new Username(mappedParticipant.participantUsername),
                        null);
                Deck deck = mappedParticipant.participantDeckId != null
                        ? new Deck(mappedParticipant.participantDeckId, user) : null;
                Participant participant = new Participant(
                        user,
                        deck,
                        mappedParticipant.participantDeckCorrelationId,
                        mappedParticipant.participantStatus.stream()
                                .map(ParticipantStateMap::mapToEntityFromDatabase)
                                .toList());
                participants.add(participant);
            }
            return new Collaboration(
                    mappedParticipants.get(0).collaborationId,
                    new DeckName(mappedParticipants.get(0).collaborationName),
                    participants.stream()
                            .collect(Collectors.toMap(Participant::getUserId, participant -> participant))
            );
        } catch (Exception e) {
            throw new IllegalRepositoryMappingException("Failed mapping between entity and database. " +
                    e.getMessage());
        }
    }
}
