// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link GeoPolygon}.
 */
public class GeoPolygonTests {
    @Test
    public void nullRingsThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new GeoPolygon((GeoLinearRing) null));
    }

    @Test
    public void simpleConstructor() {
        List<GeoLinearRing> rings = Collections.singletonList(new GeoLinearRing(GeoTestHelpers.SQUARE_LINE_POSITIONS));

        GeoPolygon polygon = new GeoPolygon(new GeoLinearRing(GeoTestHelpers.SQUARE_LINE.get().getCoordinates()));

        assertEquals(rings, polygon.getRings());

        Assertions.assertNull(polygon.getBoundingBox());
        Assertions.assertNull(polygon.getCustomProperties());
    }

    @Test
    public void complexConstructor() {
        List<GeoLinearRing> rings = Collections.singletonList(new GeoLinearRing(GeoTestHelpers.SQUARE_LINE_POSITIONS));
        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoPolygon polygon = new GeoPolygon(rings, boundingBox, properties);

        assertEquals(rings, polygon.getRings());
        assertEquals(boundingBox, polygon.getBoundingBox());
        assertEquals(properties, polygon.getCustomProperties());
    }

    @Test
    public void constructorCopiesRings() {
        List<GeoLinearRing> rings = new ArrayList<>();
        rings.add(new GeoLinearRing(GeoTestHelpers.SQUARE_LINE_POSITIONS));

        GeoPolygon polygon = new GeoPolygon(rings);
        assertEquals(rings, polygon.getRings());

        rings.add(new GeoLinearRing(GeoTestHelpers.TRIANGLE_LINE_POSITIONS));
        assertNotEquals(rings, polygon.getRings());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void polygonGeometriesEquals(GeoPolygon polygon, Object obj, boolean expected) {
        assertEquals(expected, polygon.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GeoLinearRing squareLine = new GeoLinearRing(GeoTestHelpers.SQUARE_LINE_POSITIONS);
        GeoLinearRing triangleLine = new GeoLinearRing(GeoTestHelpers.TRIANGLE_LINE_POSITIONS);

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoPolygon polygon = new GeoPolygon(squareLine);
        GeoPolygon polygon1 = new GeoPolygon(triangleLine, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(polygon, null, false),

            // Other isn't instance of type.
            Arguments.of(polygon, 1, false),

            // Other is itself.
            Arguments.of(polygon, polygon, true), Arguments.of(polygon1, polygon1, true),

            // Other is a different value.
            Arguments.of(polygon, polygon1, false), Arguments.of(polygon1, polygon, false),

            // Other is the same value.
            Arguments.of(polygon, new GeoPolygon(squareLine), true),
            Arguments.of(polygon1, new GeoPolygon(triangleLine, boundingBox, properties), true));
    }
}
