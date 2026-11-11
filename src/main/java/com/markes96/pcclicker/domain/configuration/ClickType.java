package com.markes96.pcclicker.domain.configuration;

/**
 * What a single automatic click does. A double click is one click of the session: it counts once
 * towards the rate, the counter and the click limit.
 */
public enum ClickType {

    SINGLE(1),
    DOUBLE(2);

    private final int presses;

    ClickType(int presses) {
        this.presses = presses;
    }

    /** How many times the button goes down and up. */
    public int presses() {
        return presses;
    }
}
