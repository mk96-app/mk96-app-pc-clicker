package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;

/** Closed figure that can be traced by the pointer. */
public interface Shape {

    /** Points spread along a full lap of the figure, in drawing order. Never empty. */
    List<ScreenPoint> outline();
}
