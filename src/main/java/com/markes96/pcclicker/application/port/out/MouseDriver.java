package com.markes96.pcclicker.application.port.out;

import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;

/**
 * Controls the system pointer. Implementations may throw any {@link RuntimeException} when the
 * operating system refuses the operation; the application turns that into a failed session.
 */
public interface MouseDriver {

    ScreenPoint position();

    void moveTo(ScreenPoint point);

    /** Clicks {@code button} where the pointer is: once, or twice in a row for a double click. */
    void click(MouseButton button, ClickType type);
}
