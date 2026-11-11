package com.markes96.pcclicker.adapter.in.ui.page;

import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfileEntry;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfilesViewModel;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfilesViewModel.SaveAction;
import com.markes96.pcclicker.adapter.in.ui.window.Notifier;
import com.markes96.pcclicker.adapter.in.ui.window.ToastLayer;
import com.markes96.pcclicker.domain.settings.ProfileLibrary;
import com.markes96.pcclicker.domain.settings.ProfileName;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

/**
 * Strip above the session cards to load, save and delete profiles. Saving opens an inline name
 * field instead of a dialog; deleting asks for a second click instead of a confirmation window.
 */
final class ProfileBar extends VBox {

    private static final Duration DELETE_CONFIRMATION_TIME = Duration.seconds(3);
    private static final PseudoClass DANGER = PseudoClass.getPseudoClass("danger");
    private static final PseudoClass PROMPT = PseudoClass.getPseudoClass("prompt");
    private static final PseudoClass INCLUDED = PseudoClass.getPseudoClass("included");
    private static final PseudoClass FAVORITE = PseudoClass.getPseudoClass("favorite");

    private final ProfilesViewModel profiles;
    private final Messages messages;
    private final Notifier notifier;
    private final BooleanProperty editing = new SimpleBooleanProperty(this, "editing");
    private final BooleanProperty confirmingDelete = new SimpleBooleanProperty(this, "confirmingDelete");
    private final ComboBox<ProfileEntry> picker = new ComboBox<>();
    private final TextField nameField = new TextField();
    private final PauseTransition deleteTimeout = new PauseTransition(DELETE_CONFIRMATION_TIME);
    private boolean syncing;

    ProfileBar(ProfilesViewModel profiles, Messages messages, Notifier notifier) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.notifier = Objects.requireNonNull(notifier, "notifier");
        getStyleClass().add("profile-bar");

        final HBox browse = browseRow();
        final VBox save = saveRow();
        Nodes.showWhen(browse, editing.not());
        Nodes.showWhen(save, editing);
        getChildren().addAll(browse, save);

