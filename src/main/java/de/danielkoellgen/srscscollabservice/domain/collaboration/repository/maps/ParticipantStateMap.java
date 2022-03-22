package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.State;
import org.springframework.data.cassandra.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantStateMap(

        @Column("transaction_id")
        UUID transactionId,

        @Column("status")
        Integer status,

        @Column("created_at")
        LocalDateTime createdAt
) {
        public ParticipantStateMap(State participantState) {
                this(participantState.transactionId(), participantState.status().ordinal(), participantState.createdAt());
        }
}
