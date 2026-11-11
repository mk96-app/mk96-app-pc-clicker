package com.markes96.pcclicker.adapter.in.ui.format;

import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.settings.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Short, human-readable descriptions of the session settings, used by summaries and the HUD. */
public final class SettingsDescriber {

    /** Middle dot used to join the parts of a description. */
    public static final String SEPARATOR = " \u00B7 ";

    private static final char NO_BREAK_SPACE = '\u00A0';

    private final Messages messages;

    public SettingsDescriber(Messages messages) {
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    /** Rate first, then only the options that differ from a plain steady left click. */
    public String click(ClickSettings settings) {
        if (!settings.enabled()) {
            return messages.get("summary.off");
        }
        final List<String> parts = new ArrayList<>(5);
        parts.add(messages.format("summary.clickRate", settings.rate().clicksPerSecond()));
        if (settings.button() != MouseButton.LEFT) {
            parts.add(messages.get("summary.button." + settings.button().name()));
        }
        if (settings.type() == ClickType.DOUBLE) {
            parts.add(messages.get("summary.doubleClick"));
        }
        if (!settings.variation().isNone()) {
            parts.add(messages.format("summary.variation", settings.variation().percent()));
        }
        settings.limit().clicks().ifPresent(limit ->
                parts.add(messages.format("summary.clickLimit", messages.number(limit))));
        return join(parts);
    }

    public String buttonName(MouseButton button) {
        return messages.get("button." + button.name());
    }

    public String clickTypeName(ClickType type) {
        return messages.get("clickType." + type.name());
    }

    public String presetName(Preset preset) {
        return messages.get("preset." + preset.name());
    }

    public String hotkeyName(Hotkey hotkey) {
        return messages.get("hotkey." + hotkey.name());
    }

    public String movement(MovementSettings settings) {
        if (!settings.enabled()) {
            return messages.get("summary.off");
        }
        final MovementPattern pattern = settings.pattern();
        final List<String> parts = new ArrayList<>(3);
        parts.add(patternName(pattern));
        if (pattern.usesVertices()) {
            parts.add(messages.format("summary.vertices", settings.vertices().value()));
        }
        if (pattern.usesRadius()) {
            parts.add(messages.format("summary.radius", settings.radius().pixels()));
        }
        return join(parts);
    }

    /**
     * Joins description parts with {@link #SEPARATOR}. Spaces inside each part become non-breaking,
     * so a long description can only wrap between parts ("radius 60 px" never splits).
     */
    public static String join(List<String> parts) {
        return parts.stream()
                .map(part -> part.replace(' ', NO_BREAK_SPACE))
                .collect(Collectors.joining(SEPARATOR));
    }

    public String duration(TimeLimit limit) {
        return limit.duration().map(DurationFormat::human).orElseGet(() -> messages.get("summary.unlimited"));
    }

    public String patternName(MovementPattern pattern) {
        return messages.get("pattern." + pattern.name());
    }

    public String patternDescription(MovementPattern pattern) {
        return messages.get("pattern." + pattern.name() + ".description");
    }
}
