package de.danielkoellgen.srscscollabservice.controller.collaboration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import org.jetbrains.annotations.NotNull;

public record ParticipantRequestDto(

    @NotNull String username
) {
    @JsonIgnore
    public @NotNull Username getMappedUsername() {
        try {
            return new Username(username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Username from DTO.");
        }
    }
}
