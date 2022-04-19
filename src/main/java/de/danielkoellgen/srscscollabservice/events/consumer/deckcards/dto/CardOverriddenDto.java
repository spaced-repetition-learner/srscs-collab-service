package de.danielkoellgen.srscscollabservice.events.consumer.deckcards.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CardOverriddenDto(

    @NotNull UUID parentCardId,

    @NotNull UUID newCardId,

    @NotNull UUID deckId

) {
    public static @NotNull CardOverriddenDto makeFromSerialization(@NotNull String serialized) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return mapper.readValue(serialized, CardOverriddenDto.class);
    }
}
