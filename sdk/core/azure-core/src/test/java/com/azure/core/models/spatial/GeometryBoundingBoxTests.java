// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.core.models.spatial.GeometryTestHelpers.MT_RAINIER_BOUNDING_BOX;
import static com.azure.core.models.spatial.GeometryTestHelpers.PIKES_PLACE_BOUNDING_BOX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link GeometryBoundingBox}.
 */
public class GeometryBoundingBoxTests {
    @Test
    public void simpleConstructor() {
        double expectedWest = -180;
        double expectedSouth = -90;
        double expectedEast = 180;
        double expectedNorth = 90;

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(expectedWest, expectedSouth, expectedEast,
            expectedNorth);

        assertEquals(expectedWest, boundingBox.getWest());
        assertEquals(expectedSouth, boundingBox.getSouth());
        assertEquals(expectedEast, boundingBox.getEast());
        assertEquals(expectedNorth, boundingBox.getNorth());

        assertNull(boundingBox.getMinAltitude());
        assertNull(boundingBox.getMaxAltitude());
    }

    @Test
    public void complexConstructor() {
        double expectedWest = -180;
        double expectedSouth = -90;
        double expectedEast = 180;
        double expectedNorth = 90;
        double expectedMinAltitude = -1000;
        double expectedMaxAltitude = 1000;

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(expectedWest, expectedSouth, expectedEast,
            expectedNorth, expectedMinAltitude, expectedMaxAltitude);

        assertEquals(expectedWest, boundingBox.getWest());
        assertEquals(expectedSouth, boundingBox.getSouth());
        assertEquals(expectedEast, boundingBox.getEast());
        assertEquals(expectedNorth, boundingBox.getNorth());
        assertEquals(expectedMinAltitude, boundingBox.getMinAltitude());
        assertEquals(expectedMaxAltitude, boundingBox.getMaxAltitude());
    }

    @Test
    public void constructorDoesNotValidate() {
        double expectedWest = -200;
        double expectedSouth = -100;
        double expectedEast = 200;
        double expectedNorth = 100;

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(expectedWest, expectedSouth, expectedEast,
            expectedNorth);

        assertEquals(expectedWest, boundingBox.getWest());
        assertEquals(expectedSouth, boundingBox.getSouth());
        assertEquals(expectedEast, boundingBox.getEast());
        assertEquals(expectedNorth, boundingBox.getNorth());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void geometryBoundingBoxEquals(GeometryBoundingBox boundingBox, Object obj, boolean expected) {
        assertEquals(expected, boundingBox.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GeometryBoundingBox pikePlaceBoundingBox = PIKES_PLACE_BOUNDING_BOX.get();
        GeometryBoundingBox mtRainierBoundingBox = MT_RAINIER_BOUNDING_BOX.get();

        return Stream.of(
            // Other is null.
            Arguments.of(pikePlaceBoundingBox, null, false),

            // Other isn't instance of type.
            Arguments.of(pikePlaceBoundingBox, 1, false),

            // Other is itself.
            Arguments.of(pikePlaceBoundingBox, pikePlaceBoundingBox, true),
            Arguments.of(mtRainierBoundingBox, mtRainierBoundingBox, true),

            // Other is a different value.
            Arguments.of(pikePlaceBoundingBox, mtRainierBoundingBox, false),
            Arguments.of(mtRainierBoundingBox, pikePlaceBoundingBox, false),

            // Other is the same value.
            Arguments.of(pikePlaceBoundingBox, PIKES_PLACE_BOUNDING_BOX.get(), true),
            Arguments.of(mtRainierBoundingBox, MT_RAINIER_BOUNDING_BOX.get(), true)
        );
    }
}
