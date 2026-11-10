package com.markes96.pcclicker.domain.configuration;

import java.util.Objects;

/** Whether the session moves the pointer and which shape it follows. */
public record MovementSettings(boolean enabled, MovementPattern pattern, Radius radius, VertexCount vertices) {

    public MovementSettings {
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(radius, "radius");
        Objects.requireNonNull(vertices, "vertices");
    }

    public static MovementSettings enabled(MovementPattern pattern, Radius radius, VertexCount vertices) {
        return new MovementSettings(true, pattern, radius, vertices);
    }

    public static MovementSettings disabled() {
        return new MovementSettings(false, MovementPattern.RANDOM, Radius.DEFAULT, VertexCount.DEFAULT);
    }
}
