package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import de.danielkoellgen.srscscollabservice.domain.collaborationcard.domain.Correlation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "collaborationcard_by_cardid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationByCardId {

    @PrimaryKeyColumn(name = "card_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private @NotNull UUID cardId;

    @Column(value = "root_card_id")
    private @NotNull UUID rootCardId;


    public static @NotNull CorrelationByCardId mapFromEntity(@NotNull Correlation correlation) {
        return new CorrelationByCardId(correlation.cardId(), correlation.rootCardId());
    }
}
