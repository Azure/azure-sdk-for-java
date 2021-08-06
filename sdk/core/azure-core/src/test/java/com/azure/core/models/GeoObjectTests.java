// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

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
 * Tests {@link GeoObject}.
 */
public class GeoObjectTests {
    @Test
    public void defaultGeo() {
        GeoObject geoObject = new ConcreteGeoObject(null, null);

        Assertions.assertNull(geoObject.getCustomProperties());
        Assertions.assertNull(geoObject.getBoundingBox());
    }

    @Test
    public void geoObjectCopiesPropertiesToUnmodifiableMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", "value");

        GeoObject geoObject = new ConcreteGeoObject(null, properties);

        assertEquals(properties, geoObject.getCustomProperties());

        properties.put("key2", "value2");
        assertNotEquals(properties, geoObject.getCustomProperties());

        Assertions.assertThrows(UnsupportedOperationException.class, () -> geoObject.getCustomProperties().put("key2", "value2"));
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void geometriesEqual(GeoObject geoObject, Object obj, boolean expected) {
        assertEquals(expected, geoObject.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GeoObject geoObject = new ConcreteGeoObject(null, null);
        GeoObject geoObject1 = new ConcreteGeoObject(null, Collections.singletonMap("key", "value"));

        return Stream.of(
            // Other is null.
            Arguments.of(geoObject, null, false),

            // Other isn't instance of type.
            Arguments.of(geoObject, 1, false),

            // Other is itself.
            Arguments.of(geoObject, geoObject, true),
            Arguments.of(geoObject1, geoObject1, true),

            // Other is a different value.
            Arguments.of(geoObject, geoObject1, false),
            Arguments.of(geoObject1, geoObject, false),

            // Other is the same value.
            Arguments.of(geoObject, new ConcreteGeoObject(null, null), true),
            Arguments.of(geoObject1, new ConcreteGeoObject(null, Collections.singletonMap("key", "value")), true)
        );
    }

    private static final class ConcreteGeoObject extends GeoObject {
        ConcreteGeoObject(GeoBoundingBox boundingBox, Map<String, Object> properties) {
            super(boundingBox, properties);
        }

        @Override
        public GeoObjectType getType() {
            return null;
        }
    }
}
