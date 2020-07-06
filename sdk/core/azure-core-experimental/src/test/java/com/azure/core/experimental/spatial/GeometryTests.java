// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link Geometry}.
 */
public class GeometryTests {
    @Test
    public void defaultGeometry() {
        Geometry geometry = new ConcreteGeometry(null, null);

        Assertions.assertNull(geometry.getProperties());
        Assertions.assertNull(geometry.getBoundingBox());
    }

    @Test
    public void geometryCopiesPropertiesToUnmodifiableMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", "value");

        Geometry geometry = new ConcreteGeometry(null, properties);

        assertEquals(properties, geometry.getProperties());

        properties.put("key2", "value2");
        assertNotEquals(properties, geometry.getProperties());

        Assertions.assertThrows(UnsupportedOperationException.class, () -> geometry.getProperties().put("key2", "value2"));
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void geometriesEqual(Geometry geometry, Object obj, boolean expected) {
        assertEquals(expected, geometry.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        Geometry geometry = new ConcreteGeometry(null, null);
        Geometry geometry1 = new ConcreteGeometry(null, Collections.singletonMap("key", "value"));

        return Stream.of(
            // Other is null.
            Arguments.of(geometry, null, false),

            // Other isn't instance of type.
            Arguments.of(geometry, 1, false),

            // Other is itself.
            Arguments.of(geometry, geometry, true),
            Arguments.of(geometry1, geometry1, true),

            // Other is a different value.
            Arguments.of(geometry, geometry1, false),
            Arguments.of(geometry1, geometry, false),

            // Other is the same value.
            Arguments.of(geometry, new ConcreteGeometry(null, null), true),
            Arguments.of(geometry1, new ConcreteGeometry(null, Collections.singletonMap("key", "value")), true)
        );
    }

    private static final class ConcreteGeometry extends Geometry {
        ConcreteGeometry(GeometryBoundingBox boundingBox, Map<String, Object> properties) {
            super(boundingBox, properties);
        }
    }
}
