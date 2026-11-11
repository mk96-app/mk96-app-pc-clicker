package com.markes96.pcclicker.application.port.out;

/** The settings could not be read from or written to their storage. */
public class SettingsStorageException extends RuntimeException {

    public SettingsStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
