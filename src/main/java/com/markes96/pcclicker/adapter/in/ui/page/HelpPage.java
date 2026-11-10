package com.markes96.pcclicker.adapter.in.ui.page;

import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.component.KeyCap;
import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.component.PageHeader;
import com.markes96.pcclicker.adapter.in.ui.format.SettingsDescriber;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.preview.PatternThumbnail;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.adapter.in.ui.window.Page;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.IntRange;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.control.Hotkey;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;

/**
 * Built-in guide. Every value shown (hotkeys, limits, countdown) comes from the domain or the
 * configuration, so the help can never drift from the actual behaviour.
 */
public final class HelpPage implements Page {

    private final UiContext context;
    private final Messages messages;
    private final SettingsDescriber describer;

    public HelpPage(UiContext context, SettingsDescriber describer) {
        this.context = Objects.requireNonNull(context, "context");
        this.messages = context.messages();
        this.describer = Objects.requireNonNull(describer, "describer");
    }

    @Override
    public String title() {
        return messages.get("nav.help");
    }

    @Override
    public Ikon icon() {
        return MaterialDesignH.HELP_CIRCLE_OUTLINE;
    }

    @Override
    public Node view() {
        final VBox column = new VBox(20,
                PageHeader.of(messages.get("help.eyebrow"), messages.get("help.title"), messages.get("help.subtitle")),
                section(messages.get("help.steps.title"), steps()),
                twoColumns(
                        section(messages.get("help.hotkeys.title"), hotkeys()),
                        section(messages.get("help.limits.title"), limits())),
                section(messages.get("help.patterns.title"), patterns()));
        column.getStyleClass().add("page-column");

        final ScrollPane scroll = new ScrollPane(column);
        scroll.getStyleClass().add("page-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private Node steps() {
        final GridPane grid = evenGrid(4);
        final List<Step> steps = List.of(
                new Step(messages.get("help.step1.title"), messages.get("help.step1.text")),
                new Step(messages.get("help.step2.title"), messages.get("help.step2.text")),
                new Step(messages.get("help.step3.title"),
                        messages.format("help.step3.text", context.countdownSeconds())),
                new Step(messages.get("help.step4.title"), messages.get("help.step4.text")));
        for (int i = 0; i < steps.size(); i++) {
            final VBox card = new VBox(8,
                    Nodes.label(String.valueOf(i + 1), "step-number"),
                    Nodes.label(steps.get(i).title(), "step-title"),
                    Nodes.paragraph(steps.get(i).text(), "step-text"));
            card.getStyleClass().add("step-card");
            grid.add(card, i, 0);
        }
        return grid;
    }

    private Node hotkeys() {
        final VBox list = new VBox(10);
        for (Hotkey hotkey : Hotkey.values()) {
            final HBox row = new HBox(12,
                    KeyCap.of(context.keyBindings().keyFor(hotkey)),
                    Nodes.label(messages.get("hotkey." + hotkey.name()), "help-row-text"));
            row.setAlignment(Pos.CENTER_LEFT);
            list.getChildren().add(row);
        }
        list.getChildren().add(Nodes.paragraph(messages.get("help.hotkeys.text"), "help-note"));
        return list;
    }

    private Node limits() {
        final VBox list = new VBox(8,
                limit(messages.get("help.limits.clickRate"), ClickRate.RANGE),
                limit(messages.get("help.limits.radius"), Radius.RANGE),
                limit(messages.get("help.limits.vertices"), VertexCount.RANGE),
                limit(messages.get("help.limits.duration"), TimeLimit.MINUTES_RANGE),
                Nodes.paragraph(messages.get("help.limits.durationOff"), "help-note"));
        return list;
    }

    private Node limit(String name, IntRange range) {
        final HBox row = new HBox(12,
                Nodes.label(name, "help-row-text"),
                Nodes.spacer(),
                Nodes.label(messages.format("field.range", messages.number(range.min()), messages.number(range.max())),
                        "mono-value"));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("limit-row");
        return row;
    }

    private Node patterns() {
        final GridPane grid = evenGrid(2);
        final MovementPattern[] patterns = MovementPattern.values();
        for (int i = 0; i < patterns.length; i++) {
            final MovementPattern pattern = patterns[i];
            final VBox texts = new VBox(4,
                    Nodes.label(describer.patternName(pattern), "step-title"),
                    Nodes.paragraph(describer.patternDescription(pattern), "step-text"));
            HBox.setHgrow(texts, Priority.ALWAYS);
            final HBox item = new HBox(14, new PatternThumbnail(pattern, Theme.VIOLET_BRIGHT), texts);
            item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("pattern-help");
            grid.add(item, i % 2, i / 2);
        }
        return grid;
    }

    private static VBox section(String title, Node content) {
        final VBox section = new VBox(12, Nodes.label(title, "section-title"), content);
        section.getStyleClass().add("help-section");
        return section;
    }

    private static Node twoColumns(VBox left, VBox right) {
        final GridPane grid = evenGrid(2);
        grid.setHgap(16);
        left.setMaxHeight(Double.MAX_VALUE);
        right.setMaxHeight(Double.MAX_VALUE);
        grid.add(left, 0, 0);
        grid.add(right, 1, 0);
        return grid;
    }

    private record Step(String title, String text) {
    }

    private static GridPane evenGrid(int columns) {
        final GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        for (int i = 0; i < columns; i++) {
            final ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100.0 / columns);
            constraints.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(constraints);
        }
        return grid;
    }
}
