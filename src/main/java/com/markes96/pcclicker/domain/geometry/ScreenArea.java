package com.markes96.pcclicker.domain.geometry;

/** Rectangle of pixels, typically one monitor. Bounds are inclusive. */
public record ScreenArea(int x, int y, int width, int height) {

    public ScreenArea {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Area must not be empty: " + width + "x" + height);
        }
    }

    public int minX() {
        return x;
    }

    public int maxX() {
        return x + width - 1;
    }

    public int minY() {
        return y;
    }

    public int maxY() {
        return y + height - 1;
    }

    public boolean contains(ScreenPoint point) {
        return point.x() >= minX() && point.x() <= maxX() && point.y() >= minY() && point.y() <= maxY();
    }

    public ScreenPoint center() {
        return new ScreenPoint(x + width / 2, y + height / 2);
    }
}
