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
 * Tests {@link LineGeometry}.
 */
public class LineGeometryTests {
    @Test
    public void nullPositionsThrows() {
        assertThrows(NullPointerException.class, () -> new LineGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        List<GeometryPosition> expectedPositions = Arrays.asList(new GeometryPosition(0, 0),
            new GeometryPosition(0, 1));

        LineGeometry line = new LineGeometry(expectedPositions);

        assertEquals(expectedPositions, line.getPositions());

        assertNull(line.getBoundingBox());
        assertNull(line.getProperties());
    }

    @Test
    public void complexConstructor() {
        List<GeometryPosition> expectedPositions = Arrays.asList(new GeometryPosition(0, 0),
            new GeometryPosition(0, 1));
        GeometryBoundingBox expectedBoundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> expectedProperties = Collections.singletonMap("key", "value");

        LineGeometry line = new LineGeometry(expectedPositions, expectedBoundingBox, expectedProperties);

        assertEquals(expectedPositions, line.getPositions());
        assertEquals(expectedBoundingBox, line.getBoundingBox());
        assertEquals(expectedProperties, line.getProperties());
    }

    @Test
    public void constructorCopiesPositions() {
        List<GeometryPosition> expectedPositions = new ArrayList<>();
        expectedPositions.add(new GeometryPosition(0, 0));
        expectedPositions.add(new GeometryPosition(0, 1));

        LineGeometry line = new LineGeometry(expectedPositions);
        assertEquals(expectedPositions, line.getPositions());

        expectedPositions.add(new GeometryPosition(1, 1));
        assertNotEquals(expectedPositions, line.getPositions());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void lineGeometriesEqual(LineGeometry line, Object obj, boolean expected) {
        assertEquals(expected, line.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<GeometryPosition> positions = Arrays.asList(new GeometryPosition(0, 0), new GeometryPosition(0, 1));
        List<GeometryPosition> positions1 = Arrays.asList(new GeometryPosition(0, 0), new GeometryPosition(1, 1));

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        LineGeometry line = new LineGeometry(positions);
        LineGeometry line1 = new LineGeometry(positions1, boundingBox, properties);

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
            Arguments.of(line, new LineGeometry(positions), true),
            Arguments.of(line1, new LineGeometry(positions1, boundingBox, properties), true)
        );
    }
}
