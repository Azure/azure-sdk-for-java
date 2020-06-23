// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link MultiPointGeometryTests}.
 */
public class MultiPointGeometryTests {
    @Test
    public void nullPointsThrows() {
        assertThrows(NullPointerException.class, () -> new MultiPointGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        List<PointGeometry> points = Arrays.asList(
            new PointGeometry(new GeometryPosition(0, 0)),
            new PointGeometry(new GeometryPosition(1, 1))
        );

        MultiPointGeometry multiPoint = new MultiPointGeometry(points);

        assertEquals(points, multiPoint.getPoints());

        assertNull(multiPoint.getBoundingBox());
        assertNull(multiPoint.getProperties());
    }

    @Test
    public void complexConstructor() {
        List<PointGeometry> points = Arrays.asList(
            new PointGeometry(new GeometryPosition(0, 0)),
            new PointGeometry(new GeometryPosition(1, 1))
        );

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        MultiPointGeometry multiPoint = new MultiPointGeometry(points, boundingBox, properties);

        assertEquals(points, multiPoint.getPoints());
        assertEquals(boundingBox, multiPoint.getBoundingBox());
        assertEquals(properties, multiPoint.getProperties());
    }

    @Test
    public void constructorCopiesPoints() {
        List<PointGeometry> points = new ArrayList<>();
        points.add(new PointGeometry(new GeometryPosition(0, 0)));
        points.add(new PointGeometry(new GeometryPosition(1, 1)));

        MultiPointGeometry multiPoint = new MultiPointGeometry(points);
        assertEquals(points, multiPoint.getPoints());

        points.add(new PointGeometry(new GeometryPosition(0, 1)));
        assertNotEquals(points, multiPoint.getPoints());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void multiPointGeometriesEquals(MultiPointGeometry multiPoint, Object obj, boolean expected) {
        assertEquals(expected, multiPoint.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<PointGeometry> points = Arrays.asList(
            new PointGeometry(new GeometryPosition(0, 0)),
            new PointGeometry(new GeometryPosition(1, 1))
        );

        List<PointGeometry> points1 = Arrays.asList(
            new PointGeometry(new GeometryPosition(0, 0)),
            new PointGeometry(new GeometryPosition(0, 1))
        );

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        MultiPointGeometry multiPoint = new MultiPointGeometry(points);
        MultiPointGeometry multiPoint1 = new MultiPointGeometry(points1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(multiPoint, null, false),

            // Other isn't instance of type.
            Arguments.of(multiPoint, 1, false),

            // Other is itself.
            Arguments.of(multiPoint, multiPoint, true),
            Arguments.of(multiPoint1, multiPoint1, true),

            // Other is a different value.
            Arguments.of(multiPoint, multiPoint1, false),
            Arguments.of(multiPoint1, multiPoint, false),

            // Other is the same value.
            Arguments.of(multiPoint, new MultiPointGeometry(points), true),
            Arguments.of(multiPoint1, new MultiPointGeometry(points1, boundingBox, properties), true)
        );
    }
}
