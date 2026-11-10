package com.markes96.pcclicker.domain.configuration;

/** Radius, in screen pixels, of the shapes drawn by the pointer. */
public record Radius(int pixels) {

    public static final IntRange RANGE = new IntRange(1, 100);
    public static final Radius DEFAULT = new Radius(10);

    public Radius {
        RANGE.require(pixels, "Radius");
    }
}
