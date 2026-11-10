package com.markes96.pcclicker.domain.configuration;

/** Number of vertices of the polygon drawn by the pointer. */
public record VertexCount(int value) {

    public static final IntRange RANGE = new IntRange(2, 300);
    public static final VertexCount DEFAULT = new VertexCount(3);

    public VertexCount {
        RANGE.require(value, "Vertex count");
    }
}
