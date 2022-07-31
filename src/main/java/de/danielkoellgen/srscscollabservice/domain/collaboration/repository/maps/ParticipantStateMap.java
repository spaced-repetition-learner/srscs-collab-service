package de.danielkoellgen.srscscollabservice.domain.collaboration.repository.maps;

import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.ParticipantStatus;
import de.danielkoellgen.srscscollabservice.domain.collaboration.domain.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.time.LocalDateTime;
import java.util.UUID;

@UserDefinedType("participation_state")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantStateMap {

    @Column("status")
    @NotNull
    private Integer status;

    @Column("created_at")
    @NotNull
    private LocalDateTime createdAt;

    public ParticipantStateMap(State participantState) {
        this(ParticipantStatus.fromEnum(participantState.status()), participantState.createdAt());
    }

    public static @NotNull State mapToEntityFromDatabase(@NotNull ParticipantStateMap stateMap) {
        return new State(ParticipantStatus.fromNumber(stateMap.status), stateMap.createdAt);
    }
}
