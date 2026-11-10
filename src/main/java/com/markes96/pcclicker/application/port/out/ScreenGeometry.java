package com.markes96.pcclicker.application.port.out;

import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;

/** Knows the monitors layout. */
@FunctionalInterface
public interface ScreenGeometry {

    /** Bounds of the monitor showing {@code point}, or of the primary monitor if none does. */
    ScreenArea areaContaining(ScreenPoint point);
}
