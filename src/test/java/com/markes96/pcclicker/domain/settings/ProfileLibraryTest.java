package com.markes96.pcclicker.domain.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ProfileLibraryTest {

    private static SessionSettings clicking(int perSecond) {
        return new SessionSettings(ClickSettings.enabled(new ClickRate(perSecond)), MovementSettings.disabled(),
                TimeLimit.unlimited());
    }

    private static Profile profile(String name, int perSecond) {
        return new Profile(new ProfileName(name), clicking(perSecond));
    }

    private static List<String> names(ProfileLibrary library) {
        return library.profiles().stream().map(profile -> profile.name().value()).toList();
    }

    @Test
    void starts_empty() {
        assertThat(ProfileLibrary.empty().profiles()).isEmpty();
    }

    @Test
    void keeps_profiles_sorted_by_name_ignoring_case_and_accents() {
        final ProfileLibrary library = ProfileLibrary.empty()
                .save(profile("zeta", 1))
                .save(profile("Árbol", 1))
                .save(profile("beta", 1))
                .save(profile("Alfa", 1));

        assertThat(names(library)).containsExactly("Alfa", "Árbol", "beta", "zeta");
    }

    @Test
    void saving_under_an_existing_name_replaces_that_profile_whatever_the_case() {
        final ProfileLibrary library = ProfileLibrary.empty()
                .save(profile("Farm", 5))
                .save(profile("FARM", 25));

        assertThat(library.profiles()).containsExactly(profile("FARM", 25));
        assertThat(library.find(new ProfileName("farm"))).contains(profile("FARM", 25));
    }

    @Test
    void deletes_by_name_whatever_the_case() {
        final ProfileLibrary library = ProfileLibrary.empty().save(profile("Farm", 5)).save(profile("Idle", 1));

        final ProfileLibrary updated = library.delete(new ProfileName("farm"));

        assertThat(names(updated)).containsExactly("Idle");
        assertThat(updated.delete(new ProfileName("missing"))).isSameAs(updated);
    }

    @Test
    void is_immutable() {
        final ProfileLibrary original = ProfileLibrary.empty();

        original.save(profile("Farm", 5));

        assertThat(original.profiles()).isEmpty();
    }

    @Test
    void has_a_maximum_size_but_existing_profiles_can_still_be_replaced() {
        final ProfileLibrary full = ProfileLibrary.of(IntStream.range(0, ProfileLibrary.MAX_PROFILES)
                .mapToObj(i -> profile("P" + i, 1))
                .toList());

        assertThat(full.isFull()).isTrue();
        assertThatIllegalArgumentException().isThrownBy(() -> full.save(profile("one more", 1)));
        assertThat(full.save(profile("p7", 99)).find(new ProfileName("P7"))).contains(profile("p7", 99));
    }

    @Test
    void building_from_stored_profiles_keeps_the_last_of_duplicated_names() {
        final ProfileLibrary library = ProfileLibrary.of(List.of(profile("a", 1), profile("A", 2)));

        assertThat(library.profiles()).containsExactly(profile("A", 2));
    }
}
