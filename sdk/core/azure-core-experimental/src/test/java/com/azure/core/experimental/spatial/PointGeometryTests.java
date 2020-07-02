// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link PointGeometry}.
 */
public class PointGeometryTests {
    @Test
    public void nullPositionThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new PointGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        GeometryPosition position = new GeometryPosition(0, 0);

        PointGeometry point = new PointGeometry(position);

        assertEquals(position, point.getPosition());

        Assertions.assertNull(point.getBoundingBox());
        Assertions.assertNull(point.getProperties());
    }

    @Test
    public void complexConstructor() {
        GeometryPosition position = new GeometryPosition(0, 0);
        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        PointGeometry point = new PointGeometry(position, boundingBox, properties);

        assertEquals(position, point.getPosition());
        assertEquals(boundingBox, point.getBoundingBox());
        assertEquals(properties, point.getProperties());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void pointGeometriesEquals(PointGeometry point, Object obj, boolean expected) {
        assertEquals(expected, point.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GeometryPosition position = new GeometryPosition(0, 0);
        GeometryPosition position1 = new GeometryPosition(1, 1);

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        PointGeometry point = new PointGeometry(position);
        PointGeometry point1 = new PointGeometry(position1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(point, null, false),

            // Other isn't instance of type.
            Arguments.of(point, 1, false),

            // Other is itself.
            Arguments.of(point, point, true),
            Arguments.of(point1, point1, true),

            // Other is a different value.
            Arguments.of(point, point1, false),
            Arguments.of(point1, point, false),

            // Other is the same value.
            Arguments.of(point, new PointGeometry(position), true),
            Arguments.of(point1, new PointGeometry(position1, boundingBox, properties), true)
        );
    }
}
