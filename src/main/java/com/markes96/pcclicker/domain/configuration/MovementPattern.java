package com.markes96.pcclicker.domain.configuration;

/** Shape followed by the pointer while a session moves the mouse. */
public enum MovementPattern {

    /** Wanders around the screen changing direction a little on every step. */
    RANDOM(false, false),
    /** Draws a circle around the starting point. */
    CIRCLE(true, false),
    /** Draws a regular polygon around the starting point. */
    POLYGON(true, true),
    /** Travels in straight lines, bouncing off the screen edges. */
    REBOUND(false, false);

    private final boolean usesRadius;
    private final boolean usesVertices;

    MovementPattern(boolean usesRadius, boolean usesVertices) {
        this.usesRadius = usesRadius;
        this.usesVertices = usesVertices;
    }

    public boolean usesRadius() {
        return usesRadius;
    }

    public boolean usesVertices() {
        return usesVertices;
    }
}
