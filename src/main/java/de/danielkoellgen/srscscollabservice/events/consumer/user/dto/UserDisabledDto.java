package de.danielkoellgen.srscscollabservice.events.consumer.user.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UserDisabledDto(

    @NotNull UUID userId

) {
    public static @NotNull UserDisabledDto makeFromSerialization(@NotNull String serialized) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return mapper.readValue(serialized, UserDisabledDto.class);
    }
}
