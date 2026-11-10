package com.markes96.pcclicker.adapter.in.ui.component;

import java.util.List;
import java.util.function.IntFunction;
import javafx.beans.property.IntegerProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

/** Row of shortcuts that set a numeric value in one click; the chip matching the value is highlighted. */
public final class PresetChips extends FlowPane {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    public PresetChips(List<Integer> presets, IntegerProperty target, IntFunction<String> text) {
        getStyleClass().add("preset-chips");
        setHgap(6);
        setVgap(6);
        for (int preset : presets) {
            final Button chip = new Button(text.apply(preset));
            chip.getStyleClass().add("chip");
            chip.setFocusTraversable(false);
            chip.setOnAction(event -> target.set(preset));
            chip.pseudoClassStateChanged(SELECTED, target.get() == preset);
            target.addListener((observable, previous, current) ->
                    chip.pseudoClassStateChanged(SELECTED, current.intValue() == preset));
            getChildren().add(chip);
        }
    }
}
