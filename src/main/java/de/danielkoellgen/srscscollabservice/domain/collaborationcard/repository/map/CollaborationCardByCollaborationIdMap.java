package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import de.danielkoellgen.srscscollabservice.domain.card.domain.Card;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardVersion;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.core.IllegalRepositoryMappingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Table(value = "collaborationcard_by_collaborationid")
public record CollaborationCardByCollaborationIdMap(

        @PrimaryKeyColumn(name = "collaboration_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        @NotNull
        UUID collaborationId,

        @PrimaryKeyColumn(name = "collaboration_card_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        @NotNull
        UUID collaborationCardId,

        @NotNull
        UUID cardVersionId,

        @Column("root_card_id")
        @NotNull
        UUID rootCardId
) {
    /*
     *  It is assumed that only the newest CardVersion for each CollaborationCard is fetched. Therefore, a
     *      CollaborationCard can have only one CardVersion. Duplicates will result in an exception.
     */
    public static @NotNull List<CollaborationCard> mapToEntityFromDatabase(
            @NotNull List<CollaborationCardByCollaborationIdMap> collaborationCardVersions
    ) {
        Map<UUID, CollaborationCard> collaborationCardMap = new HashMap<>();
        collaborationCardVersions.forEach( cbc -> {
            Card rootCard = new Card(cbc.rootCardId, null);
            CardVersion cardVersion = new CardVersion(cbc.cardVersionId, rootCard);
            Collaboration collaboration = new Collaboration(cbc.collaborationId);
            CollaborationCard collaborationCard = new CollaborationCard(
                    cbc.collaborationCardId, collaboration, List.of(cardVersion)
            );
            if (collaborationCardMap.containsKey(collaborationCard.getCollaborationCardId())) {
                throw new IllegalRepositoryMappingException("Duplicated CollaborationCard detected while mapping. "+
                        "This error occurred, because multiple CardVersions per CollaborationCard were fetched.");
            }
            collaborationCardMap.put(collaborationCard.getCollaborationCardId(), collaborationCard);
        });
        return collaborationCardMap.values().stream().toList();
    }
}
