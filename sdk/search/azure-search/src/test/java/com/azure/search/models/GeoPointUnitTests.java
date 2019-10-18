// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class GeoPointUnitTests {
    @Test
    public void canCreate() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);

        Assert.assertNotNull(geoPoint);
        Assert.assertNotNull(geoPoint.getCoordinates());
        Assert.assertEquals(2, geoPoint.getCoordinates().size());
        Assert.assertEquals(100.0, geoPoint.getCoordinates().get(0), 0.0);
        Assert.assertEquals(1.0, geoPoint.getCoordinates().get(1), 0.0);
        Assert.assertNotNull(geoPoint.getCoordinateSystem());
    }

    @Test
    public void canCreateWithCrs() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);

        Assert.assertNotNull(geoPoint);
        Assert.assertNotNull(geoPoint.getCoordinates());
        Assert.assertEquals(2, geoPoint.getCoordinates().size());
        Assert.assertEquals(100.0, geoPoint.getCoordinates().get(0), 0.0);
        Assert.assertEquals(1.0, geoPoint.getCoordinates().get(1), 0.0);
        Assert.assertNotNull(geoPoint.getCoordinateSystem());
        Assert.assertEquals("name", geoPoint.getCoordinateSystem().getType());
        Assert.assertEquals(1, geoPoint.getCoordinateSystem().getProperties().size());
        Assert.assertTrue(geoPoint.getCoordinateSystem().getProperties().get("name").startsWith("EPSG"));
    }

    @Test
    public void canValidateWithCrs() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);
        Assert.assertTrue(geoPoint.isValid());
    }

    @Test
    public void canValidateWithoutCrs() {
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);
        Assert.assertTrue(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLatitudeHi() {
        GeoPoint geoPoint = GeoPoint.create(180.1, 1.0);
        Assert.assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLatitudeLow() {
        GeoPoint geoPoint = GeoPoint.create(-180.1, 1.0);
        Assert.assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLongitudeHi() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 90.1);
        Assert.assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectLongitudeLow() {
        GeoPoint geoPoint = GeoPoint.create(-100.0, -90.1);
        Assert.assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectCoordinatesSize() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 0.1)
            .setCoordinates(Collections.singletonList(100.0));

        Assert.assertFalse(geoPoint.isValid());
    }

    @Test
    public void canInvalidateWithNullIncorrect() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 0.1)
            .setCoordinates(null);

        Assert.assertFalse(geoPoint.isValid());
    }
}
