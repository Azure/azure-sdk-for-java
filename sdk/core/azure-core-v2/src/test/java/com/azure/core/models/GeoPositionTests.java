// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.core.models.GeoTestHelpers.MT_RAINIER_POSITION;
import static com.azure.core.models.GeoTestHelpers.PIKES_PLACE_POSITION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeoPositionTests {
    @Test
    public void simpleConstructor() {
        double expectedLongitude = -180;
        double expectedLatitude = -90;

        GeoPosition position = new GeoPosition(expectedLongitude, expectedLatitude);

        assertEquals(expectedLongitude, position.getLongitude());
        assertEquals(expectedLatitude, position.getLatitude());

        Assertions.assertNull(position.getAltitude());
    }

    @Test
    public void complexConstructor() {
        double expectedLongitude = -180;
        double expectedLatitude = -90;
        double expectedAltitude = 1000;

        GeoPosition position = new GeoPosition(expectedLongitude, expectedLatitude, expectedAltitude);

        assertEquals(expectedLongitude, position.getLongitude());
        assertEquals(expectedLatitude, position.getLatitude());
        assertEquals(expectedAltitude, position.getAltitude());
    }

    @Test
    public void constructorDoesNotValidate() {
        double expectedLongitude = -200;
        double expectedLatitude = -100;

        GeoPosition position = new GeoPosition(expectedLongitude, expectedLatitude);

        assertEquals(expectedLongitude, position.getLongitude());
        assertEquals(expectedLatitude, position.getLatitude());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void geoPositionEquals(GeoPosition position, Object obj, boolean expected) {
        assertEquals(expected, position.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GeoPosition pikePlacePosition = PIKES_PLACE_POSITION.get();
        GeoPosition mtRainierPosition = MT_RAINIER_POSITION.get();

        return Stream.of(
            // Other is null.
            Arguments.of(pikePlacePosition, null, false),

            // Other isn't instance of type.
            Arguments.of(pikePlacePosition, 1, false),

            // Other is itself.
            Arguments.of(pikePlacePosition, pikePlacePosition, true),
            Arguments.of(mtRainierPosition, mtRainierPosition, true),

            // Other is a different value.
            Arguments.of(pikePlacePosition, mtRainierPosition, false),
            Arguments.of(mtRainierPosition, pikePlacePosition, false),

            // Other is the same value.
            Arguments.of(pikePlacePosition, PIKES_PLACE_POSITION.get(), true),
            Arguments.of(mtRainierPosition, MT_RAINIER_POSITION.get(), true));
    }
}
