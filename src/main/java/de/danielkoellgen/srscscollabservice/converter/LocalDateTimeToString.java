package de.danielkoellgen.srscscollabservice.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LocalDateTimeToString implements Converter<LocalDateTime, String> {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public String convert(LocalDateTime source) {
        return source.format(formatter);
    }

}
