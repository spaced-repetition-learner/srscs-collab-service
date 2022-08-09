package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "collaborationcardeventlock")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationCardEventLock {

    @PrimaryKeyColumn(name = "collaboration_card_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private @NotNull UUID collaborationCardId;

    @PrimaryKeyColumn(name = "participant_user_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private @NotNull UUID userId;
}
