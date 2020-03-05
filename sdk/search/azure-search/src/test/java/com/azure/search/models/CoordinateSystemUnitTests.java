// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoordinateSystemUnitTests {
    @Test
    public void canCreate() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();

        assertNotNull(coordinateSystem);
        assertEquals("name", coordinateSystem.getType());
        assertEquals(1, coordinateSystem.getProperties().size());
        assertTrue(coordinateSystem.getProperties().get("name").startsWith("EPSG"));
    }

    @Test
    public void canValidate() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        assertTrue(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectType() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create()
            .setType("IncorrectType");

        assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithNullProperties() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().setProperties(null);

        assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithEmptyProperties() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create().setProperties(new HashMap<>());

        assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectPropertiesSize() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.getProperties().put("IncorrectKey", "value");

        assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectProperty() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.getProperties().remove("name");
        coordinateSystem.getProperties().put("IncorrectKey", "value");

        assertFalse(coordinateSystem.isValid());
    }

    @Test
    public void canInvalidateWithIncorrectPropertyValue() {
        CoordinateSystem coordinateSystem = CoordinateSystem.create();
        coordinateSystem.getProperties().put("name", "IncorrectValue");

        assertFalse(coordinateSystem.isValid());
    }
}
