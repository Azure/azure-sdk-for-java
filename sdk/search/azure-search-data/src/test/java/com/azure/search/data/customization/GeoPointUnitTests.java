// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.customization.models.GeoPoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GeoPointUnitTests {
    @Test
    public void canCreate() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 1.0);

        Assert.assertNotNull(geoPoint);
        Assert.assertNotNull(geoPoint.coordinates());
        Assert.assertEquals(2, geoPoint.coordinates().size());
        Assert.assertEquals(100.0, geoPoint.coordinates().get(0), 0.0);
        Assert.assertEquals(1.0, geoPoint.coordinates().get(1), 0.0);
        Assert.assertNull(geoPoint.coordinateSystem());
    }

    @Test
    public void canCreateWithCrs() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(100.0, 1.0);

        Assert.assertNotNull(geoPoint);
        Assert.assertNotNull(geoPoint.coordinates());
        Assert.assertEquals(2, geoPoint.coordinates().size());
        Assert.assertEquals(100.0, geoPoint.coordinates().get(0), 0.0);
        Assert.assertEquals(1.0, geoPoint.coordinates().get(1), 0.0);
        Assert.assertNotNull(geoPoint.coordinateSystem());
        Assert.assertEquals("name", geoPoint.coordinateSystem().type());
        Assert.assertEquals(1, geoPoint.coordinateSystem().properties().size());
        Assert.assertTrue(geoPoint.coordinateSystem().properties().get("name").startsWith("EPSG"));
    }

    @Test
    public void canCreateMap() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 1.0);
        Map geoPointMap = geoPoint.createObjectMap();

        Assert.assertNotNull(geoPointMap);
        Assert.assertTrue(geoPointMap.containsKey("type"));
        Assert.assertEquals("Point", geoPointMap.get("type"));

        Assert.assertTrue(geoPointMap.containsKey("coordinates"));
        Assert.assertTrue(geoPointMap.get("coordinates") instanceof List);
        List<Double> coordinates = (List) geoPointMap.get("coordinates");
        Assert.assertEquals(2, coordinates.size());
        Assert.assertEquals(100.0, coordinates.get(0), 0.0);
        Assert.assertEquals(1.0, coordinates.get(1), 0.0);
    }

    @Test
    public void canValidateWithCrs() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(100.0, 1.0);
        Assert.assertTrue(geoPoint.validate());
    }

    @Test
    public void canValidateWithoutCrs() {
        GeoPoint geoPoint = GeoPoint.create(100.0, 1.0);
        Assert.assertTrue(geoPoint.validate());
    }

    @Test
    public void canInvalidateWithIncorrectLatitudeHi() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(180.1, 1.0);
        Assert.assertFalse(geoPoint.validate());
    }

    @Test
    public void canInvalidateWithIncorrectLatitudeLow() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(-180.1, 1.0);
        Assert.assertFalse(geoPoint.validate());
    }

    @Test
    public void canInvalidateWithIncorrectLongitudeHi() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(100.0, 90.1);
        Assert.assertFalse(geoPoint.validate());
    }

    @Test
    public void canInvalidateWithIncorrectLongitudeLow() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(-100.0, -90.1);
        Assert.assertFalse(geoPoint.validate());
    }

    @Test
    public void canInvalidateWithIncorrectCoordinatesSize() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(100.0, 0.1);
        geoPoint.coordinates(Collections.singletonList(100.0));

        Assert.assertFalse(geoPoint.validate());
    }

    @Test
    public void canInvalidateWithNullIncorrect() {
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(100.0, 0.1);
        geoPoint.coordinates(null);

        Assert.assertFalse(geoPoint.validate());
    }
}
