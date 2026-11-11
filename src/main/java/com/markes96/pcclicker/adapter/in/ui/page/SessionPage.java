package com.markes96.pcclicker.adapter.in.ui.page;

import atlantafx.base.controls.ToggleSwitch;
import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.component.FeatureCard;
import com.markes96.pcclicker.adapter.in.ui.component.FeatureCard.Accent;
import com.markes96.pcclicker.adapter.in.ui.component.KeyCap;
import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.component.NumberStepper;
import com.markes96.pcclicker.adapter.in.ui.component.PageHeader;
import com.markes96.pcclicker.adapter.in.ui.component.PatternPicker;
import com.markes96.pcclicker.adapter.in.ui.component.PresetChips;
import com.markes96.pcclicker.adapter.in.ui.component.SegmentedPicker;
import com.markes96.pcclicker.adapter.in.ui.format.DurationFormat;
import com.markes96.pcclicker.adapter.in.ui.format.SettingsDescriber;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.preview.SessionPreview;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ConfigurationViewModel;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfilesViewModel;
import com.markes96.pcclicker.adapter.in.ui.window.Notifier;
import com.markes96.pcclicker.adapter.in.ui.window.ToastLayer;
import com.markes96.pcclicker.adapter.in.ui.window.Page;
import com.markes96.pcclicker.domain.configuration.ClickLimit;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.control.Hotkey;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

/**
 * Home page: one card per capability on the left and, on the right, a live preview, a summary
 * and the start button. More capabilities fit as more cards without touching the rest.
 */
public final class SessionPage implements Page {

    private static final List<Integer> CLICK_PRESETS = List.of(1, 5, 10, 25, 50, 100);
    private static final List<Integer> MINUTE_PRESETS = List.of(5, 15, 30, 60, 120);

    private static final PseudoClass DANGER = PseudoClass.getPseudoClass("danger");
    private static final javafx.util.Duration RESET_CONFIRMATION_TIME = javafx.util.Duration.seconds(3);

    private final ConfigurationViewModel form;
    private final ProfilesViewModel profiles;
    private final UiContext context;
    private final Messages messages;
    private final SettingsDescriber describer;
    private final ObservableBooleanValue animate;
    private final Notifier notifier;
    private final Runnable onStart;

    /**
     * @param animate  whether the preview may animate (the window is visible)
     * @param notifier shows the result of saving or deleting profiles
     * @param onStart  called when the user asks to start with a valid form
     */
    public SessionPage(ConfigurationViewModel form, ProfilesViewModel profiles, UiContext context,
                       SettingsDescriber describer, ObservableBooleanValue animate, Notifier notifier,
                       Runnable onStart) {
        this.form = Objects.requireNonNull(form, "form");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.notifier = Objects.requireNonNull(notifier, "notifier");
        this.context = Objects.requireNonNull(context, "context");
        this.messages = context.messages();
        this.describer = Objects.requireNonNull(describer, "describer");
        this.animate = Objects.requireNonNull(animate, "animate");
        this.onStart = Objects.requireNonNull(onStart, "onStart");
    }

    @Override
    public String title() {
        return messages.get("nav.session");
    }

    @Override
    public Ikon icon() {
        return MaterialDesignT.TUNE_VARIANT;
    }

    @Override
    public Node view() {
        final HBox layout = new HBox(configurationColumn(), summaryPanel());
        layout.getStyleClass().add("session-page");
        return layout;
    }

    // --- Left column: what to automate ---

