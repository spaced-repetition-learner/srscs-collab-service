package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value = "collaboration_by_deckid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationByDeckIdMap {
    @PrimaryKeyColumn(name = "deck_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID deckId;

    @PrimaryKeyColumn(name = "collaboration_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID collaborationId;

    @PrimaryKeyColumn(name = "participant_user_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID participantUserId;

    @Column("participant_deck_id")
    private UUID participantDeckId;

    @Column("participant_status")
    private List<ParticipantStateMap> participantStatus;
}