        deleteTimeout.setOnFinished(event -> confirmingDelete.set(false));
    }

    // --- Browsing: pick, save, delete ---

    private HBox browseRow() {
        picker.getStyleClass().add("profile-picker");
        picker.setItems(profiles.entries());
        picker.setPromptText(messages.get("profiles.none"));
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setPrefWidth(160); // grows into the free space, never pushes the buttons out
        picker.setMinWidth(120);
        picker.setVisibleRowCount(8);
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProfileEntry entry) {
                return entry == null ? "" : entry.label();
            }

            @Override
            public ProfileEntry fromString(String text) {
                return null; // not editable
            }
        });
        picker.setCellFactory(list -> new EntryCell(false));
        // JavaFX does not always bring the prompt back once the value is cleared (e.g. after a delete).
        picker.setButtonCell(new EntryCell(true));
        HBox.setHgrow(picker, Priority.ALWAYS);

        picker.valueProperty().addListener((observable, previous, chosen) -> {
            if (!syncing && chosen != null) {
                profiles.load(chosen);
                confirmingDelete.set(false);
            }
        });
        profiles.selected().addListener((observable, previous, current) -> syncPicker());
        profiles.entries().addListener((ListChangeListener<ProfileEntry>) change -> syncPicker());
        syncPicker();

        final Button star = new Button();
        star.getStyleClass().addAll("profile-button", "profile-star");
        star.setMinWidth(Button.USE_PREF_SIZE);
        star.graphicProperty().bind(Bindings.when(profiles.selectionFavorite())
                .then(Icons.of(MaterialDesignS.STAR, 17))
                .otherwise(Icons.of(MaterialDesignS.STAR_OUTLINE, 17)));
        profiles.selectionFavorite().addListener((observable, was, favorite) ->
                star.pseudoClassStateChanged(FAVORITE, favorite));
        final Tooltip starTip = new Tooltip();
        starTip.textProperty().bind(Bindings.when(profiles.selectionFavorite())
                .then(messages.get("profiles.unfavorite"))
                .otherwise(messages.get("profiles.favorite")));
        star.setTooltip(starTip);
        star.accessibleTextProperty().bind(starTip.textProperty());
        star.disableProperty().bind(profiles.selected().isNull());
        star.setOnAction(event -> profiles.toggleFavorite());

        final Label modified = Nodes.label(messages.get("profiles.modified"), "profile-modified");
        modified.setMinWidth(Label.USE_PREF_SIZE);
        Nodes.showWhen(modified, profiles.modified());

        final Button save = new Button(messages.get("profiles.save"), Icons.of(MaterialDesignC.CONTENT_SAVE_OUTLINE, 16));
        save.getStyleClass().add("profile-button");
        save.setMinWidth(Button.USE_PREF_SIZE);
        save.setOnAction(event -> beginSaving());

        final Button delete = new Button();
        delete.getStyleClass().add("profile-button");
        delete.setMinWidth(Button.USE_PREF_SIZE);
        delete.graphicProperty().bind(Bindings.when(confirmingDelete)
                .then(Icons.of(MaterialDesignD.DELETE_ALERT_OUTLINE, 16))
                .otherwise(Icons.of(MaterialDesignD.DELETE_OUTLINE, 16)));
        delete.textProperty().bind(Bindings.when(confirmingDelete)
                .then(messages.get("profiles.confirmDelete"))
                .otherwise(""));
        confirmingDelete.addListener((observable, was, confirming) ->
                delete.pseudoClassStateChanged(DANGER, confirming));
        final Tooltip deleteTip = new Tooltip();
        deleteTip.textProperty().bind(Bindings.when(profiles.selected().isNotNull()
                        .and(profiles.selectionEditable().not()))
                .then(messages.get("profiles.includedLocked"))
                .otherwise(messages.get("profiles.delete")));
        delete.setTooltip(deleteTip);
        delete.setAccessibleText(messages.get("profiles.delete"));
        delete.disableProperty().bind(profiles.selectionEditable().not());
        delete.setOnAction(event -> onDelete());

        final Label title = Nodes.label(messages.get("profiles.title"), "profile-label");
        title.setMinWidth(Label.USE_PREF_SIZE);
        // Disabled controls get no tooltip, so the explanation lives on a wrapper around the button.
        final StackPane deleteHolder = new StackPane(delete);
        deleteHolder.setMinWidth(StackPane.USE_PREF_SIZE);
        Tooltip.install(deleteHolder, deleteTip);

        final HBox row = new HBox(10,
                Icons.of(MaterialDesignB.BOOKMARK_OUTLINE, 18),
                title,
                picker,
                star,
                modified,
                save,
                deleteHolder);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void syncPicker() {
        syncing = true;
        try {
            picker.setValue(profiles.selected().get());
        } finally {
            syncing = false;
        }
    }

    private void onDelete() {
        if (!confirmingDelete.get()) {
            confirmingDelete.set(true);
            deleteTimeout.playFromStart();
            return;
        }
        deleteTimeout.stop();
        confirmingDelete.set(false);
        final ProfileEntry entry = profiles.selected().get();
        profiles.deleteSelected();
        notifier.notify(ToastLayer.Kind.INFO, messages.format("profiles.deleted", entry.label()), null);
    }

    // --- Saving: inline name field ---

    private VBox saveRow() {
        nameField.getStyleClass().add("profile-name-field");
        nameField.setPromptText(messages.get("profiles.namePrompt"));
        nameField.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= ProfileName.MAX_LENGTH ? change : null));
        HBox.setHgrow(nameField, Priority.ALWAYS);

        final Button confirm = new Button();
        confirm.getStyleClass().addAll("profile-button", "profile-confirm");
        confirm.setMinWidth(Button.USE_PREF_SIZE);
        confirm.setGraphic(Icons.of(MaterialDesignC.CHECK, 16));
        confirm.textProperty().bind(Bindings.createStringBinding(
                () -> profiles.saveActionFor(nameField.getText()) == SaveAction.REPLACE
                        ? messages.get("profiles.replace")
                        : messages.get("profiles.create"),
                nameField.textProperty(), profiles.entries()));
        confirm.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !profiles.canSave(nameField.getText()), nameField.textProperty(), profiles.entries()));
        confirm.setOnAction(event -> confirmSaving());

        final Button cancel = new Button(messages.get("profiles.cancel"));
        cancel.getStyleClass().add("profile-button");
        cancel.setMinWidth(Button.USE_PREF_SIZE);
        cancel.setOnAction(event -> editing.set(false));

        nameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !confirm.isDisabled()) {
                confirmSaving();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                editing.set(false);
                event.consume();
            }
        });

        final Label hint = Nodes.label("", "field-hint");
        hint.textProperty().bind(Bindings.createStringBinding(() -> switch (profiles.saveActionFor(nameField.getText())) {
            case REPLACE -> messages.format("profiles.replaceHint", nameField.getText().strip());
            case RESERVED -> messages.get("profiles.reserved");
            case FULL -> messages.format("profiles.full", ProfileLibrary.MAX_PROFILES);
            case INVALID, CREATE -> messages.format("profiles.nameHint", ProfileName.MAX_LENGTH);
        }, nameField.textProperty(), profiles.entries()));

        final HBox row = new HBox(10, Icons.of(MaterialDesignB.BOOKMARK_PLUS_OUTLINE, 18), nameField, confirm, cancel);
        row.setAlignment(Pos.CENTER_LEFT);
        final VBox box = new VBox(6, row, hint);
        box.getStyleClass().add("profile-save");
        return box;
    }

    private void beginSaving() {
        confirmingDelete.set(false);
        nameField.setText(profiles.suggestedName());
        editing.set(true);
        nameField.requestFocus();
        nameField.selectAll();
    }

    /** Profile in the picker: presets carry a lock, since they can be used but not changed. */
    private final class EntryCell extends ListCell<ProfileEntry> {

        private final boolean buttonCell;

        private EntryCell(boolean buttonCell) {
            this.buttonCell = buttonCell;
        }

        @Override
        protected void updateItem(ProfileEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            final boolean none = empty || entry == null;
            setText(none ? (buttonCell ? picker.getPromptText() : null) : entry.label());
            setGraphic(none ? null : marks(entry));
            setTooltip(!none && !buttonCell && !entry.editable() ? new Tooltip(messages.get("profiles.included")) : null);
            pseudoClassStateChanged(PROMPT, none && buttonCell);
            pseudoClassStateChanged(INCLUDED, !none && !entry.editable());
        }

        /** A yellow star for favourites and a lock for included profiles, in a fixed-width slot. */
        private HBox marks(ProfileEntry entry) {
            final HBox marks = new HBox(4);
            marks.setAlignment(Pos.CENTER_LEFT);
            marks.getStyleClass().add("profile-marks");
            if (entry.favorite()) {
                marks.getChildren().add(Nodes.styled(Icons.of(MaterialDesignS.STAR, 14), "favorite-star"));
            }
            if (!entry.editable()) {
                marks.getChildren().add(Icons.of(MaterialDesignL.LOCK_OUTLINE, 14));
            }
            return marks.getChildren().isEmpty() ? null : marks;
        }
    }

    private void confirmSaving() {
        final boolean replacing = profiles.saveActionFor(nameField.getText()) == SaveAction.REPLACE;
        final ProfileName name = profiles.save(nameField.getText());
        editing.set(false);
        notifier.notify(ToastLayer.Kind.SUCCESS,
                messages.format(replacing ? "profiles.replaced" : "profiles.saved", name.value()), null);
    }
}
