package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.application.port.in.PreferencesUseCase;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.control.KeyBindings;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.domain.settings.Preferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;

/**
 * Observable preferences for the whole UI: every key cap, hint and countdown on screen follows
 * them, so a change in the settings page shows everywhere at once. Each change is handed to the
 * application, which stores it and applies it to the global hotkeys.
 */
public final class PreferencesViewModel {

    private final PreferencesUseCase useCase;
    private final ReadOnlyObjectWrapper<Preferences> preferences;
    private final List<Consumer<RuntimeException>> storageFailureListeners = new ArrayList<>();

    public PreferencesViewModel(PreferencesUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
        this.preferences = new ReadOnlyObjectWrapper<>(this, "preferences", useCase.preferences());
    }

    public ReadOnlyObjectProperty<Preferences> preferences() {
        return preferences.getReadOnlyProperty();
    }

    public Preferences current() {
        return preferences.get();
    }

    /** Key currently bound to {@code hotkey}, kept up to date. */
    public ObjectBinding<Key> keyOf(Hotkey hotkey) {
        Objects.requireNonNull(hotkey, "hotkey");
        return Bindings.createObjectBinding(() -> preferences.get().keys().keyFor(hotkey), preferences);
    }

    /** Countdown seconds, kept up to date. */
    public ObservableValue<Integer> countdownSeconds() {
        return Bindings.createObjectBinding(() -> preferences.get().countdown().value(), preferences);
    }

    /**
     * Binds {@code hotkey} to {@code key}. When another hotkey used that key, they swap keys.
     *
     * @return the hotkey that received the old key in a swap, if any
     */
    public Optional<Hotkey> rebind(Hotkey hotkey, Key key) {
        final KeyBindings keys = current().keys();
        final Optional<Hotkey> displaced = keys.hotkeyFor(key).filter(other -> other != hotkey);
        change(current -> current.withKeys(keys.with(hotkey, key)));
        return displaced;
    }

    public void setCountdown(int seconds) {
        change(current -> current.withCountdown(new CountdownSeconds(CountdownSeconds.RANGE.clamp(seconds))));
    }

    public void setShowPanel(boolean show) {
        change(current -> current.withShowPanel(show));
    }

    public void restoreDefaults() {
        change(current -> Preferences.defaults());
    }

    /** Called on the UI thread when a change could not be stored; it still applies until closing. */
    public void onStorageFailure(Consumer<RuntimeException> listener) {
        storageFailureListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void change(UnaryOperator<Preferences> change) {
        final Preferences updated = change.apply(current());
        if (updated.equals(current())) {
            return;
        }
        preferences.set(updated);
        try {
            useCase.updatePreferences(updated);
        } catch (RuntimeException failure) {
            storageFailureListeners.forEach(listener -> listener.accept(failure));
        }
    }
}
