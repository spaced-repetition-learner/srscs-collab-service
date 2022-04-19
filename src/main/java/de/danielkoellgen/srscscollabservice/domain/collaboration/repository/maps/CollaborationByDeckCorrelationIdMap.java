package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "collaboration_by_deckcorrelationid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationByDeckCorrelationIdMap {

    @PrimaryKeyColumn(name = "deck_correlation_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID deckCorrelationId;

    @Column("collaboration_id")
    private UUID collaborationId;


    public static @NotNull CollaborationByDeckCorrelationIdMap mapFromEntity(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        return new CollaborationByDeckCorrelationIdMap(
                participant.getDeckCorrelationId(),
                collaboration.getCollaborationId()
        );
    }
}
