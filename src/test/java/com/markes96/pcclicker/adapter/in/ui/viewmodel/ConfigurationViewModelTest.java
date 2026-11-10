package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.InvalidSessionConfigurationException;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import org.junit.jupiter.api.Test;

class ConfigurationViewModelTest {

    private final ConfigurationViewModel viewModel = new ConfigurationViewModel();

    @Test
    void starts_with_every_action_off_and_the_domain_defaults() {
        assertThat(viewModel.clickEnabled().get()).isFalse();
        assertThat(viewModel.movementEnabled().get()).isFalse();
        assertThat(viewModel.timeLimitEnabled().get()).isFalse();
        assertThat(viewModel.clickRate().get()).isEqualTo(ClickRate.DEFAULT.clicksPerSecond());
        assertThat(viewModel.pattern().get()).isEqualTo(MovementPattern.RANDOM);
        assertThat(viewModel.radius().get()).isEqualTo(Radius.DEFAULT.pixels());
        assertThat(viewModel.vertices().get()).isEqualTo(VertexCount.DEFAULT.value());
    }

    @Test
    void can_start_only_with_at_least_one_action() {
        assertThat(viewModel.canStart().get()).isFalse();

        viewModel.clickEnabled().set(true);
        assertThat(viewModel.canStart().get()).isTrue();

        viewModel.clickEnabled().set(false);
        viewModel.movementEnabled().set(true);
        assertThat(viewModel.canStart().get()).isTrue();
    }

    @Test
    void shows_radius_and_vertices_only_for_the_patterns_that_use_them() {
        viewModel.movementEnabled().set(true);

        viewModel.pattern().set(MovementPattern.RANDOM);
        assertThat(viewModel.radiusApplies().get()).isFalse();
        assertThat(viewModel.verticesApply().get()).isFalse();

        viewModel.pattern().set(MovementPattern.CIRCLE);
        assertThat(viewModel.radiusApplies().get()).isTrue();
        assertThat(viewModel.verticesApply().get()).isFalse();

        viewModel.pattern().set(MovementPattern.POLYGON);
        assertThat(viewModel.radiusApplies().get()).isTrue();
        assertThat(viewModel.verticesApply().get()).isTrue();
    }

    @Test
    void builds_the_session_configuration() {
        viewModel.clickEnabled().set(true);
        viewModel.clickRate().set(40);
        viewModel.movementEnabled().set(true);
        viewModel.pattern().set(MovementPattern.POLYGON);
        viewModel.radius().set(55);
        viewModel.vertices().set(7);
        viewModel.timeLimitEnabled().set(true);
        viewModel.timeLimitMinutes().set(15);

        final SessionConfiguration configuration = viewModel.toConfiguration();

        assertThat(configuration.click().enabled()).isTrue();
        assertThat(configuration.click().rate()).isEqualTo(new ClickRate(40));
        assertThat(configuration.movement().enabled()).isTrue();
        assertThat(configuration.movement().pattern()).isEqualTo(MovementPattern.POLYGON);
        assertThat(configuration.movement().radius()).isEqualTo(new Radius(55));
        assertThat(configuration.movement().vertices()).isEqualTo(new VertexCount(7));
        assertThat(configuration.timeLimit()).isEqualTo(TimeLimit.ofMinutes(15));
    }

    @Test
    void a_disabled_time_limit_means_no_limit() {
        viewModel.clickEnabled().set(true);
        viewModel.timeLimitMinutes().set(15);

        assertThat(viewModel.toConfiguration().timeLimit()).isEqualTo(TimeLimit.unlimited());
    }

    @Test
    void out_of_range_values_are_clamped_instead_of_failing() {
        viewModel.clickEnabled().set(true);
        viewModel.clickRate().set(5_000);
        viewModel.movementEnabled().set(true);
        viewModel.radius().set(0);
        viewModel.vertices().set(1_000);

        final SessionConfiguration configuration = viewModel.toConfiguration();

        assertThat(configuration.click().rate().clicksPerSecond()).isEqualTo(ClickRate.RANGE.max());
        assertThat(configuration.movement().radius().pixels()).isEqualTo(Radius.RANGE.min());
        assertThat(configuration.movement().vertices().value()).isEqualTo(VertexCount.RANGE.max());
    }

    @Test
    void refuses_to_build_a_configuration_without_actions() {
        assertThatThrownBy(viewModel::toConfiguration).isInstanceOf(InvalidSessionConfigurationException.class);
    }

    @Test
    void exposes_each_part_even_when_the_whole_is_not_valid_yet() {
        viewModel.clickRate().set(12);

        assertThat(viewModel.clickSettings().enabled()).isFalse();
        assertThat(viewModel.clickSettings().rate()).isEqualTo(new ClickRate(12));
        assertThat(viewModel.movementSettings().enabled()).isFalse();
        assertThat(viewModel.timeLimit()).isEqualTo(TimeLimit.unlimited());
    }

    @Test
    void exposes_every_setting_so_observers_can_follow_the_whole_form() {
        assertThat(viewModel.settings()).containsExactlyInAnyOrder(
                viewModel.clickEnabled(), viewModel.clickRate(),
                viewModel.movementEnabled(), viewModel.pattern(), viewModel.radius(), viewModel.vertices(),
                viewModel.timeLimitEnabled(), viewModel.timeLimitMinutes());
    }
}
