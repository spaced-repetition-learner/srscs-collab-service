package de.danielkoellgen.srscscollabservice.domain.core;

public class IllegalEntityPersistenceState extends RuntimeException {

    public IllegalEntityPersistenceState(String message) {
        super(message);
    }
}
