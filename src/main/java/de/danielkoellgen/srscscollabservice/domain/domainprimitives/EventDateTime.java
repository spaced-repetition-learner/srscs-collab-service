package de.danielkoellgen.srscscollabservice.domain.domainprimitives;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.PersistenceConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
public class EventDateTime {

    @Getter
    private final @NotNull LocalDateTime dateTime;

    private final static String pattern_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @PersistenceConstructor
    public EventDateTime(@NotNull LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public static @NotNull EventDateTime makeFromLocalDateTime(@NotNull LocalDateTime dateTime) {
        return new EventDateTime(dateTime);
    }

    public static @NotNull EventDateTime makeFromFormattedString(@NotNull String dateTimeFormatted) {
        return new EventDateTime(
                LocalDateTime.parse(dateTimeFormatted, DateTimeFormatter.ofPattern(pattern_RFC3339)));
    }

    public @NotNull String getFormatted() {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern_RFC3339));
    }

    @Override
    public String toString() {
        return "EventDateTime{" +
                "dateTime=" + getFormatted() +
                '}';
    }
}
