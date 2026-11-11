package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfileEntry.Included;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfileEntry.Saved;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfilesViewModel.SaveAction;
import com.markes96.pcclicker.application.service.SettingsService;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileLibrary;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import com.markes96.pcclicker.domain.settings.UserSettings;
import com.markes96.pcclicker.support.InMemorySettingsRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProfilesViewModelTest {

    private static String presetName(Preset preset) {
        return "Preset " + preset.name();
    }

    private final InMemorySettingsRepository repository = new InMemorySettingsRepository();
    private final ConfigurationViewModel form = new ConfigurationViewModel();
    private SettingsService service;
    private ProfilesViewModel profiles;

    @BeforeEach
    void setUp() {
        service = SettingsService.load(repository);
        profiles = new ProfilesViewModel(service, form, ProfilesViewModelTest::presetName);
    }

    private void configureClicking(int perSecond) {
        form.clickEnabled().set(true);
        form.clickRate().set(perSecond);
    }

    private List<Saved> savedEntries() {
        return profiles.entries().stream().filter(Saved.class::isInstance).map(Saved.class::cast).toList();
    }

    @Test
    void starts_without_a_selection_and_with_the_included_profiles() {
        assertThat(profiles.selected().get()).isNull();
        assertThat(profiles.modified().get()).isFalse();
        assertThat(profiles.entries())
                .extracting(ProfileEntry::label)
                .containsExactly(Arrays.stream(Preset.values()).map(ProfilesViewModelTest::presetName)
                        .toArray(String[]::new));
    }

    @Nested
    class IncludedProfiles {

        private Included first() {
            return (Included) profiles.entries().getFirst();
        }

        @Test
        void can_be_loaded_into_the_form() {
            profiles.load(new Included(Preset.HUMAN_CLICK, presetName(Preset.HUMAN_CLICK)));

            assertThat(form.toSettings()).isEqualTo(Preset.HUMAN_CLICK.settings());
            assertThat(profiles.modified().get()).isFalse();
        }

        @Test
        void can_be_edited_in_the_form_but_never_deleted() {
            profiles.load(first());
            form.clickRate().set(99);

            assertThat(profiles.modified().get()).isTrue();
            assertThat(profiles.selectionEditable().get()).isFalse();

            profiles.deleteSelected();
            assertThat(profiles.entries()).contains(first());
            assertThat(profiles.selected().get()).isEqualTo(first());
        }

        @Test
        void can_be_saved_as_a_profile_of_the_user_under_another_name() {
            profiles.load(first());

            assertThat(profiles.suggestedName()).isEmpty();
            profiles.save("My anti-AFK");

            assertThat(savedEntries()).extracting(Saved::label).containsExactly("My anti-AFK");
            assertThat(service.find(new ProfileName("My anti-AFK"))).map(Profile::settings)
                    .contains(Preset.ANTI_AFK.settings());
        }

        @Test
        void their_names_cannot_be_taken() {
            assertThat(profiles.saveActionFor("preset anti_afk")).isEqualTo(SaveAction.RESERVED);
            assertThat(profiles.canSave("Preset ANTI_AFK")).isFalse();
            assertThatIllegalArgumentException().isThrownBy(() -> profiles.save("Preset ANTI_AFK"));
        }
    }

    @Nested
    class Favourites {

        @Test
        void a_starred_profile_moves_to_the_top_and_keeps_its_selection() {
            profiles.save("Zeta");

            profiles.toggleFavorite();

            assertThat(profiles.entries().getFirst()).isEqualTo(new Saved(new ProfileName("Zeta"), true));
            assertThat(profiles.selected().get().favorite()).isTrue();
            assertThat(profiles.selectionFavorite().get()).isTrue();
            assertThat(service.favorites().contains(ProfileRef.of(new ProfileName("Zeta")))).isTrue();
        }

        @Test
        void included_profiles_can_be_starred_too_and_favourites_keep_their_usual_order() {
            profiles.load(new Included(Preset.ONE_HOUR_FARM, presetName(Preset.ONE_HOUR_FARM)));
            profiles.toggleFavorite();
            profiles.load(new Included(Preset.HUMAN_CLICK, presetName(Preset.HUMAN_CLICK)));
            profiles.toggleFavorite();

            assertThat(profiles.entries()).extracting(ProfileEntry::label).startsWith(
                    presetName(Preset.HUMAN_CLICK), presetName(Preset.ONE_HOUR_FARM), presetName(Preset.ANTI_AFK));
        }

        @Test
        void unstarring_puts_it_back_in_its_place() {
            profiles.load(new Included(Preset.ONE_HOUR_FARM, presetName(Preset.ONE_HOUR_FARM)));
            profiles.toggleFavorite();
            profiles.toggleFavorite();

            assertThat(profiles.entries().getFirst().label()).isEqualTo(presetName(Preset.ANTI_AFK));
            assertThat(profiles.selectionFavorite().get()).isFalse();
        }

        @Test
        void the_star_is_remembered_after_restarting() {
            profiles.load(new Included(Preset.RIGHT_CLICK, presetName(Preset.RIGHT_CLICK)));
            profiles.toggleFavorite();

            final ProfilesViewModel reopened = new ProfilesViewModel(
                    SettingsService.load(repository), new ConfigurationViewModel(), ProfilesViewModelTest::presetName);

            assertThat(reopened.entries().getFirst()).isEqualTo(
                    new Included(Preset.RIGHT_CLICK, presetName(Preset.RIGHT_CLICK), true));
        }

        @Test
        void without_a_selection_nothing_is_starred() {
            profiles.toggleFavorite();

            assertThat(service.favorites().refs()).isEmpty();
        }
    }

    @Nested
    class SavedProfiles {

        @Test
        void saving_stores_the_form_and_selects_the_new_profile() {
            configureClicking(25);

            final ProfileName name = profiles.save("  Farm  ");

            assertThat(name.value()).isEqualTo("Farm");
            assertThat(savedEntries()).containsExactly(new Saved(new ProfileName("Farm")));
            assertThat(profiles.selected().get()).isEqualTo(new Saved(new ProfileName("Farm")));
            assertThat(profiles.selectionEditable().get()).isTrue();
            assertThat(profiles.suggestedName()).isEqualTo("Farm");
            assertThat(service.find(name)).map(Profile::settings).contains(form.toSettings());
            assertThat(profiles.modified().get()).isFalse();
        }

        @Test
        void come_after_the_included_ones() {
            profiles.save("Aaa");

            assertThat(profiles.entries().getLast()).isEqualTo(new Saved(new ProfileName("Aaa")));
            assertThat(profiles.entries().getFirst()).isInstanceOf(Included.class);
        }

        @Test
        void editing_the_form_after_saving_marks_it_as_modified_and_undoing_clears_it() {
            configureClicking(25);
            profiles.save("Farm");

            form.clickRate().set(50);
            assertThat(profiles.modified().get()).isTrue();

            form.clickRate().set(25);
            assertThat(profiles.modified().get()).isFalse();
        }

        @Test
        void loading_fills_the_form_and_keeps_the_stored_spelling() {
            configureClicking(25);
            profiles.save("Farm");
            form.clickEnabled().set(false);
            form.movementEnabled().set(true);
            form.pattern().set(MovementPattern.CIRCLE);
            profiles.clearSelection();

            profiles.load(new Saved(new ProfileName("FARM")));

            assertThat(form.clickEnabled().get()).isTrue();
            assertThat(form.clickRate().get()).isEqualTo(25);
            assertThat(form.movementEnabled().get()).isFalse();
            assertThat(profiles.selected().get()).isEqualTo(new Saved(new ProfileName("Farm")));
            assertThat(profiles.modified().get()).isFalse();
        }

        @Test
        void tells_what_saving_under_a_name_would_do() {
            profiles.save("Farm");

            assertThat(profiles.saveActionFor("")).isEqualTo(SaveAction.INVALID);
            assertThat(profiles.saveActionFor("x".repeat(ProfileName.MAX_LENGTH + 1))).isEqualTo(SaveAction.INVALID);
            assertThat(profiles.saveActionFor("farm ")).isEqualTo(SaveAction.REPLACE);
            assertThat(profiles.saveActionFor("Idle")).isEqualTo(SaveAction.CREATE);
            assertThatIllegalArgumentException().isThrownBy(() -> profiles.save("   "));
        }

        @Test
        void a_full_library_only_allows_replacing() {
            final SettingsService full = SettingsService.load(new InMemorySettingsRepository(
                    UserSettings.defaults().withProfiles(ProfileLibrary.of(
                            IntStream.range(0, ProfileLibrary.MAX_PROFILES)
                                    .mapToObj(i -> new Profile(new ProfileName("P" + i), SessionSettings.defaults()))
                                    .toList()))));
            final ProfilesViewModel crowded = new ProfilesViewModel(full, form, ProfilesViewModelTest::presetName);

            assertThat(crowded.saveActionFor("New one")).isEqualTo(SaveAction.FULL);
            assertThat(crowded.saveActionFor("p1")).isEqualTo(SaveAction.REPLACE);
        }

        @Test
        void deleting_removes_the_selected_profile_and_keeps_the_form() {
            configureClicking(25);
            profiles.save("Farm");

            profiles.deleteSelected();

            assertThat(savedEntries()).isEmpty();
            assertThat(profiles.selected().get()).isNull();
            assertThat(form.clickRate().get()).isEqualTo(25);
        }

        @Test
        void a_save_that_cannot_be_stored_is_kept_and_reported() {
            final List<RuntimeException> failures = new ArrayList<>();
            profiles.onStorageFailure(failures::add);
            repository.failWrites(true);

            profiles.save("Farm");

            assertThat(failures).hasSize(1);
            assertThat(savedEntries()).containsExactly(new Saved(new ProfileName("Farm")));
        }
    }
}
