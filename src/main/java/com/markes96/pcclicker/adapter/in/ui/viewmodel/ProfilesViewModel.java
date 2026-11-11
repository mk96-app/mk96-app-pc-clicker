package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfileEntry.Included;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfileEntry.Saved;
import com.markes96.pcclicker.application.port.in.ProfilesUseCase;
import com.markes96.pcclicker.domain.settings.Favorites;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileLibrary;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Profiles as seen from the session form. Favourites come first, then the presets included with
 * the application, then the user's saved profiles. Any of them can be loaded into the form and
 * starred; only saved ones can be overwritten or deleted, and a new profile cannot take the name
 * of a preset.
 */
public final class ProfilesViewModel {

    /** What saving under a given name would do. */
    public enum SaveAction {
        /** The name is blank, too long or has invalid characters. */
        INVALID,
        /** The name belongs to a preset included with the application. */
        RESERVED,
        /** The name is new but the library is full. */
        FULL,
        CREATE,
        REPLACE
    }

    private final ProfilesUseCase useCase;
    private final ConfigurationViewModel form;
    private final List<Included> included;
    private final ObservableList<ProfileEntry> entries = FXCollections.observableArrayList();
    private final ReadOnlyObjectWrapper<ProfileEntry> selected = new ReadOnlyObjectWrapper<>(this, "selected");
    private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper(this, "modified");
    private final BooleanBinding selectionEditable = Bindings.createBooleanBinding(
            () -> selected.get() != null && selected.get().editable(), selected);
    private final BooleanBinding selectionFavorite = Bindings.createBooleanBinding(
            () -> selected.get() != null && selected.get().favorite(), selected);
    private final List<Consumer<RuntimeException>> storageFailureListeners = new ArrayList<>();

