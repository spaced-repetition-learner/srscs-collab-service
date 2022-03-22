package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value = "collaboration_by_deckid")
public record CollaborationByDeckIdMap(

        @PrimaryKeyColumn(name = "deck_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        UUID deckId,

        @PrimaryKeyColumn(name = "collaboration_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        UUID collaborationId,

        @PrimaryKeyColumn(name = "participant_user_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        UUID participantUserId,

        @Column("participant_deck_id")
        UUID participantDeckId,

        @Column("participant_status")
        List<ParticipationStateMap> participantStatus
) {
}
