package com.markes96.pcclicker.domain.geometry;

/** A pixel on the screen, in the same coordinate space the operating system uses for the pointer. */
public record ScreenPoint(int x, int y) {

    public ScreenPoint translate(int dx, int dy) {
        return new ScreenPoint(x + dx, y + dy);
    }
}
