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
 * Tests {@link GeoLineString}.
 */
public class GeoLineStringTests {
    @Test
    public void nullPositionsThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new GeoLineString(null));
    }

    @Test
    public void simpleConstructor() {
        GeoArray<GeoPosition> expectedPositions = new GeoArray<>(Arrays.asList(new GeoPosition(0, 0),
            new GeoPosition(0, 1)));

        GeoLineString line = new GeoLineString(expectedPositions);

        assertEquals(expectedPositions, line.getCoordinates());

        Assertions.assertNull(line.getBoundingBox());
        Assertions.assertNull(line.getCustomProperties());
    }

    @Test
    public void complexConstructor() {
        GeoArray<GeoPosition> expectedPositions = new GeoArray<>(Arrays.asList(new GeoPosition(0, 0),
            new GeoPosition(0, 1)));
        GeoBoundingBox expectedBoundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> expectedProperties = Collections.singletonMap("key", "value");

        GeoLineString line = new GeoLineString(expectedPositions, expectedBoundingBox, expectedProperties);

        assertEquals(expectedPositions, line.getCoordinates());
        assertEquals(expectedBoundingBox, line.getBoundingBox());
        assertEquals(expectedProperties, line.getCustomProperties());
    }

    @Test
    public void constructorCopiesPositions() {
        List<GeoPosition> expectedPositions = new ArrayList<>();
        expectedPositions.add(new GeoPosition(0, 0));
        expectedPositions.add(new GeoPosition(0, 1));

        GeoLineString line = new GeoLineString(expectedPositions);
        assertEquals(new GeoArray<>(expectedPositions), line.getCoordinates());

        expectedPositions.add(new GeoPosition(1, 1));
        assertNotEquals(new GeoArray<>(expectedPositions), line.getCoordinates());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void lineGeometriesEqual(GeoLineString line, Object obj, boolean expected) {
        assertEquals(expected, line.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<GeoPosition> positions = Arrays.asList(new GeoPosition(0, 0), new GeoPosition(0, 1));
        List<GeoPosition> positions1 = Arrays.asList(new GeoPosition(0, 0), new GeoPosition(1, 1));

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoLineString line = new GeoLineString(positions);
        GeoLineString line1 = new GeoLineString(positions1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(line, null, false),

            // Other isn't instance of type.
            Arguments.of(line, 1, false),

            // Other is itself.
            Arguments.of(line, line, true),
            Arguments.of(line1, line1, true),

            // Other is a different value.
            Arguments.of(line, line1, false),
            Arguments.of(line1, line, false),

            // Other is the same value.
            Arguments.of(line, new GeoLineString(positions), true),
            Arguments.of(line1, new GeoLineString(positions1, boundingBox, properties), true)
        );
    }
}
