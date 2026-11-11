package com.markes96.pcclicker.adapter.in.ui.page;

import atlantafx.base.controls.ToggleSwitch;
import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.component.FeatureCard;
import com.markes96.pcclicker.adapter.in.ui.component.KeyCap;
import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.component.NumberStepper;
import com.markes96.pcclicker.adapter.in.ui.component.PageHeader;
import com.markes96.pcclicker.adapter.in.ui.format.KeyCodes;
import com.markes96.pcclicker.adapter.in.ui.format.SettingsDescriber;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.PreferencesViewModel;
import com.markes96.pcclicker.adapter.in.ui.window.Notifier;
import com.markes96.pcclicker.adapter.in.ui.window.Page;
import com.markes96.pcclicker.adapter.in.ui.window.ToastLayer;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import java.util.Objects;
import java.util.Optional;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignK;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

/**
 * Preferences: the key of every global hotkey, the countdown and the floating panel. Changes apply
 * and are saved as soon as they are made; there is no "save" button to forget.
 */
public final class SettingsPage implements Page {

    private static final PseudoClass CAPTURING = PseudoClass.getPseudoClass("capturing");
    /** Hotkeys stay off a little after a new key is chosen, so its release cannot trigger them. */
    private static final Duration HOTKEY_RESUME_DELAY = Duration.millis(400);

    private final UiContext context;
    private final Messages messages;
    private final SettingsDescriber describer;
    private final PreferencesViewModel preferences;
    private final Notifier notifier;

    private final ObjectProperty<Hotkey> capturing = new SimpleObjectProperty<>(this, "capturing");
    private final PauseTransition resumeHotkeys = new PauseTransition(HOTKEY_RESUME_DELAY);
    private final Label keyStatus = Nodes.paragraph("", "field-hint", "key-status");
    private Scene captureScene;
    private final EventHandler<KeyEvent> captureFilter = this::onCapturedKey;

    public SettingsPage(UiContext context, SettingsDescriber describer, Notifier notifier) {
        this.context = Objects.requireNonNull(context, "context");
        this.messages = context.messages();
        this.describer = Objects.requireNonNull(describer, "describer");
        this.preferences = context.preferences();
        this.notifier = Objects.requireNonNull(notifier, "notifier");
        resumeHotkeys.setOnFinished(event -> context.hotkeys().resumeHotkeys());
    }

    @Override
    public String title() {
        return messages.get("nav.settings");
    }

    @Override
    public Ikon icon() {
        return MaterialDesignC.COG_OUTLINE;
    }

