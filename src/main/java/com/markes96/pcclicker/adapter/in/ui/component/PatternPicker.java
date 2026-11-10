package com.markes96.pcclicker.adapter.in.ui.component;

import com.markes96.pcclicker.adapter.in.ui.format.SettingsDescriber;
import com.markes96.pcclicker.adapter.in.ui.preview.PatternThumbnail;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import java.util.EnumMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

/** Visual selector of movement patterns: one tile per pattern with a drawing of its trajectory. */
public final class PatternPicker extends GridPane {

    private final Map<MovementPattern, ToggleButton> tiles = new EnumMap<>(MovementPattern.class);

    public PatternPicker(ObjectProperty<MovementPattern> selected, SettingsDescriber describer) {
        getStyleClass().add("pattern-picker");
        setHgap(8);

        final ToggleGroup group = new ToggleGroup();
        final MovementPattern[] patterns = MovementPattern.values();
        for (int column = 0; column < patterns.length; column++) {
            final MovementPattern pattern = patterns[column];
            final ToggleButton tile = new ToggleButton(describer.patternName(pattern),
                    new PatternThumbnail(pattern, Theme.VIOLET_BRIGHT));
            tile.getStyleClass().add("pattern-tile");
            tile.setContentDisplay(ContentDisplay.TOP);
            tile.setAlignment(Pos.CENTER);
            tile.setMaxWidth(Double.MAX_VALUE);
            tile.setToggleGroup(group);
            tile.setUserData(pattern);
            final Tooltip tooltip = new Tooltip(describer.patternDescription(pattern));
            tooltip.setShowDelay(Duration.millis(350));
            tile.setTooltip(tooltip);
            tiles.put(pattern, tile);

            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100.0 / patterns.length);
            constraints.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(constraints);
            add(tile, column, 0);
        }

        group.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null) {
                group.selectToggle(previous); // a pattern is always selected
            } else {
                selected.set((MovementPattern) current.getUserData());
            }
        });
        selected.addListener((observable, previous, current) -> select(group, current));
        select(group, selected.get());
    }

    private void select(ToggleGroup group, MovementPattern pattern) {
        final Toggle tile = tiles.get(pattern);
        if (group.getSelectedToggle() != tile) {
            group.selectToggle(tile);
        }
    }
}
