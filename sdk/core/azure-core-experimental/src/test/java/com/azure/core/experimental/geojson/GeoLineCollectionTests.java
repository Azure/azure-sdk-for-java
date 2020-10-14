// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

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

import static com.azure.core.experimental.geojson.GeoTestHelpers.RECTANGLE_LINE;
import static com.azure.core.experimental.geojson.GeoTestHelpers.SQUARE_LINE;
import static com.azure.core.experimental.geojson.GeoTestHelpers.TRIANGLE_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link GeoLineCollection}.
 */
public class GeoLineCollectionTests {
    @Test
    public void nullLinesThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new GeoLineCollection(null));
    }

    @Test
    public void simpleConstructor() {
        List<GeoLine> expectedLines = Arrays.asList(SQUARE_LINE.get(), TRIANGLE_LINE.get());

        GeoLineCollection multiLine = new GeoLineCollection(expectedLines);

        assertEquals(expectedLines, multiLine.getLines());

        Assertions.assertNull(multiLine.getBoundingBox());
        Assertions.assertNull(multiLine.getCustomProperties());
    }

    @Test
    public void complexConstructor() {
        List<GeoLine> expectedLines = Arrays.asList(SQUARE_LINE.get(), TRIANGLE_LINE.get());

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoLineCollection multiLine = new GeoLineCollection(expectedLines, boundingBox, properties);

        assertEquals(expectedLines, multiLine.getLines());
        assertEquals(boundingBox, multiLine.getBoundingBox());
        assertEquals(properties, multiLine.getCustomProperties());
    }

    @Test
    public void constructorCopiesLines() {
        List<GeoLine> expectedLines = new ArrayList<>();
        expectedLines.add(SQUARE_LINE.get());
        expectedLines.add(TRIANGLE_LINE.get());

        GeoLineCollection multiLine = new GeoLineCollection(expectedLines);
        assertEquals(expectedLines, multiLine.getLines());

        expectedLines.add(RECTANGLE_LINE.get());
        assertNotEquals(expectedLines, multiLine.getLines());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void multiLineGeometriesEqual(GeoLineCollection multiLine, Object obj, boolean expected) {
        assertEquals(expected, multiLine.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<GeoLine> lines = Arrays.asList(SQUARE_LINE.get(), RECTANGLE_LINE.get());
        List<GeoLine> lines1 = Arrays.asList(SQUARE_LINE.get(), TRIANGLE_LINE.get());

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoLineCollection multiLine = new GeoLineCollection(lines);
        GeoLineCollection multiLine1 = new GeoLineCollection(lines1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(multiLine, null, false),

            // Other isn't instance of type.
            Arguments.of(multiLine, 1, false),

            // Other is itself.
            Arguments.of(multiLine, multiLine, true),
            Arguments.of(multiLine1, multiLine1, true),

            // Other is a different value.
            Arguments.of(multiLine, multiLine1, false),
            Arguments.of(multiLine1, multiLine, false),

            // Other is the same value.
            Arguments.of(multiLine, new GeoLineCollection(lines), true),
            Arguments.of(multiLine1, new GeoLineCollection(lines1, boundingBox, properties), true)
        );
    }
}
