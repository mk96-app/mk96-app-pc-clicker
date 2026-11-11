package com.markes96.pcclicker.adapter.in.ui.component;

import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.domain.configuration.IntRange;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

/**
 * Integer input made of − / value / + with a unit. Accepts only digits, clamps to its range on
 * commit (Enter or focus loss), steps with the arrow keys and the mouse wheel, and repeats while
 * the buttons are held down.
 */
public final class NumberStepper extends HBox {

    private static final int FAST_STEPS_PER_RANGE = 50;
    private static final int MIN_COLUMNS = 3;

    private final IntRange range;
    private final IntegerProperty value;
    private final TextField field = new TextField();

    public NumberStepper(IntRange range, IntegerProperty value, String unit, Messages messages) {
        this.range = Objects.requireNonNull(range, "range");
        this.value = Objects.requireNonNull(value, "value");
        getStyleClass().add("stepper");
        setAlignment(Pos.CENTER_LEFT);

        final int fastMultiplier = Math.max(1, (range.max() - range.min()) / FAST_STEPS_PER_RANGE);
        final Button decrease = stepButton(MaterialDesignM.MINUS, messages.get("field.decrease"));
        final Button increase = stepButton(MaterialDesignP.PLUS, messages.get("field.increase"));
        PressAndHold.install(decrease, multiplier -> stepBy(-multiplier), fastMultiplier);
        PressAndHold.install(increase, multiplier -> stepBy(multiplier), fastMultiplier);
        decrease.disableProperty().bind(value.lessThanOrEqualTo(range.min()));
        increase.disableProperty().bind(value.greaterThanOrEqualTo(range.max()));

        configureField(messages);

        getChildren().addAll(decrease, field, increase);
        if (unit != null && !unit.isBlank()) {
            final Label unitLabel = Nodes.label(unit, "stepper-unit");
            unitLabel.setMinWidth(Label.USE_PREF_SIZE);
            getChildren().add(unitLabel);
        }
    }

    private void configureField(Messages messages) {
        field.getStyleClass().add("stepper-field");
        field.setAlignment(Pos.CENTER);
        // Room for the largest value, and never less than three digits: bold digits are wide.
        field.setPrefColumnCount(Math.max(MIN_COLUMNS, String.valueOf(range.max()).length()));
        field.setMinWidth(TextField.USE_PREF_SIZE);
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,9}") ? change : null));
        field.setText(String.valueOf(value.get()));
        field.setAccessibleHelp(messages.format("field.range", range.min(), range.max()));
        field.setTooltip(new Tooltip(messages.format("field.range", range.min(), range.max())));

        value.addListener((observable, previous, current) -> field.setText(String.valueOf(current.intValue())));
        field.setOnAction(event -> commit());
        field.focusedProperty().addListener((observable, wasFocused, focused) -> {
            if (!focused) {
                commit();
            }
        });
        field.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                stepBy(1);
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                stepBy(-1);
                event.consume();
            }
        });
        field.setOnScroll(event -> {
            if (event.getDeltaY() != 0) {
                stepBy(event.getDeltaY() > 0 ? 1 : -1);
                event.consume();
            }
        });
    }

    private void commit() {
        final String text = field.getText();
        if (text == null || text.isEmpty()) {
            field.setText(String.valueOf(value.get()));
            return;
        }
        final long typed = Long.parseLong(text);
        final int clamped = (int) Math.max(range.min(), Math.min(range.max(), typed));
        value.set(clamped);
        field.setText(String.valueOf(clamped)); // the listener does not fire when the value is unchanged
    }

    private void stepBy(int delta) {
        value.set(range.clamp(value.get() + delta));
    }

    private static Button stepButton(Ikon ikon, String description) {
        final Button button = new Button(null, Icons.of(ikon, 16));
        button.getStyleClass().add("stepper-button");
        button.setFocusTraversable(false);
        button.setAccessibleText(description);
        return button;
    }
}
