// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

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
 * Tests {@link GeoPoint}.
 */
public class GeoPointTests {
    @Test
    public void nullPositionThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new GeoPoint(null));
    }

    @Test
    public void simpleConstructor() {
        GeoPosition position = new GeoPosition(0, 0);

        GeoPoint point = new GeoPoint(position);

        assertEquals(position, point.getCoordinates());

        Assertions.assertNull(point.getBoundingBox());
        Assertions.assertNull(point.getCustomProperties());
    }

    @Test
    public void complexConstructor() {
        GeoPosition position = new GeoPosition(0, 0);
        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoPoint point = new GeoPoint(position, boundingBox, properties);

        assertEquals(position, point.getCoordinates());
        assertEquals(boundingBox, point.getBoundingBox());
        assertEquals(properties, point.getCustomProperties());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void pointGeometriesEquals(GeoPoint point, Object obj, boolean expected) {
        assertEquals(expected, point.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GeoPosition position = new GeoPosition(0, 0);
        GeoPosition position1 = new GeoPosition(1, 1);

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoPoint point = new GeoPoint(position);
        GeoPoint point1 = new GeoPoint(position1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(point, null, false),

            // Other isn't instance of type.
            Arguments.of(point, 1, false),

            // Other is itself.
            Arguments.of(point, point, true), Arguments.of(point1, point1, true),

            // Other is a different value.
            Arguments.of(point, point1, false), Arguments.of(point1, point, false),

            // Other is the same value.
            Arguments.of(point, new GeoPoint(position), true),
            Arguments.of(point1, new GeoPoint(position1, boundingBox, properties), true));
    }
}
