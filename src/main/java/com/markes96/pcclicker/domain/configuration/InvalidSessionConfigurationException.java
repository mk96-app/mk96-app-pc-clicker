package com.markes96.pcclicker.domain.configuration;

/** Thrown when a set of settings cannot describe a valid session. */
public final class InvalidSessionConfigurationException extends IllegalArgumentException {

    public InvalidSessionConfigurationException(String message) {
        super(message);
    }
}