    /** @param presetName name of each preset in the user's language */
    public ProfilesViewModel(ProfilesUseCase useCase, ConfigurationViewModel form, Function<Preset, String> presetName) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
        this.form = Objects.requireNonNull(form, "form");
        Objects.requireNonNull(presetName, "presetName");
        this.included = useCase.presets().stream().map(preset -> new Included(preset, presetName.apply(preset))).toList();
        refresh();
        for (Observable setting : form.settings()) {
            setting.addListener(observable -> updateModified());
        }
    }

    /**
     * Favourites first; within each group, presets in their own order and then saved profiles
     * sorted by name.
     */
    public ObservableList<ProfileEntry> entries() {
        return FXCollections.unmodifiableObservableList(entries);
    }

    /** Profile last loaded or saved; {@code null} when the form is not tied to one. */
    public ReadOnlyObjectProperty<ProfileEntry> selected() {
        return selected.getReadOnlyProperty();
    }

    /** True when the form differs from the selected profile. */
    public ReadOnlyBooleanProperty modified() {
        return modified.getReadOnlyProperty();
    }

    /** True when the selected profile is one the user saved, so it can be deleted. */
    public BooleanBinding selectionEditable() {
        return selectionEditable;
    }

    /** True when the selected profile is marked with a star. */
    public BooleanBinding selectionFavorite() {
        return selectionFavorite;
    }

    /** Stars or unstars the selected profile, which moves it to or from the top of the list. */
    public void toggleFavorite() {
        final ProfileEntry entry = selected.get();
        if (entry == null) {
            return;
        }
        storing(() -> useCase.setFavorite(entry.ref(), !entry.favorite()));
    }

    /** Fills the form with the profile. Does nothing if a saved profile no longer exists. */
    public void load(ProfileEntry entry) {
        Objects.requireNonNull(entry, "entry");
        final Optional<SessionSettings> settings = settingsOf(entry);
        if (settings.isEmpty()) {
            refresh();
            return;
        }
        selected.set(current(entry));
        form.load(settings.get());
        updateModified();
    }

    /** Forgets the selected profile without touching the form. */
    public void clearSelection() {
        selected.set(null);
        updateModified();
    }

    /** Name to propose when saving: the selected saved profile, or nothing to start a new one. */
    public String suggestedName() {
        return selected.get() instanceof Saved saved ? saved.name().value() : "";
    }

    public SaveAction saveActionFor(String text) {
        if (!ProfileName.isValid(text)) {
            return SaveAction.INVALID;
        }
        final ProfileName name = new ProfileName(text);
        if (isPresetName(name)) {
            return SaveAction.RESERVED;
        }
        if (savedNamed(name).isPresent()) {
            return SaveAction.REPLACE;
        }
        return savedCount() >= ProfileLibrary.MAX_PROFILES ? SaveAction.FULL : SaveAction.CREATE;
    }

    /** Whether {@link #save(String)} accepts {@code text}. */
    public boolean canSave(String text) {
        final SaveAction action = saveActionFor(text);
        return action == SaveAction.CREATE || action == SaveAction.REPLACE;
    }

    /**
     * Saves the form under {@code text} and selects it.
     *
     * @throws IllegalArgumentException when {@link #canSave(String)} is false
     */
    public ProfileName save(String text) {
        if (!canSave(text)) {
            throw new IllegalArgumentException("Cannot save a profile named '" + text + "': " + saveActionFor(text));
        }
        final ProfileName name = new ProfileName(text);
        storing(() -> useCase.save(new Profile(name, form.toSettings())));
        selected.set(entryFor(ProfileRef.of(name)).orElse(new Saved(name)));
        updateModified();
        return name;
    }

    /** Deletes the selected saved profile; the form keeps its values. Presets are never deleted. */
    public void deleteSelected() {
        if (!(selected.get() instanceof Saved saved)) {
            return;
        }
        storing(() -> useCase.delete(saved.name()));
        selected.set(null);
        updateModified();
    }

    /** Called on the UI thread when a change could not be stored; it still applies until closing. */
    public void onStorageFailure(Consumer<RuntimeException> listener) {
        storageFailureListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void storing(Runnable change) {
        try {
            change.run();
        } catch (IllegalArgumentException rejected) {
            throw rejected;
        } catch (RuntimeException failure) {
            storageFailureListeners.forEach(listener -> listener.accept(failure));
        } finally {
            refresh();
        }
    }

    private void refresh() {
        final Favorites favorites = useCase.favorites();
        final List<ProfileEntry> all = new ArrayList<>();
        included.forEach(preset -> all.add(new Included(preset.preset(), preset.label(),
                favorites.contains(preset.ref()))));
        useCase.profiles().forEach(profile -> all.add(new Saved(profile.name(),
                favorites.contains(ProfileRef.of(profile.name())))));
        // Stable sort: favourites move up and every group keeps its own order.
        all.sort(Comparator.comparing(entry -> !entry.favorite()));
        entries.setAll(all);
        if (selected.get() != null) {
            selected.set(entryFor(selected.get().ref()).orElse(null));
        }
    }

    private Optional<ProfileEntry> entryFor(ProfileRef ref) {
        return entries.stream().filter(entry -> entry.ref().equals(ref)).findFirst();
    }

    private Optional<SessionSettings> settingsOf(ProfileEntry entry) {
        return switch (entry) {
            case Included preset -> Optional.of(preset.preset().settings());
            case Saved saved -> useCase.find(saved.name()).map(Profile::settings);
        };
    }

    /** The entry as listed now: stored spelling of a saved name and current favourite state. */
    private ProfileEntry current(ProfileEntry entry) {
        return entryFor(entry.ref()).orElse(entry);
    }

    private Optional<ProfileEntry> savedNamed(ProfileName name) {
        return entries.stream().filter(entry -> entry instanceof Saved saved && saved.name().sameAs(name)).findFirst();
    }

    private long savedCount() {
        return entries.stream().filter(ProfileEntry::editable).count();
    }

    private boolean isPresetName(ProfileName name) {
        return included.stream().anyMatch(preset -> preset.label().strip().toLowerCase(Locale.ROOT).equals(name.key()));
    }

    private void updateModified() {
        final ProfileEntry entry = selected.get();
        modified.set(entry != null && settingsOf(entry)
                .map(settings -> !settings.equals(form.toSettings()))
                .orElse(false));
    }
}
