package com.markes96.pcclicker.domain.movement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CyclicPathTest {

    private static final ScreenPoint A = new ScreenPoint(1, 1);
    private static final ScreenPoint B = new ScreenPoint(2, 2);
    private static final ScreenPoint C = new ScreenPoint(3, 3);

    @Test
    void walks_its_points_in_order_and_starts_again_at_the_end() {
        final CyclicPath path = new CyclicPath(List.of(A, B, C));

        assertThat(List.of(path.next(), path.next(), path.next(), path.next(), path.next()))
                .containsExactly(A, B, C, A, B);
    }

    @Test
    void rejects_an_empty_list_of_points() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CyclicPath(List.of()));
    }

    @Test
    void is_not_affected_by_later_changes_to_the_source_list() {
        final List<ScreenPoint> source = new ArrayList<>(List.of(A, B));
        final CyclicPath path = new CyclicPath(source);

        source.clear();

        assertThat(path.next()).isEqualTo(A);
    }
}
