// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeoPointUnitTests {
    @Test
    public void canCreate() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);

        assertNotNull(geoPoint);
        assertEquals(100.0, geoPoint.getLongitude(), 0.0);
        assertEquals(1.0, geoPoint.getLatitude(), 0.0);
        assertNotNull(geoPoint.getCoordinateSystem());
    }

    @Test
    public void canCreateWithCrs() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);

        assertNotNull(geoPoint);
        assertEquals(100.0, geoPoint.getLongitude(), 0.0);
        assertEquals(1.0, geoPoint.getLatitude(), 0.0);
        assertNotNull(geoPoint.getCoordinateSystem());
        assertEquals("name", geoPoint.getCoordinateSystem().getType());
        assertEquals(1, geoPoint.getCoordinateSystem().getProperties().size());
        assertTrue(geoPoint.getCoordinateSystem().getProperties().get("name").startsWith("EPSG"));
    }

    @Test
    public void canValidateWithCrs() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);
        assertTrue(geoPoint.isValid());
    }

    @Test
    public void canValidateWithoutCrs() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);
        assertTrue(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLatitudeHi() {
        GeoPoint geoPoint = GeoPoint.create(180.1, 1.0);
        assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLatitudeLow() {
        GeoPoint geoPoint = GeoPoint.create(-180.1, 1.0);
        assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLongitudeHi() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 90.1);
        assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLongitudeLow() {
        GeoPoint geoPoint = GeoPoint.create(-100.0, -90.1);
        assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectCoordinatesSize() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 0.1)
            .setCoordinates(Collections.singletonList(100.0));

        assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithNullIncorrect() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 0.1)
            .setCoordinates(null);

        assertFalse(geoPoint.isValid());
    }

    @Test
    public void canFormatWithToString() {
        GeoPoint point = GeoPoint.create(7.678581, -122.131577);

        assertEquals("+7.678581-122.131577CRSEPSG:4326/", point.toString());
    }
}
