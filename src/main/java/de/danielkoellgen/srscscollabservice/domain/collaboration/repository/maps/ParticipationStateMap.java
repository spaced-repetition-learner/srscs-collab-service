package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import org.springframework.data.cassandra.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipationStateMap(

        @Column("transaction_id")
        UUID transactionId,

        @Column("status")
        Integer status,

        @Column("created_at")
        LocalDateTime createdAt
) {
}
