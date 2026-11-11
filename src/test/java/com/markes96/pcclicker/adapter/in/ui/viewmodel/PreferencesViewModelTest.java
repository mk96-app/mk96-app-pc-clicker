package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.application.port.in.PreferencesUseCase;
import com.markes96.pcclicker.application.port.out.SettingsStorageException;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.domain.settings.Preferences;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

class PreferencesViewModelTest {

    private final List<Preferences> stored = new ArrayList<>();
    private boolean failing;
    private final PreferencesUseCase useCase = new PreferencesUseCase() {
        @Override
        public Preferences preferences() {
            return Preferences.defaults().withCountdown(new CountdownSeconds(5));
        }

        @Override
        public void updatePreferences(Preferences preferences) {
            if (failing) {
                throw new SettingsStorageException("Disk full", new IllegalStateException());
            }
            stored.add(preferences);
        }
    };
    private final PreferencesViewModel viewModel = new PreferencesViewModel(useCase);

    @Test
    void starts_from_the_stored_preferences() {
        assertThat(viewModel.current().countdown().value()).isEqualTo(5);
    }

    @Test
    void key_bindings_follow_every_change() {
        final ObjectBinding<Key> stopKey = viewModel.keyOf(Hotkey.STOP);

        viewModel.rebind(Hotkey.STOP, Key.S);

        assertThat(stopKey.get()).isEqualTo(Key.S);
        assertThat(stored).last().satisfies(preferences ->
                assertThat(preferences.keys().keyFor(Hotkey.STOP)).isEqualTo(Key.S));
    }

    @Test
    void rebinding_to_a_key_in_use_reports_the_hotkey_that_got_the_old_key() {
        assertThat(viewModel.rebind(Hotkey.STOP, Key.P)).contains(Hotkey.TOGGLE_PAUSE);
        assertThat(viewModel.keyOf(Hotkey.TOGGLE_PAUSE).get()).isEqualTo(Key.X);

        assertThat(viewModel.rebind(Hotkey.STOP, Key.F11)).isEmpty();
        assertThat(viewModel.rebind(Hotkey.STOP, Key.F11)).isEmpty();
    }

    @Test
    void the_countdown_is_clamped_and_observable() {
        final ObservableValue<Integer> countdown = viewModel.countdownSeconds();

        viewModel.setCountdown(99);

        assertThat(countdown.getValue()).isEqualTo(CountdownSeconds.RANGE.max());
    }

    @Test
    void unchanged_values_are_not_stored_again() {
        viewModel.setCountdown(5);
        viewModel.setShowPanel(true);

        assertThat(stored).isEmpty();
    }

    @Test
    void restoring_the_defaults_resets_everything() {
        viewModel.rebind(Hotkey.START, Key.F1);
        viewModel.setShowPanel(false);

        viewModel.restoreDefaults();

        assertThat(viewModel.current()).isEqualTo(Preferences.defaults());
    }

    @Test
    void a_change_that_cannot_be_stored_is_kept_and_reported() {
        final List<RuntimeException> failures = new ArrayList<>();
        viewModel.onStorageFailure(failures::add);
        failing = true;

        viewModel.setShowPanel(false);

        assertThat(viewModel.current().showPanel()).isFalse();
        assertThat(failures).singleElement().isInstanceOf(SettingsStorageException.class);
    }
}
