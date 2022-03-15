package de.danielkoellgen.srscscollabservice.domain.core;

import java.util.function.Function;

abstract public class AbstractStringValidation {

    public Boolean validateMinLengthOrThrow(String input, Integer minLength, Function<String, Exception> exception)
            throws Exception  {
        if (input.length() < minLength) {
            throw exception.apply("Input too short. Length is " + input.length() + " but required is " + minLength +
                    ".");
        }
        return true;
    }

    public Boolean validateMaxLengthOrThrow(String input, Integer maxLength, Function<String, Exception> exception)
            throws Exception {
        if (input.length() > maxLength) {
            throw exception.apply("Input too long. Length is " + input.length() + " but maximum allowed length is " +
                    maxLength + ".");
        }
        return true;
    }

    public Boolean validateRegexOrThrow(String input, String pattern, Function<String, Exception> exception)
            throws Exception {
        if (!input.matches(pattern)) {
            throw exception.apply("Input contains invalid charsets.");
        }
        return true;
    }
}
