// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import org.junit.jupiter.api.Assertions;
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

import static com.azure.core.experimental.spatial.GeometryTestHelpers.RECTANGLE_POLYGON;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.SQUARE_POLYGON;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.TRIANGLE_POLYGON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link MultiPolygonGeometry}.
 */
public class MultiPolygonGeometryTests {
    @Test
    public void nullPolygonsThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new MultiPolygonGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        List<PolygonGeometry> polygons = Arrays.asList(TRIANGLE_POLYGON.get(), SQUARE_POLYGON.get());

        MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(polygons);

        assertEquals(polygons, multiPolygon.getPolygons());

        Assertions.assertNull(multiPolygon.getBoundingBox());
        Assertions.assertNull(multiPolygon.getProperties());
    }

    @Test
    public void complexConstructor() {
        List<PolygonGeometry> polygons = Arrays.asList(TRIANGLE_POLYGON.get(), SQUARE_POLYGON.get());

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(polygons, boundingBox, properties);

        assertEquals(polygons, multiPolygon.getPolygons());
        assertEquals(boundingBox, multiPolygon.getBoundingBox());
        assertEquals(properties, multiPolygon.getProperties());
    }

    @Test
    public void constructorCopiesPolygons() {
        List<PolygonGeometry> polygons = new ArrayList<>();
        polygons.add(TRIANGLE_POLYGON.get());
        polygons.add(SQUARE_POLYGON.get());

        MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(polygons);
        assertEquals(polygons, multiPolygon.getPolygons());

        polygons.add(RECTANGLE_POLYGON.get());

        assertNotEquals(polygons, multiPolygon.getPolygons());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void multiPolygonGeometriesEquals(MultiPolygonGeometry multiPolygon, Object obj, boolean expected) {
        assertEquals(expected, multiPolygon.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<PolygonGeometry> polygons = Arrays.asList(TRIANGLE_POLYGON.get(), SQUARE_POLYGON.get());
        List<PolygonGeometry> polygons1 = Arrays.asList(TRIANGLE_POLYGON.get(), RECTANGLE_POLYGON.get());

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 2, 2);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(polygons);
        MultiPolygonGeometry multiPolygon1 = new MultiPolygonGeometry(polygons1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(multiPolygon, null, false),

            // Other isn't instance of type.
            Arguments.of(multiPolygon, 1, false),

            // Other is itself.
            Arguments.of(multiPolygon, multiPolygon, true),
            Arguments.of(multiPolygon1, multiPolygon1, true),

            // Other is a different value.
            Arguments.of(multiPolygon, multiPolygon1, false),
            Arguments.of(multiPolygon1, multiPolygon, false),

            // Other is the same value.
            Arguments.of(multiPolygon, new MultiPolygonGeometry(polygons), true),
            Arguments.of(multiPolygon1, new MultiPolygonGeometry(polygons1, boundingBox, properties), true)
        );
    }
}
