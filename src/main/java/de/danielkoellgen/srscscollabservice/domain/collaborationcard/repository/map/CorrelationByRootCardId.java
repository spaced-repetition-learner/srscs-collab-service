package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.CollaborationCard;
import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value = "collaborationcard_by_rootcardid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationByRootCardId {

    @PrimaryKeyColumn(name = "root_card_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private @NotNull UUID rootCardId;

    @PrimaryKeyColumn(name = "correlation_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private @NotNull UUID correlationId;

    @Column(value = "collaboration_card_id", isStatic = true)
    private @NotNull UUID collaborationCardId;

    @Column(value = "collaboration_id")
    private @NotNull UUID collaborationId;

    @Column(value = "deck_id")
    private @NotNull UUID deckId;

    @Column(value = "user_id")
    private @NotNull UUID userId;

    @Column(value = "card_id")
    private @Nullable UUID cardId;


    public static @NotNull List<CorrelationByRootCardId> mapFromEntity(@NotNull UUID collaborationId,
            @NotNull UUID collaborationCardId, @NotNull List<Correlation> correlations) {
        return correlations.stream()
                .map(x -> new CorrelationByRootCardId(
                        x.rootCardId(),
                        x.correlationId(),
                        collaborationCardId,
                        collaborationId,
                        x.deckId(),
                        x.userId(),
                        x.cardId()
                ))
                .toList();
    }
}
