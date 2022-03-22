package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value = "collaboration_by_userid")
public record CollaborationByUserIdMap(

        @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        UUID userId,

        @PrimaryKeyColumn(name = "collaboration_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        UUID collaborationId,

        @PrimaryKeyColumn(name = "participant_user_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        UUID participantUserId,

        @Column(value = "collaboration_name", isStatic = true)
        String collaborationName,

        @Column("participant_username")
        String participantUsername,

        @Column("participant_deck_id")
        UUID participantDeckId,

        @Column("participant_status")
        List<ParticipantStateMap> participantStatus
) {
}