    @Override
    public Node view() {
        final Button restore = new Button(messages.get("settings.restore"), Icons.of(MaterialDesignR.RESTORE, 16));
        restore.getStyleClass().add("secondary-button");
        restore.setOnAction(event -> {
            stopCapturing();
            preferences.restoreDefaults();
            keyStatus.setText("");
            notifier.notify(ToastLayer.Kind.INFO, messages.get("settings.restored"), null);
        });

        final Label location = Nodes.paragraph(
                messages.format("settings.location", context.settingsLocation()), "help-note");
        final HBox footer = new HBox(16, location, Nodes.spacer(), restore);
        footer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(location, Priority.ALWAYS);

        final VBox column = new VBox(16,
                PageHeader.of(messages.get("settings.eyebrow"), messages.get("settings.title"),
                        messages.get("settings.subtitle")),
                hotkeysCard(),
                sessionCard(),
                footer);
        column.getStyleClass().add("page-column");

        final ScrollPane scroll = new ScrollPane(column);
        scroll.getStyleClass().add("page-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // --- Hotkeys ---

    private Node hotkeysCard() {
        final VBox rows = new VBox(10);
        for (Hotkey hotkey : Hotkey.values()) {
            rows.getChildren().add(hotkeyRow(hotkey));
        }
        if (!context.hotkeysAvailable()) {
            rows.getChildren().add(Nodes.paragraph(messages.get("hotkeys.unavailable"), "help-note"));
        }
        Nodes.showWhen(keyStatus, keyStatus.textProperty().isNotEmpty());
        rows.getChildren().add(keyStatus);
        return section(MaterialDesignK.KEYBOARD_OUTLINE, messages.get("settings.keys.title"),
                messages.get("settings.keys.description"), rows);
    }

    private Node hotkeyRow(Hotkey hotkey) {
        final Button change = new Button();
        change.getStyleClass().add("key-change");
        change.setMinWidth(Button.USE_PREF_SIZE);
        change.textProperty().bind(Bindings.when(capturing.isEqualTo(hotkey))
                .then(messages.get("settings.keys.pressKey"))
                .otherwise(messages.get("settings.keys.change")));
        capturing.addListener((observable, previous, current) ->
                change.pseudoClassStateChanged(CAPTURING, current == hotkey));
        change.setOnAction(event -> {
            if (capturing.get() == hotkey) {
                stopCapturing();
            } else {
                startCapturing(hotkey, change.getScene());
            }
        });
        change.focusedProperty().addListener((observable, wasFocused, focused) -> {
            if (!focused && capturing.get() == hotkey) {
                stopCapturing();
            }
        });

        final VBox texts = new VBox(1,
                Nodes.label(describer.hotkeyName(hotkey), "field-name"),
                Nodes.label(messages.get("settings.keys." + hotkey.name()), "field-hint"));
        HBox.setHgrow(texts, Priority.ALWAYS);
        final HBox row = new HBox(12, texts, KeyCap.of(preferences.keyOf(hotkey)), change);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("field-row");
        return row;
    }

    private void startCapturing(Hotkey hotkey, Scene scene) {
        stopCapturing();
        resumeHotkeys.stop();
        context.hotkeys().suspendHotkeys();
        capturing.set(hotkey);
        captureScene = scene;
        captureScene.addEventFilter(KeyEvent.KEY_PRESSED, captureFilter);
        captureScene.addEventFilter(KeyEvent.KEY_RELEASED, captureFilter);
        keyStatus.setText(messages.format("settings.keys.capturing", describer.hotkeyName(hotkey)));
    }

    private void stopCapturing() {
        if (captureScene != null) {
            captureScene.removeEventFilter(KeyEvent.KEY_PRESSED, captureFilter);
            captureScene.removeEventFilter(KeyEvent.KEY_RELEASED, captureFilter);
            captureScene = null;
        }
        if (capturing.get() != null) {
            capturing.set(null);
            keyStatus.setText("");
            resumeHotkeys.playFromStart();
        }
    }

    /**
     * The key is taken on release, the same moment the global hook reacts, and hotkeys only resume
     * a little later: the key just chosen never fires its new hotkey by accident.
     */
    private void onCapturedKey(KeyEvent event) {
        event.consume();
        final Hotkey hotkey = capturing.get();
        if (hotkey == null || event.getEventType() != KeyEvent.KEY_RELEASED) {
            return;
        }
        final Optional<Key> key = KeyCodes.keyOf(event.getCode());
        if (key.isEmpty()) {
            keyStatus.setText(messages.get("settings.keys.unsupported"));
            return;
        }
        final Key previousKey = preferences.current().keys().keyFor(hotkey);
        final Optional<Hotkey> displaced = preferences.rebind(hotkey, key.get());
        stopCapturing();
        keyStatus.setText(displaced
                .map(other -> messages.format("settings.keys.swapped", key.get().label(), describer.hotkeyName(hotkey),
                        previousKey.label(), describer.hotkeyName(other)))
                .orElseGet(() -> messages.format("settings.keys.assigned", key.get().label(),
                        describer.hotkeyName(hotkey))));
    }

    // --- Session behaviour ---

    private Node sessionCard() {
        final IntegerProperty countdown = new SimpleIntegerProperty(preferences.current().countdown().value());
        countdown.addListener((observable, previous, seconds) -> preferences.setCountdown(seconds.intValue()));
        final NumberStepper countdownStepper =
                new NumberStepper(CountdownSeconds.RANGE, countdown, messages.get("unit.seconds"), messages);

        final ToggleSwitch showPanel = new ToggleSwitch();
        showPanel.setSelected(preferences.current().showPanel());
        showPanel.selectedProperty().addListener((observable, was, show) -> preferences.setShowPanel(show));
        showPanel.setAccessibleText(messages.get("settings.panel"));
        showPanel.setDisable(!context.hotkeysAvailable());

        // Restoring the defaults changes the preferences from outside these controls.
        preferences.preferences().addListener((observable, previous, current) -> {
            countdown.set(current.countdown().value());
            showPanel.setSelected(current.showPanel());
        });

        final VBox rows = new VBox(12,
                FeatureCard.field(messages.get("settings.countdown"), messages.get("settings.countdownHint"),
                        countdownStepper),
                FeatureCard.field(messages.get("settings.panel"),
                        messages.get(context.hotkeysAvailable() ? "settings.panelHint" : "settings.panelRequired"),
                        showPanel));
        return section(MaterialDesignT.TIMER_SAND, messages.get("settings.session.title"),
                messages.get("settings.session.description"), rows);
    }

    private static Node section(Ikon ikon, String title, String description, Node content) {
        final HBox header = new HBox(14,
                Nodes.styled(new StackPane(Icons.of(ikon, 20)), "feature-badge"),
                new VBox(2, Nodes.label(title, "feature-title"), Nodes.paragraph(description, "feature-description")));
        header.setAlignment(Pos.CENTER_LEFT);
        final VBox body = new VBox(content);
        body.getStyleClass().add("feature-body");
        final VBox card = new VBox(header, body);
        card.getStyleClass().addAll("feature-card", "settings-card", "accent-green");
        return card;
    }
}
