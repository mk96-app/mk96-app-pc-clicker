package com.markes96.pcclicker.domain.movement;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ShapesTest {

    private static final ScreenPoint CENTER = new ScreenPoint(500, 400);

    private static double distance(ScreenPoint a, ScreenPoint b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }

    @Nested
    class CircleShapeTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 10, 57, 100})
        void every_point_lies_on_the_circumference(int radius) {
            final List<ScreenPoint> outline = new CircleShape(CENTER, new Radius(radius)).outline();

            assertThat(outline).allSatisfy(point ->
                    assertThat(distance(point, CENTER)).isCloseTo(radius, org.assertj.core.data.Offset.offset(1.0)));
        }

        @Test
        void uses_one_point_every_two_pixels_of_circumference() {
            final List<ScreenPoint> outline = new CircleShape(CENTER, new Radius(10)).outline();

            assertThat(outline).hasSize((int) Math.round(2 * Math.PI * 10 * 0.5));
        }

        @Test
        void always_has_enough_points_to_draw_a_loop() {
            assertThat(new CircleShape(CENTER, new Radius(1)).outline()).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        void starts_on_the_right_of_the_center_and_turns_clockwise_on_screen() {
            final List<ScreenPoint> outline = new CircleShape(CENTER, new Radius(20)).outline();

            assertThat(outline.get(0)).isEqualTo(new ScreenPoint(520, 400));
            assertThat(outline.get(1).y()).isGreaterThan(400);
        }
    }

    @Nested
    class PolygonShapeTest {

        @Test
        void places_an_odd_polygon_with_a_vertex_pointing_up() {
            final List<ScreenPoint> vertices =
                    new PolygonShape(CENTER, new VertexCount(3), new Radius(10)).vertices();

            assertThat(vertices).hasSize(3);
            assertThat(vertices.get(0)).isEqualTo(new ScreenPoint(500, 390));
        }

        @Test
        void places_a_square_with_horizontal_and_vertical_sides() {
            final List<ScreenPoint> vertices =
                    new PolygonShape(CENTER, new VertexCount(4), new Radius(100)).vertices();

            assertThat(vertices).containsExactly(
                    new ScreenPoint(571, 329),
                    new ScreenPoint(571, 471),
                    new ScreenPoint(429, 471),
                    new ScreenPoint(429, 329));
        }

        @Test
        void places_an_even_polygon_with_a_vertex_pointing_right() {
            final List<ScreenPoint> vertices =
                    new PolygonShape(CENTER, new VertexCount(6), new Radius(10)).vertices();

            assertThat(vertices.get(0)).isEqualTo(new ScreenPoint(510, 400));
        }

        @Test
        void outline_starts_at_the_first_vertex_and_visits_every_vertex_in_order() {
            final PolygonShape square = new PolygonShape(CENTER, new VertexCount(4), new Radius(100));
            final List<ScreenPoint> outline = square.outline();

            assertThat(outline.get(0)).isEqualTo(square.vertices().get(0));
            assertThat(outline).containsSubsequence(square.vertices());
        }

        @Test
        void outline_points_are_spaced_about_two_pixels_apart() {
            final List<ScreenPoint> outline =
                    new PolygonShape(CENTER, new VertexCount(4), new Radius(100)).outline();

            for (int i = 1; i < outline.size(); i++) {
                assertThat(distance(outline.get(i - 1), outline.get(i))).isBetween(1.0, 3.0);
            }
        }

        @Test
        void tiny_polygons_still_produce_a_point_per_side() {
            final PolygonShape tiny = new PolygonShape(CENTER, new VertexCount(300), new Radius(1));

            assertThat(tiny.outline()).hasSizeGreaterThanOrEqualTo(300);
        }

        @Test
        void a_two_vertex_polygon_is_a_segment_walked_back_and_forth() {
            final PolygonShape segment = new PolygonShape(CENTER, new VertexCount(2), new Radius(10));

            assertThat(segment.vertices()).containsExactly(new ScreenPoint(510, 400), new ScreenPoint(490, 400));
            assertThat(segment.outline()).allSatisfy(point -> assertThat(point.y()).isEqualTo(400));
        }
    }
}
