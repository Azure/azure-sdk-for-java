// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.models.GeoTestHelpers.RECTANGLE_LINE;
import static com.azure.core.models.GeoTestHelpers.RECTANGLE_POLYGON;
import static com.azure.core.models.GeoTestHelpers.SQUARE_LINE;
import static com.azure.core.models.GeoTestHelpers.SQUARE_POLYGON;
import static com.azure.core.models.GeoTestHelpers.TRIANGLE_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link GeoCollection}.
 */
public class GeoCollectionTests {
    @Test
    public void nullGeometriesThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new GeoCollection(null));
    }

    @Test
    public void simpleConstructor() {
        List<GeoObject> geometries = Arrays.asList(SQUARE_LINE.get(), SQUARE_POLYGON.get());

        GeoCollection collection = new GeoCollection(geometries);

        assertEquals(geometries, collection.getGeometries());

        Assertions.assertNull(collection.getBoundingBox());
        Assertions.assertNull(collection.getCustomProperties());
    }

    @Test
    public void complexConstructor() {
        List<GeoObject> geometries = Arrays.asList(SQUARE_LINE.get(), SQUARE_POLYGON.get());
        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoCollection collection = new GeoCollection(geometries, boundingBox, properties);

        assertEquals(geometries, collection.getGeometries());
        assertEquals(boundingBox, collection.getBoundingBox());
        assertEquals(properties, collection.getCustomProperties());
    }

    @Test
    public void constructorCopiesGeometries() {
        List<GeoObject> geometries = new ArrayList<>();
        geometries.add(SQUARE_LINE.get());
        geometries.add(SQUARE_POLYGON.get());

        GeoCollection collection = new GeoCollection(geometries);
        assertEquals(geometries, collection.getGeometries());

        geometries.add(TRIANGLE_LINE.get());
        assertNotEquals(geometries, collection.getGeometries());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void collectionGeometriesEquals(GeoCollection collection, Object obj, boolean expected) {
        assertEquals(expected, collection.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<GeoObject> geometries = Arrays.asList(SQUARE_LINE.get(), SQUARE_POLYGON.get());
        List<GeoObject> geometries1 = Arrays.asList(RECTANGLE_LINE.get(), RECTANGLE_POLYGON.get());

        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 2, 2);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        GeoCollection collection = new GeoCollection(geometries);
        GeoCollection collection1 = new GeoCollection(geometries1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(collection, null, false),

            // Other isn't instance of type.
            Arguments.of(collection, 1, false),

            // Other is itself.
            Arguments.of(collection, collection, true), Arguments.of(collection1, collection1, true),

            // Other is a different value.
            Arguments.of(collection, collection1, false), Arguments.of(collection1, collection, false),

            // Other is the same value.
            Arguments.of(collection, new GeoCollection(geometries), true),
            Arguments.of(collection1, new GeoCollection(geometries1, boundingBox, properties), true));
    }
}
