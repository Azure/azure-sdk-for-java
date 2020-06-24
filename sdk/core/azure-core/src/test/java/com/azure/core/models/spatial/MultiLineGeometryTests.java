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

import static com.azure.core.models.spatial.GeometryTestHelpers.RECTANGLE_LINE;
import static com.azure.core.models.spatial.GeometryTestHelpers.SQUARE_LINE;
import static com.azure.core.models.spatial.GeometryTestHelpers.TRIANGLE_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link MultiLineGeometry}.
 */
public class MultiLineGeometryTests {
    @Test
    public void nullLinesThrows() {
        assertThrows(NullPointerException.class, () -> new MultiLineGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        List<LineGeometry> expectedLines = Arrays.asList(SQUARE_LINE.get(), TRIANGLE_LINE.get());

        MultiLineGeometry multiLine = new MultiLineGeometry(expectedLines);

        assertEquals(expectedLines, multiLine.getLines());

        assertNull(multiLine.getBoundingBox());
        assertNull(multiLine.getProperties());
    }

    @Test
    public void complexConstructor() {
        List<LineGeometry> expectedLines = Arrays.asList(SQUARE_LINE.get(), TRIANGLE_LINE.get());

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        MultiLineGeometry multiLine = new MultiLineGeometry(expectedLines, boundingBox, properties);

        assertEquals(expectedLines, multiLine.getLines());
        assertEquals(boundingBox, multiLine.getBoundingBox());
        assertEquals(properties, multiLine.getProperties());
    }

    @Test
    public void constructorCopiesLines() {
        List<LineGeometry> expectedLines = new ArrayList<>();
        expectedLines.add(SQUARE_LINE.get());
        expectedLines.add(TRIANGLE_LINE.get());

        MultiLineGeometry multiLine = new MultiLineGeometry(expectedLines);
        assertEquals(expectedLines, multiLine.getLines());

        expectedLines.add(RECTANGLE_LINE.get());
        assertNotEquals(expectedLines, multiLine.getLines());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void multiLineGeometriesEqual(MultiLineGeometry multiLine, Object obj, boolean expected) {
        assertEquals(expected, multiLine.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<LineGeometry> lines = Arrays.asList(SQUARE_LINE.get(), RECTANGLE_LINE.get());
        List<LineGeometry> lines1 = Arrays.asList(SQUARE_LINE.get(), TRIANGLE_LINE.get());

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        MultiLineGeometry multiLine = new MultiLineGeometry(lines);
        MultiLineGeometry multiLine1 = new MultiLineGeometry(lines1, boundingBox, properties);

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
            Arguments.of(multiLine, new MultiLineGeometry(lines), true),
            Arguments.of(multiLine1, new MultiLineGeometry(lines1, boundingBox, properties), true)
        );
    }
}
