// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.experimental.spatial.GeometryTestHelpers.SQUARE_LINE;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.TRIANGLE_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link PolygonGeometry}.
 */
public class PolygonGeometryTests {
    @Test
    public void nullRingsThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new PolygonGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        List<LineGeometry> rings = Collections.singletonList(SQUARE_LINE.get());

        PolygonGeometry polygon = new PolygonGeometry(rings);

        assertEquals(rings, polygon.getRings());

        Assertions.assertNull(polygon.getBoundingBox());
        Assertions.assertNull(polygon.getProperties());
    }

    @Test
    public void complexConstructor() {
        List<LineGeometry> rings = Collections.singletonList(SQUARE_LINE.get());
        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        PolygonGeometry polygon = new PolygonGeometry(rings, boundingBox, properties);

        assertEquals(rings, polygon.getRings());
        assertEquals(boundingBox, polygon.getBoundingBox());
        assertEquals(properties, polygon.getProperties());
    }

    @Test
    public void constructorCopiesRings() {
        List<LineGeometry> rings = new ArrayList<>();
        rings.add(SQUARE_LINE.get());

        PolygonGeometry polygon = new PolygonGeometry(rings);
        assertEquals(rings, polygon.getRings());

        rings.add(TRIANGLE_LINE.get());
        assertNotEquals(rings, polygon.getRings());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void polygonGeometriesEquals(PolygonGeometry polygon, Object obj, boolean expected) {
        assertEquals(expected, polygon.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<LineGeometry> squareLine = Collections.singletonList(SQUARE_LINE.get());
        List<LineGeometry> triangleLine = Collections.singletonList(TRIANGLE_LINE.get());

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        PolygonGeometry polygon = new PolygonGeometry(squareLine);
        PolygonGeometry polygon1 = new PolygonGeometry(triangleLine, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(polygon, null, false),

            // Other isn't instance of type.
            Arguments.of(polygon, 1, false),

            // Other is itself.
            Arguments.of(polygon, polygon, true),
            Arguments.of(polygon1, polygon1, true),

            // Other is a different value.
            Arguments.of(polygon, polygon1, false),
            Arguments.of(polygon1, polygon, false),

            // Other is the same value.
            Arguments.of(polygon, new PolygonGeometry(squareLine), true),
            Arguments.of(polygon1, new PolygonGeometry(triangleLine, boundingBox, properties), true)
        );
    }
}
