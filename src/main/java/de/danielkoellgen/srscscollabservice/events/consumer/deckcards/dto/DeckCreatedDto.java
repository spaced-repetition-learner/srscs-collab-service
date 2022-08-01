package de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscscollabservice.domain.domainprimitives.DeckName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DeckCreatedDto(

    @NotNull UUID deckId,

    @NotNull UUID userId,

    @NotNull String deckName

) {
    public static @NotNull DeckCreatedDto makeFromSerialization(@NotNull String serialized)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return mapper.readValue(serialized, DeckCreatedDto.class);
    }

    @JsonIgnore
    public @NotNull DeckName getMappedDeckName() {
        try {
            return new DeckName(deckName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DeckName from event payload.");
        }
    }
}
