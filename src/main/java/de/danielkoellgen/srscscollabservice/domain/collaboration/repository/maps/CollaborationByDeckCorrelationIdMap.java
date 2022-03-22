package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "collaboration_by_deckcorrelationid")
public record CollaborationByDeckCorrelationIdMap(

        @PrimaryKeyColumn(name = "deck_correlation_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        UUID deckCorrelationId,

        @Column("collaboration_id")
        UUID collaborationId
) {
}
