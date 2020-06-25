// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link Point}.
 */
public class PointTests {
    @Test
    public void simpleConstructor() {
        Point point = new Point(1222.0, 2334.9);
        BoundingBox boundingBox = new BoundingBox(new ArrayList<>(Arrays.asList(point)));
        assertEquals(point, boundingBox.getPoints().get(0));
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void pointEquals(Point point, Object obj, boolean expected) {
        assertEquals(expected, point.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        Point point = new Point(110, 54);
        Point point1 = new Point(123, 123.0);

        return Stream.of(
            // Other is null.
            Arguments.of(point, null, false),

            // Other isn't instance of type.
            Arguments.of(point, "1", false),

            // Other is itself.
            Arguments.of(point, point, true),
            Arguments.of(point1, point1, true),

            // Other is a different value.
            Arguments.of(point, point1, false),
            Arguments.of(point1, point, false),

            // Other is the same value.
            Arguments.of(point, new Point(110, 54), true),
            Arguments.of(point1, new Point(123, 123.0), true)
        );
    }
}
