// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link GeoPointCollectionTests}.
 */
public class GeoPointCollectionTests {
    @Test
    public void nullPointsThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new GeoPointCollection(null));
    }

    @Test
    public void simpleConstructor() {
        List<GeoPoint> points = Arrays.asList(
            new GeoPoint(new GeoPosition(0, 0)),
            new GeoPoint(new GeoPosition(1, 1))
        );

        GeoPointCollection multiPoint = new GeoPointCollection(points);

        assertEquals(points, multiPoint.getPoints());

        Assertions.assertNull(multiPoint.getBoundingBox());
        Assertions.assertNull(multiPoint.getCustomProperties());
    }

    @Test
    public void complexConstructor() {
        List<GeoPoint> points = Arrays.asList(
            new GeoPoint(new GeoPosition(0, 0)),
            new GeoPoint(new GeoPosition(1, 1))
        );

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoPointCollection multiPoint = new GeoPointCollection(points, boundingBox, properties);

        assertEquals(points, multiPoint.getPoints());
        assertEquals(boundingBox, multiPoint.getBoundingBox());
        assertEquals(properties, multiPoint.getCustomProperties());
    }

    @Test
    public void constructorCopiesPoints() {
        List<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(new GeoPosition(0, 0)));
        points.add(new GeoPoint(new GeoPosition(1, 1)));

        GeoPointCollection multiPoint = new GeoPointCollection(points);
        assertEquals(points, multiPoint.getPoints());

        points.add(new GeoPoint(new GeoPosition(0, 1)));
        assertNotEquals(points, multiPoint.getPoints());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void multiPointGeometriesEquals(GeoPointCollection multiPoint, Object obj, boolean expected) {
        assertEquals(expected, multiPoint.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<GeoPoint> points = Arrays.asList(
            new GeoPoint(new GeoPosition(0, 0)),
            new GeoPoint(new GeoPosition(1, 1))
        );

        List<GeoPoint> points1 = Arrays.asList(
            new GeoPoint(new GeoPosition(0, 0)),
            new GeoPoint(new GeoPosition(0, 1))
        );

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoPointCollection multiPoint = new GeoPointCollection(points);
        GeoPointCollection multiPoint1 = new GeoPointCollection(points1, boundingBox, properties);

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
            Arguments.of(multiPoint, new GeoPointCollection(points), true),
            Arguments.of(multiPoint1, new GeoPointCollection(points1, boundingBox, properties), true)
        );
    }
}
