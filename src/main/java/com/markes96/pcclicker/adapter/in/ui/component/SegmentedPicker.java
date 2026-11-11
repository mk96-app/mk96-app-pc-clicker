package com.markes96.pcclicker.adapter.in.ui.component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/**
 * Row of joined buttons to pick one value of an enum, bound both ways to a property. One option is
 * always selected.
 */
public final class SegmentedPicker<E extends Enum<E>> extends HBox {

    private final Map<E, ToggleButton> segments;
    private final ToggleGroup group = new ToggleGroup();

    public SegmentedPicker(Class<E> type, ObjectProperty<E> selected, Function<E, String> text) {
        Objects.requireNonNull(selected, "selected");
        segments = new EnumMap<>(type);
        getStyleClass().add("segmented");
        setAlignment(Pos.CENTER_LEFT);

        final E[] values = type.getEnumConstants();
        for (int index = 0; index < values.length; index++) {
            final E value = values[index];
            final ToggleButton segment = new ToggleButton(text.apply(value));
            segment.getStyleClass().add("segment");
            if (index == 0) {
                segment.getStyleClass().add("segment-first");
            }
            if (index == values.length - 1) {
                segment.getStyleClass().add("segment-last");
            }
            segment.setToggleGroup(group);
            segment.setUserData(value);
            segment.setFocusTraversable(false);
            segments.put(value, segment);
            getChildren().add(segment);
        }

        group.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null) {
                group.selectToggle(previous); // a value is always selected
            } else {
                selected.set(type.cast(current.getUserData()));
            }
        });
        selected.addListener((observable, previous, current) -> select(current));
        select(selected.get());
    }

    private void select(E value) {
        final Toggle segment = segments.get(value);
        if (group.getSelectedToggle() != segment) {
            group.selectToggle(segment);
        }
    }
}
