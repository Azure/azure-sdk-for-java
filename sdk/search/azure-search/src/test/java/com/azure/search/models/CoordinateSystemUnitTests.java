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
        Assert.assertEquals("name", coordinateSystem.getType());
        Assert.assertEquals(1, coordinateSystem.getProperties().size());
        Assert.assertTrue(coordinateSystem.getProperties().get("name").startsWith("EPSG"));
    }

    @Test
    public void canValidate() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        Assert.assertTrue(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectType() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create()
            .setType("IncorrectType");

        Assert.assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithNullProperties() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().setProperties(null);

        Assert.assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithEmptyProperties() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().setProperties(new HashMap<>());

        Assert.assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectPropertiesSize() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.getProperties().put("IncorrectKey", "value");

        Assert.assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectProperty() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.getProperties().remove("name");
        coordinateSystem.getProperties().put("IncorrectKey", "value");

        Assert.assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectPropertyValue() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.getProperties().put("name", "IncorrectValue");

        Assert.assertFalse(coordinateSystem.isValid());
    }
}
