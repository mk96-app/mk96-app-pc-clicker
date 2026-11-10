package com.markes96.pcclicker.adapter.out.system;

import com.markes96.pcclicker.application.port.out.MouseDriver;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;

/** Drives the real pointer through {@link Robot}. */
public final class AwtMouseDriver implements MouseDriver {

    private final Robot robot;

    private AwtMouseDriver(Robot robot) {
        this.robot = robot;
    }

    /**
     * @throws MouseUnavailableException if the system does not allow controlling the pointer
     *                                   (headless environments, missing permissions)
     */
    public static AwtMouseDriver create() {
        try {
            final Robot robot = new Robot();
            robot.setAutoWaitForIdle(false);
            robot.setAutoDelay(0);
            return new AwtMouseDriver(robot);
        } catch (AWTException | SecurityException failure) {
            throw new MouseUnavailableException(failure);
        }
    }

    @Override
    public ScreenPoint position() {
        final PointerInfo pointer = MouseInfo.getPointerInfo();
        if (pointer == null) {
            throw new IllegalStateException("The pointer position is not available");
        }
        final Point location = pointer.getLocation();
        return new ScreenPoint(location.x, location.y);
    }

    @Override
    public void moveTo(ScreenPoint point) {
        robot.mouseMove(point.x(), point.y());
    }

    @Override
    public void click() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /** The operating system does not let this process control the pointer. */
    public static final class MouseUnavailableException extends IllegalStateException {

        MouseUnavailableException(Throwable cause) {
            super("The system does not allow controlling the mouse", cause);
        }
    }
}
