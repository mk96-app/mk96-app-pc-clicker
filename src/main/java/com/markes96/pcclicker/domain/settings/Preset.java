package com.markes96.pcclicker.domain.settings;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;

/**
 * Profiles that come with the application: ready to use, never deleted or overwritten. They have
 * no name here so each language can name them; users start from one and save their own variant.
 */
public enum Preset {

    /** Keeps the session alive: small circles, no clicks. */
    ANTI_AFK(new SessionSettings(
            ClickSettings.disabled(),
            MovementSettings.enabled(MovementPattern.CIRCLE, new Radius(15), VertexCount.DEFAULT),
            TimeLimit.unlimited())),

    /** Ten left clicks per second, perfectly regular. */
    STEADY_CLICK(clicking(ClickSettings.enabled(new ClickRate(10)))),

    /** Seven clicks per second with an irregular, person-like pace. */
    HUMAN_CLICK(clicking(ClickSettings.enabled(new ClickRate(7)).withVariation(new RateVariation(35)))),

    /** As fast as most programs register clicks. */
    FAST_CLICK(clicking(ClickSettings.enabled(new ClickRate(50)))),

    /** One double click every second, e.g. to open things repeatedly. */
    DOUBLE_CLICK(clicking(ClickSettings.enabled(new ClickRate(1)).withType(ClickType.DOUBLE))),

    /** Two right clicks per second. */
    RIGHT_CLICK(clicking(ClickSettings.enabled(new ClickRate(2)).withButton(MouseButton.RIGHT))),

    /** An hour of irregular clicking that stops by itself. */
    ONE_HOUR_FARM(new SessionSettings(
            ClickSettings.enabled(new ClickRate(12)).withVariation(new RateVariation(20)),
            MovementSettings.disabled(),
            TimeLimit.ofMinutes(60)));

    private final SessionSettings settings;

    Preset(SessionSettings settings) {
        this.settings = settings;
    }

    public SessionSettings settings() {
        return settings;
    }

    private static SessionSettings clicking(ClickSettings click) {
        return new SessionSettings(click, MovementSettings.disabled(), TimeLimit.unlimited());
    }
}
