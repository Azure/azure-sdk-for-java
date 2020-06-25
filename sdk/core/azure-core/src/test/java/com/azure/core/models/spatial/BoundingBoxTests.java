// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link BoundingBox}.
 */
public class BoundingBoxTests {
    static Point topLeft = new Point(378.2, 292.4);
    static Point topRight = new Point(1117.7, 468.3);
    static Point bottomRight = new Point(1035.7, 812.7);
    static Point bottomLeft = new Point(296.3, 636.8);

    @Test
    public void constructorTest() {
        List<Point> expectedPoints = Arrays.asList(
            topLeft, topRight, bottomRight, bottomLeft
        );

        BoundingBox boundingBox = new BoundingBox(new ArrayList<>(Arrays.asList(
            topLeft, topRight, bottomRight, bottomLeft
        )));

        final List<Point> actualPoints = boundingBox.getPoints();
        for (int i=0; i < actualPoints.size(); ++i) {
            Point expectedPoint = expectedPoints.get(i);
            Point actualPoint = actualPoints.get(i);
            assertEquals(expectedPoint.getX(), actualPoint.getX());
            assertEquals(expectedPoint.getY(), actualPoint.getY());
        }
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void boundingBoxEquals(BoundingBox boundingBox, Object obj, boolean expected) {
        assertEquals(expected, boundingBox.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        BoundingBox text1BoundingBox = new BoundingBox(new ArrayList<>(Arrays.asList(
            topLeft, topRight, bottomRight, bottomLeft
        )));
        BoundingBox text2BoundingBox = new BoundingBox(new ArrayList<>(Arrays.asList(
            new Point(8.2, 2.92),  new Point(78.2, 22.4),
            new Point(3.8, 29.4),  new Point(23.2, 92.8)
        )));

        return Stream.of(
            // Other is null.
            Arguments.of(text1BoundingBox, null, false),

            // Other isn't instance of type.
            Arguments.of(text1BoundingBox, 1, false),

            // Other is itself.
            Arguments.of(text1BoundingBox, text1BoundingBox, true),
            Arguments.of(text2BoundingBox, text2BoundingBox, true),

            // Other is a different value.
            Arguments.of(text1BoundingBox, text2BoundingBox, false),
            Arguments.of(text2BoundingBox, text1BoundingBox, false),

            // Other is the same value.
            Arguments.of(text1BoundingBox, text1BoundingBox, true),
            Arguments.of(text2BoundingBox, text2BoundingBox, true)
        );
    }
}
