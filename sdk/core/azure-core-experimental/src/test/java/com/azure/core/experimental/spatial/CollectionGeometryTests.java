// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

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

import static com.azure.core.experimental.spatial.GeometryTestHelpers.RECTANGLE_LINE;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.RECTANGLE_POLYGON;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.SQUARE_LINE;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.SQUARE_POLYGON;
import static com.azure.core.experimental.spatial.GeometryTestHelpers.TRIANGLE_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link CollectionGeometry}.
 */
public class CollectionGeometryTests {
    @Test
    public void nullGeometriesThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new CollectionGeometry(null));
    }

    @Test
    public void simpleConstructor() {
        List<Geometry> geometries = Arrays.asList(SQUARE_LINE.get(), SQUARE_POLYGON.get());

        CollectionGeometry collection = new CollectionGeometry(geometries);

        assertEquals(geometries, collection.getGeometries());

        Assertions.assertNull(collection.getBoundingBox());
        Assertions.assertNull(collection.getProperties());
    }

    @Test
    public void complexConstructor() {
        List<Geometry> geometries = Arrays.asList(SQUARE_LINE.get(), SQUARE_POLYGON.get());
        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        CollectionGeometry collection = new CollectionGeometry(geometries, boundingBox, properties);

        assertEquals(geometries, collection.getGeometries());
        assertEquals(boundingBox, collection.getBoundingBox());
        assertEquals(properties, collection.getProperties());
    }

    @Test
    public void constructorCopiesGeometries() {
        List<Geometry> geometries = new ArrayList<>();
        geometries.add(SQUARE_LINE.get());
        geometries.add(SQUARE_POLYGON.get());

        CollectionGeometry collection = new CollectionGeometry(geometries);
        assertEquals(geometries, collection.getGeometries());

        geometries.add(TRIANGLE_LINE.get());
        assertNotEquals(geometries, collection.getGeometries());
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void collectionGeometriesEquals(CollectionGeometry collection, Object obj, boolean expected) {
        assertEquals(expected, collection.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        List<Geometry> geometries = Arrays.asList(SQUARE_LINE.get(), SQUARE_POLYGON.get());
        List<Geometry> geometries1 = Arrays.asList(RECTANGLE_LINE.get(), RECTANGLE_POLYGON.get());

        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 2, 2);
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        CollectionGeometry collection = new CollectionGeometry(geometries);
        CollectionGeometry collection1 = new CollectionGeometry(geometries1, boundingBox, properties);

        return Stream.of(
            // Other is null.
            Arguments.of(collection, null, false),

            // Other isn't instance of type.
            Arguments.of(collection, 1, false),

            // Other is itself.
            Arguments.of(collection, collection, true),
            Arguments.of(collection1, collection1, true),

            // Other is a different value.
            Arguments.of(collection, collection1, false),
            Arguments.of(collection1, collection, false),

            // Other is the same value.
            Arguments.of(collection, new CollectionGeometry(geometries), true),
            Arguments.of(collection1, new CollectionGeometry(geometries1, boundingBox, properties), true)
        );
    }
}
