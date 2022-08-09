package de.danielkoellgen.srscscollabservice.domain.collaborationcard.repository.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Table(value = "collaborationcardevent")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationCardEvent {

    @PrimaryKeyColumn(name = "collaboration_card_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private @NotNull UUID collaborationCardId;

    @PrimaryKeyColumn(name = "root_card_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private @NotNull UUID rootCardId;

    @Column(value = "created_at")
    private @NotNull String createdAt;

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public static CollaborationCardEvent makeFromFormatted(UUID collaborationCardId, UUID rootCardId,
            LocalDateTime createdAt) {
        return new CollaborationCardEvent(collaborationCardId, rootCardId,
                createdAt.format(formatter));
    }

    public LocalDateTime getCreatedAtFormatted() {
        return LocalDateTime.parse(createdAt, formatter);
    }
}
