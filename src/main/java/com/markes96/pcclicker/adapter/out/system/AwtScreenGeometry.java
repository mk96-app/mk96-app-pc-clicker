package com.markes96.pcclicker.adapter.out.system;

import com.markes96.pcclicker.application.port.out.ScreenGeometry;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Monitor layout read from AWT. Read on every call so plugging a monitor in is picked up. */
public final class AwtScreenGeometry implements ScreenGeometry {

    private final Supplier<List<ScreenArea>> monitors;

    /** @param monitors supplies the monitors' bounds, the primary monitor first */
    AwtScreenGeometry(Supplier<List<ScreenArea>> monitors) {
        this.monitors = Objects.requireNonNull(monitors, "monitors");
    }

    public static AwtScreenGeometry ofSystem() {
        return new AwtScreenGeometry(AwtScreenGeometry::systemMonitors);
    }

    @Override
    public ScreenArea areaContaining(ScreenPoint point) {
        final List<ScreenArea> areas = monitors.get();
        return areas.stream()
                .filter(area -> area.contains(point))
                .findFirst()
                .orElse(areas.get(0));
    }

    private static List<ScreenArea> systemMonitors() {
        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice primary = environment.getDefaultScreenDevice();
        final List<ScreenArea> areas = new ArrayList<>();
        areas.add(toArea(primary.getDefaultConfiguration().getBounds()));
        for (GraphicsDevice device : environment.getScreenDevices()) {
            if (device != primary) {
                areas.add(toArea(device.getDefaultConfiguration().getBounds()));
            }
        }
        return areas;
    }

    private static ScreenArea toArea(Rectangle bounds) {
        return new ScreenArea(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
