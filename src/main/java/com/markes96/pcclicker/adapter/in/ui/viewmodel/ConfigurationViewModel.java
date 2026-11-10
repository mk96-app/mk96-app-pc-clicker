package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * State of the session form. Holds raw, bindable values for the controls and turns them into a
 * valid domain {@link SessionConfiguration}; numbers are clamped to the domain ranges so a stray
 * value can never break the start of a session.
 */
public final class ConfigurationViewModel {

    /** Minutes proposed when the user turns the time limit on. */
    public static final int DEFAULT_TIME_LIMIT_MINUTES = 10;

    private final BooleanProperty clickEnabled = new SimpleBooleanProperty(this, "clickEnabled", false);
    private final IntegerProperty clickRate =
            new SimpleIntegerProperty(this, "clickRate", ClickRate.DEFAULT.clicksPerSecond());

    private final BooleanProperty movementEnabled = new SimpleBooleanProperty(this, "movementEnabled", false);
    private final ObjectProperty<MovementPattern> pattern =
            new SimpleObjectProperty<>(this, "pattern", MovementPattern.RANDOM);
    private final IntegerProperty radius = new SimpleIntegerProperty(this, "radius", Radius.DEFAULT.pixels());
    private final IntegerProperty vertices = new SimpleIntegerProperty(this, "vertices", VertexCount.DEFAULT.value());

    private final BooleanProperty timeLimitEnabled = new SimpleBooleanProperty(this, "timeLimitEnabled", false);
    private final IntegerProperty timeLimitMinutes =
            new SimpleIntegerProperty(this, "timeLimitMinutes", DEFAULT_TIME_LIMIT_MINUTES);

    private final BooleanBinding canStart = clickEnabled.or(movementEnabled);
    private final BooleanBinding radiusApplies =
            Bindings.createBooleanBinding(() -> pattern.get().usesRadius(), pattern);
    private final BooleanBinding verticesApply =
            Bindings.createBooleanBinding(() -> pattern.get().usesVertices(), pattern);

    public BooleanProperty clickEnabled() {
        return clickEnabled;
    }

    public IntegerProperty clickRate() {
        return clickRate;
    }

    public BooleanProperty movementEnabled() {
        return movementEnabled;
    }

    public ObjectProperty<MovementPattern> pattern() {
        return pattern;
    }

    public IntegerProperty radius() {
        return radius;
    }

    public IntegerProperty vertices() {
        return vertices;
    }

    public BooleanProperty timeLimitEnabled() {
        return timeLimitEnabled;
    }

    public IntegerProperty timeLimitMinutes() {
        return timeLimitMinutes;
    }

    /** True when at least one action is on. */
    public BooleanBinding canStart() {
        return canStart;
    }

    /** Whether the selected pattern uses a radius. */
    public BooleanBinding radiusApplies() {
        return radiusApplies;
    }

    /** Whether the selected pattern uses a vertex count. */
    public BooleanBinding verticesApply() {
        return verticesApply;
    }

    /** Every editable setting, to observe the form as a whole (previews, summaries). */
    public Observable[] settings() {
        return new Observable[] {
            clickEnabled, clickRate, movementEnabled, pattern, radius, vertices, timeLimitEnabled, timeLimitMinutes
        };
    }

    public ClickSettings clickSettings() {
        return new ClickSettings(clickEnabled.get(), new ClickRate(ClickRate.RANGE.clamp(clickRate.get())));
    }

    public MovementSettings movementSettings() {
        return new MovementSettings(
                movementEnabled.get(),
                pattern.get(),
                new Radius(Radius.RANGE.clamp(radius.get())),
                new VertexCount(VertexCount.RANGE.clamp(vertices.get())));
    }

    public TimeLimit timeLimit() {
        return timeLimitEnabled.get()
                ? TimeLimit.ofMinutes(TimeLimit.MINUTES_RANGE.clamp(timeLimitMinutes.get()))
                : TimeLimit.unlimited();
    }

    /**
     * @throws com.markes96.pcclicker.domain.configuration.InvalidSessionConfigurationException when
     *         no action is on; check {@link #canStart()} first
     */
    public SessionConfiguration toConfiguration() {
        return new SessionConfiguration(clickSettings(), movementSettings(), timeLimit());
    }
}
