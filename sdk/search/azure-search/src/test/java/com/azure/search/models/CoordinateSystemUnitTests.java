// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class CoordinateSystemUnitTests {
    @Test
    public void canCreate() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();

        Assert.assertNotNull(coordinateSystem);
        Assert.assertEquals("name", coordinateSystem.type());
        Assert.assertEquals(1, coordinateSystem.properties().size());
        Assert.assertTrue(coordinateSystem.properties().get("name").startsWith("EPSG"));
    }

    @Test
    public void canValidate() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        Assert.assertTrue(coordinateSystem.validate());
    }

    @Test
    public void canInvalidateWithIncorrectType() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().type("IncorrectType");
        Assert.assertFalse(coordinateSystem.validate());
    }

    @Test
    public void canInvalidateWithNullProperties() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().properties(null);
        Assert.assertFalse(coordinateSystem.validate());
    }

    @Test
    public void canInvalidateWithEmptyProperties() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().properties(new HashMap<>());
        Assert.assertFalse(coordinateSystem.validate());
    }

    @Test
    public void canInvalidateWithIncorrectPropertiesSize() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.properties().put("IncorrectKey", "value");
        Assert.assertFalse(coordinateSystem.validate());
    }

    @Test
    public void canInvalidateWithIncorrectProperty() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.properties().remove("name");
        coordinateSystem.properties().put("IncorrectKey", "value");
        Assert.assertFalse(coordinateSystem.validate());
    }

    @Test
    public void canInvalidateWithIncorrectPropertyValue() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.properties().put("name", "IncorrectValue");
        Assert.assertFalse(coordinateSystem.validate());
    }
}
