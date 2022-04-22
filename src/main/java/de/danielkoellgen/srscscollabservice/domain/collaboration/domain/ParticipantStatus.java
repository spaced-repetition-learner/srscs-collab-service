package de.danielkoellgen.srscscollabservice.domain.collaboration.domain;

import de.danielkoellgen.srscscollabservice.domain.core.IllegalMappingException;
import org.jetbrains.annotations.NotNull;

public enum ParticipantStatus {
    INVITED,
    INVITATION_ACCEPTED,
    INVITATION_DECLINED,
    TERMINATED;

    public static @NotNull Integer fromEnum(@NotNull ParticipantStatus status) {
        return switch (status) {
            case INVITED -> 0;
            case INVITATION_ACCEPTED -> 1;
            case INVITATION_DECLINED -> 2;
            case TERMINATED -> 3;
        };
    }

    public static @NotNull ParticipantStatus fromNumber(@NotNull Integer number) {
        return switch (number) {
            case 0 -> INVITED;
            case 1 -> INVITATION_ACCEPTED;
            case 2 -> INVITATION_DECLINED;
            case 3 -> TERMINATED;
            default -> throw new IllegalMappingException("Failed to map ParticipantStatus. Number is [" + number + "].");
        };
    }

    public static @NotNull String toStringFromEnum(@NotNull ParticipantStatus status) {
        return switch(status) {
            case INVITED                -> "INVITED";
            case INVITATION_ACCEPTED    -> "INVITATION_ACCEPTED";
            case INVITATION_DECLINED    -> "INVITATION_DECLINED";
            case TERMINATED             -> "TERMINATED";
        };
    }

    public static @NotNull ParticipantStatus toEnumFromString(@NotNull String status) {
        return switch(status) {
            case "INVITED"              -> ParticipantStatus.INVITED;
            case "INVITATION_ACCEPTED"  -> ParticipantStatus.INVITATION_ACCEPTED;
            case "INVITATION_DECLINED"  -> ParticipantStatus.INVITATION_DECLINED;
            case "TERMINATED"           -> ParticipantStatus.TERMINATED;
            default -> throw new IllegalMappingException("Failed to map to ParticipantStatus from String.");
        };
    }
}
