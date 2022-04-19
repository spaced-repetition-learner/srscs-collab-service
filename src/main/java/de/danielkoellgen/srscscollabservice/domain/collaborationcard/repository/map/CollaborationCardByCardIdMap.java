package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardVersion;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value = "collaborationcard_by_cardid")
public record CollaborationCardByCardIdMap(

        @PrimaryKeyColumn(name = "correlation_card_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        UUID correlationCardId,

        @Column("collaboration_card_id")
        UUID collaborationCardId,

        @Column("card_version_id")
        UUID cardVersionId,

        @Column("collaboration_id")
        UUID collaborationId
) {

    public static @NotNull CollaborationCard mapToEntityFromDatabase(@NotNull CollaborationCardByCardIdMap cbc) {
        Collaboration collaboration = new Collaboration(cbc.collaborationId);
        CardVersion cardVersion = new CardVersion(cbc.cardVersionId);
        return new CollaborationCard(
                cbc.collaborationCardId, collaboration, List.of(cardVersion)
        );
    }
}
