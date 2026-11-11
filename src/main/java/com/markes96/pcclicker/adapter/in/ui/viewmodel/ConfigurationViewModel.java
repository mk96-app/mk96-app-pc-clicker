package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.domain.configuration.ClickLimit;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.settings.SessionSettings;
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
    /** Clicks proposed when the user turns the click limit on. */
    public static final int DEFAULT_CLICK_LIMIT = 100;

    private final BooleanProperty clickEnabled = new SimpleBooleanProperty(this, "clickEnabled", false);
    private final IntegerProperty clickRate =
            new SimpleIntegerProperty(this, "clickRate", ClickRate.DEFAULT.clicksPerSecond());
    private final ObjectProperty<MouseButton> clickButton = new SimpleObjectProperty<>(this, "clickButton", MouseButton.LEFT);
    private final ObjectProperty<ClickType> clickType = new SimpleObjectProperty<>(this, "clickType", ClickType.SINGLE);
    private final IntegerProperty rateVariation =
            new SimpleIntegerProperty(this, "rateVariation", RateVariation.NONE.percent());
    private final BooleanProperty clickLimitEnabled = new SimpleBooleanProperty(this, "clickLimitEnabled", false);
    private final IntegerProperty clickLimit = new SimpleIntegerProperty(this, "clickLimit", DEFAULT_CLICK_LIMIT);

    private final BooleanProperty movementEnabled = new SimpleBooleanProperty(this, "movementEnabled", false);
    private final ObjectProperty<MovementPattern> pattern =
            new SimpleObjectProperty<>(this, "pattern", MovementPattern.RANDOM);
    private final IntegerProperty radius = new SimpleIntegerProperty(this, "radius", Radius.DEFAULT.pixels());
    private final IntegerProperty vertices = new SimpleIntegerProperty(this, "vertices", VertexCount.DEFAULT.value());

    private final BooleanProperty timeLimitEnabled = new SimpleBooleanProperty(this, "timeLimitEnabled", false);
    private final IntegerProperty timeLimitMinutes =
            new SimpleIntegerProperty(this, "timeLimitMinutes", DEFAULT_TIME_LIMIT_MINUTES);

    private final BooleanBinding canStart = clickEnabled.or(movementEnabled);
    private final BooleanBinding pristine = Bindings.createBooleanBinding(this::isPristine, settings());
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

    public ObjectProperty<MouseButton> clickButton() {
        return clickButton;
    }

    public ObjectProperty<ClickType> clickType() {
        return clickType;
    }

    /** Percentage of random deviation of the time between clicks. */
    public IntegerProperty rateVariation() {
        return rateVariation;
    }

    public BooleanProperty clickLimitEnabled() {
        return clickLimitEnabled;
    }

    public IntegerProperty clickLimit() {
        return clickLimit;
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
            clickEnabled, clickRate, clickButton, clickType, rateVariation, clickLimitEnabled, clickLimit,
            movementEnabled, pattern, radius, vertices, timeLimitEnabled, timeLimitMinutes
        };
    }

    public ClickSettings clickSettings() {
        return new ClickSettings(
                clickEnabled.get(),
                new ClickRate(ClickRate.RANGE.clamp(clickRate.get())),
                clickButton.get(),
                clickType.get(),
                new RateVariation(RateVariation.RANGE.clamp(rateVariation.get())),
                clickLimitEnabled.get()
                        ? ClickLimit.ofClicks(ClickLimit.RANGE.clamp(clickLimit.get()))
                        : ClickLimit.unlimited());
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

    /** The form as domain settings, valid even when no action is on. */
    public SessionSettings toSettings() {
        return new SessionSettings(clickSettings(), movementSettings(), timeLimit());
    }

    /**
     * @throws com.markes96.pcclicker.domain.configuration.InvalidSessionConfigurationException when
     *         no action is on; check {@link #canStart()} first
     */
    public SessionConfiguration toConfiguration() {
        return toSettings().toConfiguration();
    }

    /**
     * Fills the form with saved settings. A limit that is off keeps the number last typed in the
     * form, so turning it back on proposes the same value.
     */
    /** True when the form is exactly as on the very first launch, so there is nothing to clear. */
    public BooleanBinding pristine() {
        return pristine;
    }

    /**
     * Turns everything off and puts every value back to its default, including the numbers a
     * disabled limit remembers.
     */
    public void reset() {
        load(SessionSettings.defaults());
        timeLimitMinutes.set(DEFAULT_TIME_LIMIT_MINUTES);
        clickLimit.set(DEFAULT_CLICK_LIMIT);
    }

    private boolean isPristine() {
        return toSettings().equals(SessionSettings.defaults())
                && timeLimitMinutes.get() == DEFAULT_TIME_LIMIT_MINUTES
                && clickLimit.get() == DEFAULT_CLICK_LIMIT;
    }

    public void load(SessionSettings settings) {
        final ClickSettings click = settings.click();
        clickEnabled.set(click.enabled());
        clickRate.set(click.rate().clicksPerSecond());
        clickButton.set(click.button());
        clickType.set(click.type());
        rateVariation.set(click.variation().percent());
        clickLimitEnabled.set(click.limit().isLimited());
        click.limit().clicks().ifPresent(clicks -> clickLimit.set(Math.toIntExact(clicks)));

        final MovementSettings movement = settings.movement();
        movementEnabled.set(movement.enabled());
        pattern.set(movement.pattern());
        radius.set(movement.radius().pixels());
        vertices.set(movement.vertices().value());

        timeLimitEnabled.set(settings.timeLimit().isLimited());
        settings.timeLimit().duration().ifPresent(limit -> timeLimitMinutes.set(Math.toIntExact(limit.toMinutes())));
    }
}
