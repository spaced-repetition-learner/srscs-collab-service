package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value = "collaboration_by_id")
public record CollaborationByIdMap(

        @PrimaryKeyColumn(name = "collaboration_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        UUID collaborationId,

        @PrimaryKeyColumn(name = "participation_user_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        UUID participationUserId,

        @Column(value = "collaboration_name", isStatic = true)
        String collaborationName,

        @Column("participation_username")
        String participationUsername,

        @Column("participation_deck_id")
        UUID participationDeckId,

        @Column("participation_deck_correlation_id")
        UUID participationDeckCorrelationId,

        @Column("participation_status")
        List<?> participationStatus
) {
    //TODO
}
