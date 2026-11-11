package com.markes96.pcclicker.support;

import com.markes96.pcclicker.application.port.out.MouseDriver;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.ArrayList;
import java.util.List;

/** Records what the application asks the mouse to do. Can be told to fail like a real OS would. */
public final class FakeMouseDriver implements MouseDriver {

    private final List<ScreenPoint> moves = new ArrayList<>();
    private ScreenPoint position;
    private final List<Click> clicks = new ArrayList<>();
    private RuntimeException failure;

    public FakeMouseDriver(ScreenPoint position) {
        this.position = position;
    }

    @Override
    public ScreenPoint position() {
        failIfAsked();
        return position;
    }

    @Override
    public void moveTo(ScreenPoint point) {
        failIfAsked();
        moves.add(point);
        position = point;
    }

    @Override
    public void click(MouseButton button, ClickType type) {
        failIfAsked();
        clicks.add(new Click(button, type));
    }

    public void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    /** Number of clicks requested, a double click counting as one. */
    public int clicks() {
        return clicks.size();
    }

    public List<Click> clickLog() {
        return List.copyOf(clicks);
    }

    public List<ScreenPoint> moves() {
        return List.copyOf(moves);
    }

    /** One click as the application asked for it. */
    public record Click(MouseButton button, ClickType type) {
    }

    private void failIfAsked() {
        if (failure != null) {
            throw failure;
        }
    }
}
