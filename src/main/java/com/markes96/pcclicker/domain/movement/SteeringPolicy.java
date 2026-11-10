package com.markes96.pcclicker.domain.movement;

/**
 * Decides how the heading of a {@link FreeMotionPath} evolves. Headings are angles in radians in
 * screen space (0 points right, positive angles turn clockwise because the y axis points down).
 */
public interface SteeringPolicy {

    /** Heading to use after a regular step. */
    double onStep(double heading);

    /** Heading to use after the path has been reflected by an edge of the area. */
    double onBounce(double reflectedHeading);
}