    private Node configurationColumn() {
        final VBox column = new VBox(16);
        column.getStyleClass().add("page-column");
        column.getChildren().add(headerWithReset(PageHeader.of(
                messages.get("config.eyebrow"), messages.get("config.title"), messages.get("config.subtitle"))));
        if (!context.hotkeysAvailable()) {
            column.getChildren().add(banner(messages.get("hotkeys.unavailable")));
        }
        column.getChildren().add(new ProfileBar(profiles, messages, notifier));
        column.getChildren().addAll(clickCard(), movementCard(), durationCard());

        final ScrollPane scroll = new ScrollPane(column);
        scroll.getStyleClass().add("page-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        HBox.setHgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    /** Page title with the button that clears the whole form, asking for a second click first. */
    private Node headerWithReset(Node header) {
        final BooleanProperty confirming = new SimpleBooleanProperty(this, "confirmingReset");
        final PauseTransition timeout = new PauseTransition(RESET_CONFIRMATION_TIME);
        timeout.setOnFinished(event -> confirming.set(false));

        final Button reset = new Button();
        reset.getStyleClass().addAll("profile-button", "reset-button");
        reset.setMinWidth(Button.USE_PREF_SIZE);
        reset.graphicProperty().bind(Bindings.when(confirming)
                .then(Icons.of(MaterialDesignA.ALERT_OUTLINE, 16))
                .otherwise(Icons.of(MaterialDesignB.BROOM, 16)));
        reset.textProperty().bind(Bindings.when(confirming)
                .then(messages.get("reset.confirm"))
                .otherwise(messages.get("reset.button")));
        confirming.addListener((observable, was, now) -> reset.pseudoClassStateChanged(DANGER, now));
        reset.setTooltip(new Tooltip(messages.get("reset.tooltip")));
        reset.disableProperty().bind(form.pristine().and(profiles.selected().isNull()));
        reset.setOnAction(event -> {
            if (!confirming.get()) {
                confirming.set(true);
                timeout.playFromStart();
                return;
            }
            timeout.stop();
            confirming.set(false);
            profiles.clearSelection();
            form.reset();
            notifier.notify(ToastLayer.Kind.INFO, messages.get("reset.done"), null);
        });

        HBox.setHgrow(header, Priority.ALWAYS);
        final HBox row = new HBox(16, header, reset);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node clickCard() {
        final NumberStepper rate = new NumberStepper(
                ClickRate.RANGE, form.clickRate(), messages.get("unit.perSecond"), messages);
        final PresetChips presets = new PresetChips(
                CLICK_PRESETS, form.clickRate(), value -> messages.number(value) + messages.get("unit.perSecond"));
        final VBox body = new VBox(12,
                FeatureCard.field(messages.get("click.rate"), rangeHint(ClickRate.RANGE.min(), ClickRate.RANGE.max()), rate),
                presets,
                Nodes.styled(new Region(), "card-divider"),
                FeatureCard.field(messages.get("click.button"), null,
                        new SegmentedPicker<>(MouseButton.class, form.clickButton(), describer::buttonName)),
                FeatureCard.field(messages.get("click.type"), messages.get("click.typeHint"),
                        new SegmentedPicker<>(ClickType.class, form.clickType(), describer::clickTypeName)),
                FeatureCard.field(messages.get("click.variation"), messages.get("click.variationHint"),
                        new NumberStepper(RateVariation.RANGE, form.rateVariation(), messages.get("unit.percent"),
                                messages)),
                clickLimitField());
        return new FeatureCard(Accent.VIOLET, MaterialDesignC.CURSOR_DEFAULT_CLICK_OUTLINE,
                messages.get("click.title"), messages.get("click.description"), form.clickEnabled(), body);
    }

    /** "Stop after N clicks": a switch, and the number once it is on. */
    private Node clickLimitField() {
        final ToggleSwitch toggle = new ToggleSwitch();
        toggle.selectedProperty().bindBidirectional(form.clickLimitEnabled());
        toggle.setAccessibleText(messages.get("click.limit"));
        final NumberStepper clicks = new NumberStepper(
                ClickLimit.RANGE, form.clickLimit(), messages.get("unit.clicks"), messages);
        Nodes.showWhen(clicks, form.clickLimitEnabled());
        final HBox control = new HBox(10, clicks, toggle);
        control.setAlignment(Pos.CENTER_RIGHT);
        return FeatureCard.field(messages.get("click.limit"), messages.get("click.limitHint"), control);
    }

    private Node movementCard() {
        final HBox radius = FeatureCard.field(messages.get("movement.radius"),
                rangeHint(Radius.RANGE.min(), Radius.RANGE.max()),
                new NumberStepper(Radius.RANGE, form.radius(), messages.get("unit.px"), messages));
        Nodes.showWhen(radius, form.radiusApplies());
        final HBox vertices = FeatureCard.field(messages.get("movement.vertices"),
                rangeHint(VertexCount.RANGE.min(), VertexCount.RANGE.max()),
                new NumberStepper(VertexCount.RANGE, form.vertices(), null, messages));
        Nodes.showWhen(vertices, form.verticesApply());

        final Label description = Nodes.paragraph("", "field-hint", "pattern-description");
        description.textProperty().bind(Bindings.createStringBinding(
                () -> describer.patternDescription(form.pattern().get()), form.pattern()));

        final VBox body = new VBox(12,
                Nodes.label(messages.get("movement.pattern"), "field-name"),
                new PatternPicker(form.pattern(), describer),
                description,
                vertices,
                radius);

        return new FeatureCard(Accent.BLUE, MaterialDesignC.CURSOR_MOVE,
                messages.get("movement.title"), messages.get("movement.description"), form.movementEnabled(), body);
    }

    private Node durationCard() {
        final NumberStepper minutes = new NumberStepper(
                TimeLimit.MINUTES_RANGE, form.timeLimitMinutes(), messages.get("unit.min"), messages);
        final PresetChips presets = new PresetChips(
                MINUTE_PRESETS, form.timeLimitMinutes(), value -> DurationFormat.human(Duration.ofMinutes(value)));
        final VBox body = new VBox(12,
                FeatureCard.field(messages.get("duration.minutes"),
                        rangeHint(TimeLimit.MINUTES_RANGE.min(), TimeLimit.MINUTES_RANGE.max()), minutes),
                presets);
        return new FeatureCard(Accent.YELLOW, MaterialDesignT.TIMER_OUTLINE,
                messages.get("duration.title"), messages.get("duration.description"), form.timeLimitEnabled(), body);
    }

    // --- Right panel: what will happen ---

    private Node summaryPanel() {
        final SessionPreview preview = new SessionPreview(form, messages.get("preview.empty"), animate);
        VBox.setVgrow(preview, Priority.ALWAYS);

        final VBox summary = new VBox(10,
                summaryRow(MaterialDesignC.CURSOR_DEFAULT_CLICK_OUTLINE, "accent-violet", messages.get("summary.click"),
                        () -> describer.click(form.clickSettings())),
                summaryRow(MaterialDesignC.CURSOR_MOVE, "accent-blue", messages.get("summary.movement"),
                        () -> describer.movement(form.movementSettings())),
                summaryRow(MaterialDesignT.TIMER_OUTLINE, "accent-yellow", messages.get("summary.duration"),
                        () -> describer.duration(form.timeLimit())));
        summary.getStyleClass().add("summary-list");

        final FlowPane hotkeys = new FlowPane(14, 8);
        hotkeys.getStyleClass().add("hotkey-hints");
        for (Hotkey hotkey : Hotkey.values()) {
            hotkeys.getChildren().add(KeyCap.withCaption(
                    context.preferences().keyOf(hotkey), describer.hotkeyName(hotkey)));
        }

        final Button start = new Button(messages.get("start.button"), Icons.of(MaterialDesignP.PLAY, 22));
        start.getStyleClass().add("start-button");
        start.setMaxWidth(Double.MAX_VALUE);
        start.disableProperty().bind(form.canStart().not());
        start.setOnAction(event -> onStart.run());

        final Label hint = Nodes.paragraph("", "start-hint");
        final ObservableValue<Integer> countdown = context.preferences().countdownSeconds();
        hint.textProperty().bind(Bindings.createStringBinding(() -> {
            if (!form.canStart().get()) {
                return messages.get("start.disabled");
            }
            return countdown.getValue() == 0
                    ? messages.get("start.hintImmediate")
                    : messages.format("start.hint", countdown.getValue());
        }, form.canStart(), countdown));

        final VBox panel = new VBox(16,
                Nodes.label(messages.get("summary.title"), "panel-title"),
                preview,
                summary,
                hotkeys,
                start,
                hint);
        panel.getStyleClass().add("summary-panel");
        return panel;
    }

    private Node summaryRow(Ikon ikon, String accent, String name, Supplier<String> value) {
        final Label nameLabel = Nodes.label(name, "summary-name");
        nameLabel.setMinWidth(Region.USE_PREF_SIZE);
        // Long descriptions wrap, right-aligned, instead of being cut with an ellipsis.
        final Label valueLabel = Nodes.paragraph("", "summary-value");
        valueLabel.setTextAlignment(TextAlignment.RIGHT);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(valueLabel, Priority.ALWAYS);
        // The supplier reads every setting it depends on, which keeps the lazy bindings valid.
        valueLabel.textProperty().bind(Bindings.createStringBinding(value::get, form.settings()));
        final HBox row = new HBox(10,
                Nodes.styled(new HBox(Icons.of(ikon, 16)), "summary-icon", accent),
                nameLabel,
                valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("summary-row");
        return row;
    }

    private String rangeHint(int min, int max) {
        return messages.format("field.range", messages.number(min), messages.number(max));
    }

    private static Node banner(String text) {
        final HBox banner = new HBox(10, Icons.of(MaterialDesignA.ALERT_CIRCLE_OUTLINE, 18),
                Nodes.paragraph(text, "banner-text"));
        banner.getStyleClass().add("banner-warning");
        banner.setAlignment(Pos.CENTER_LEFT);
        return banner;
    }
}
