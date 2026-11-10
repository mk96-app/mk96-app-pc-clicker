package com.markes96.pcclicker.support;

import com.markes96.pcclicker.application.port.out.MouseDriver;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.ArrayList;
import java.util.List;

/** Records what the application asks the mouse to do. Can be told to fail like a real OS would. */
public final class FakeMouseDriver implements MouseDriver {

    private final List<ScreenPoint> moves = new ArrayList<>();
    private ScreenPoint position;
    private int clicks;
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
    public void click() {
        failIfAsked();
        clicks++;
    }

    public void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    public int clicks() {
        return clicks;
    }

    public List<ScreenPoint> moves() {
        return List.copyOf(moves);
    }

    private void failIfAsked() {
        if (failure != null) {
            throw failure;
        }
    }
}
