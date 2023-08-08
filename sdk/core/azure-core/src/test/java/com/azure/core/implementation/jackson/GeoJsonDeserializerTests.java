// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLineStringCollection;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPointCollection;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Tests {@link GeoJsonDeserializer}.
 */
public class GeoJsonDeserializerTests {
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();

    @Test
    public void jsonWithoutTypeThrows() {
        String missingType = "{\"coordinates\":[0,0]}";
        Assertions.assertThrows(IllegalStateException.class,
            () -> ADAPTER.deserialize(missingType, GeoObject.class, SerializerEncoding.JSON));
    }

    @Test
    public void jsonWithoutCoordinatesThrows() {
        String missingCoordinates = "{\"type\":\"Point\"}";
        Assertions.assertThrows(IllegalStateException.class,
            () -> ADAPTER.deserialize(missingCoordinates, GeoPoint.class, SerializerEncoding.JSON));
    }

    @Test
    public void unknownGeoTypeThrows() {
        String unknownType = "{\"type\":\"Custom\",\"coordinates\":[0,0]}";
        Assertions.assertThrows(IllegalStateException.class,
            () -> ADAPTER.deserialize(unknownType, GeoObject.class, SerializerEncoding.JSON));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"type\":\"Point\",\"coordinates\":[1]}",
        "{\"type\":\"Point\",\"coordinates\":[4,4,4,4]}"
    })
    public void invalidCoordinateCountThrows(String invalidCoordinateCount) {
        Assertions.assertThrows(IllegalStateException.class,
            () -> ADAPTER.deserialize(invalidCoordinateCount, GeoPoint.class, SerializerEncoding.JSON));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[2,2]}",
        "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[8,8,8,8,8,8,8,8]}"
    })
    public void invalidBoundBoxThrows(String invalidBoundBox) {
        Assertions.assertThrows(IllegalStateException.class,
            () -> ADAPTER.deserialize(invalidBoundBox, GeoPoint.class, SerializerEncoding.JSON));
    }

    @Test
    public void collectionWithoutGeometriesThrows() {
        String invalidCollection = "{\"type\":\"GeometryCollection\"}";
        Assertions.assertThrows(IllegalStateException.class,
            () -> ADAPTER.deserialize(invalidCollection, GeoCollection.class, SerializerEncoding.JSON));
    }

    @ParameterizedTest
    @MethodSource("deserializeSupplier")
    public <T extends GeoObject> void deserialize(String json, Class<T> type, T expectedGeo) throws IOException {
        Assertions.assertEquals(expectedGeo, ADAPTER.deserialize(json, type, SerializerEncoding.JSON));
    }

    private static Stream<Arguments> deserializeSupplier() {
        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1, 0D, 1D);
        Map<String, Object> simpleProperties = Collections.singletonMap("key", "value");
        Map<String, Object> arrayProperties = Collections.singletonMap("text", Arrays.asList("hello", "world"));

        Map<String, Object> crs = new HashMap<>();
        crs.put("type", "name");
        crs.put("properties", Collections.singletonMap("name", "EPSG:432"));
        Map<String, Object> objectProperties = Collections.singletonMap("crs", crs);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPoint> pointSupplier =
            (box, properties) -> new GeoPoint(new GeoPosition(0, 0, 0D), box, properties);

        List<GeoPosition> positions = Arrays.asList(new GeoPosition(0, 0, 1D),
            new GeoPosition(1, 1, 1D));
        BiFunction<GeoBoundingBox, Map<String, Object>, GeoLineString> lineSupplier =
            (box, properties) -> new GeoLineString(positions, box, properties);

        List<GeoLinearRing> rings = Collections.singletonList(new GeoLinearRing(Arrays.asList(
            new GeoPosition(0, 0, 1D), new GeoPosition(0, 1, 1D),
            new GeoPosition(1, 1, 1D), new GeoPosition(0, 0, 1D)
        )));
        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygon> polygonSupplier =
            (box, properties) -> new GeoPolygon(rings, box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPointCollection> multiPointSupplier =
            (box, properties) -> new GeoPointCollection(Arrays.asList(pointSupplier.apply(null, null),
                pointSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoLineStringCollection> multiLineSupplier =
            (box, properties) -> new GeoLineStringCollection(Arrays.asList(lineSupplier.apply(null, null),
                lineSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygonCollection> multiPolygonSuppluer =
            (box, properties) -> new GeoPolygonCollection(Arrays.asList(polygonSupplier.apply(null, null),
                polygonSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoCollection> collectionSupplier =
            (box, properties) -> new GeoCollection(Arrays.asList(pointSupplier.apply(null, null),
                multiLineSupplier.apply(box, properties), polygonSupplier.apply(box, properties)), box, properties);

        return Stream.of(
            // GeoPoint
            Arguments.of(deserializerArgumentSupplier(null, null, pointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, pointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, pointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, pointSupplier)),

            // GeoLine
            Arguments.of(deserializerArgumentSupplier(null, null, lineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, lineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, lineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, lineSupplier)),

            // GeoPolygon
            Arguments.of(deserializerArgumentSupplier(null, null, polygonSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, polygonSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, polygonSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, polygonSupplier)),

            // GeoPointCollection
            Arguments.of(deserializerArgumentSupplier(null, null, multiPointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiPointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiPointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiPointSupplier)),

            // GeoLineCollection
            Arguments.of(deserializerArgumentSupplier(null, null, multiLineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiLineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiLineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiLineSupplier)),

            // GeoPolygonCollection
            Arguments.of(deserializerArgumentSupplier(null, null, multiPolygonSuppluer)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiPolygonSuppluer)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiPolygonSuppluer)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiPolygonSuppluer)),

            // GeoCollection
            Arguments.of(deserializerArgumentSupplier(null, null, collectionSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, collectionSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, collectionSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier))
        );
    }

    private static Object[] deserializerArgumentSupplier(GeoBoundingBox boundingBox,
        Map<String, Object> properties,
        BiFunction<GeoBoundingBox, Map<String, Object>, ? extends GeoObject> geoSupplier) {
        GeoObject geoObject = geoSupplier.apply(boundingBox, properties);
        return new Object[]{GeoSerializationTestHelpers.geoToJson(geoObject), geoObject.getClass(), geoObject};
    }
}
