package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CardVersion;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Table(value = "collaborationcard_by_transactionid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public record CollaborationCardByTransactionIdMap(

        @PrimaryKeyColumn(name = "correlation_card_transaction_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        UUID correlationCardTransactionId,

        @Column("collaboration_id")
        UUID collaborationId,

        @Column("collaboration_card_id")
        UUID collaborationCardId,

        @Column("card_version_id")
        UUID cardVersionId
) {
    public static @NotNull CollaborationCard mapToEntityFromDatabase(@NotNull CollaborationCardByTransactionIdMap cbc) {
        Correlation correlation = new Correlation(null, cbc.correlationCardTransactionId);
        CardVersion cardVersion = new CardVersion(cbc.cardVersionId, Set.of(correlation));
        Collaboration collaboration = new Collaboration(cbc.collaborationId);
        return new CollaborationCard(
                cbc.collaborationCardId, collaboration, List.of(cardVersion)
        );
    }
}
