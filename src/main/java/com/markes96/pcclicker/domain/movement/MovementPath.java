package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.geometry.ScreenPoint;

/** Endless sequence of pointer positions. Each call returns the next position to move to. */
@FunctionalInterface
public interface MovementPath {

    ScreenPoint next();
}
