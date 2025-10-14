// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.utils.IOExceptionCheckedFunction;
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
 * Tests GeoJson deserialization.
 */
public class GeoJsonDeserializerTests {
    @Test
    public void jsonWithoutTypeThrows() {
        String missingType = "{\"coordinates\":[0,0]}";
        Assertions.assertThrows(IllegalStateException.class, () -> deserialize(missingType, GeoObject::fromJson));
    }

    @Test
    public void jsonWithoutCoordinatesThrows() {
        String missingCoordinates = "{\"type\":\"Point\"}";
        Assertions.assertThrows(IllegalStateException.class, () -> deserialize(missingCoordinates, GeoPoint::fromJson));
    }

    @Test
    public void unknownGeoTypeThrows() {
        String unknownType = "{\"type\":\"Custom\",\"coordinates\":[0,0]}";
        Assertions.assertThrows(IllegalStateException.class, () -> deserialize(unknownType, GeoObject::fromJson));
    }

    @ParameterizedTest
    @ValueSource(
        strings = { "{\"type\":\"Point\",\"coordinates\":[1]}", "{\"type\":\"Point\",\"coordinates\":[4,4,4,4]}" })
    public void invalidCoordinateCountThrows(String invalidCoordinateCount) {
        Assertions.assertThrows(IllegalStateException.class,
            () -> deserialize(invalidCoordinateCount, GeoPoint::fromJson));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[2,2]}",
            "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[8,8,8,8,8,8,8,8]}" })
    public void invalidBoundBoxThrows(String invalidBoundBox) {
        Assertions.assertThrows(IllegalStateException.class, () -> deserialize(invalidBoundBox, GeoPoint::fromJson));
    }

    @Test
    public void collectionWithoutGeometriesThrows() {
        String invalidCollection = "{\"type\":\"GeometryCollection\"}";
        Assertions.assertThrows(IllegalStateException.class,
            () -> deserialize(invalidCollection, GeoCollection::fromJson));
    }

    @ParameterizedTest
    @MethodSource("deserializeSupplier")
    public <T extends GeoObject> void deserialize(String json, IOExceptionCheckedFunction<JsonReader, T> deserializer,
        T expectedGeo) throws IOException {
        Assertions.assertEquals(expectedGeo, deserialize(json, deserializer));
    }

    private static <T> T deserialize(String json, IOExceptionCheckedFunction<JsonReader, T> deserializer)
        throws IOException {
        try (JsonReader jsonReader = JsonReader.fromString(json)) {
            return deserializer.apply(jsonReader);
        }
    }

    private static Stream<Arguments> deserializeSupplier() {
        GeoBoundingBox boundingBox = new GeoBoundingBox(0, 0, 1, 1, 0D, 1D);
        Map<String, Object> simpleProperties = Collections.singletonMap("key", "value");
        Map<String, Object> arrayProperties = Collections.singletonMap("text", Arrays.asList("hello", "world"));

        Map<String, Object> crs = new HashMap<>();
        crs.put("type", "name");
        crs.put("properties", Collections.singletonMap("name", "EPSG:432"));
        Map<String, Object> objectProperties = Collections.singletonMap("crs", crs);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPoint> pointSupplier
            = (box, properties) -> new GeoPoint(new GeoPosition(0, 0, 0D), box, properties);

        List<GeoPosition> positions = Arrays.asList(new GeoPosition(0, 0, 1D), new GeoPosition(1, 1, 1D));
        BiFunction<GeoBoundingBox, Map<String, Object>, GeoLineString> lineSupplier
            = (box, properties) -> new GeoLineString(positions, box, properties);

        List<GeoLinearRing> rings = Collections.singletonList(new GeoLinearRing(Arrays.asList(new GeoPosition(0, 0, 1D),
            new GeoPosition(0, 1, 1D), new GeoPosition(1, 1, 1D), new GeoPosition(0, 0, 1D))));
        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygon> polygonSupplier
            = (box, properties) -> new GeoPolygon(rings, box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPointCollection> multiPointSupplier
            = (box, properties) -> new GeoPointCollection(
                Arrays.asList(pointSupplier.apply(null, null), pointSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoLineStringCollection> multiLineSupplier
            = (box, properties) -> new GeoLineStringCollection(
                Arrays.asList(lineSupplier.apply(null, null), lineSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygonCollection> multiPolygonSupplier
            = (box, properties) -> new GeoPolygonCollection(
                Arrays.asList(polygonSupplier.apply(null, null), polygonSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoCollection> collectionSupplier
            = (box, properties) -> new GeoCollection(Arrays.asList(pointSupplier.apply(null, null),
                multiLineSupplier.apply(box, properties), polygonSupplier.apply(box, properties)), box, properties);

        return Stream.of(
            // GeoPoint
            Arguments.of(deserializerArgumentSupplier(null, null, pointSupplier, GeoPoint::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, simpleProperties, pointSupplier, GeoPoint::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, pointSupplier, GeoPoint::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, objectProperties, pointSupplier, GeoPoint::fromJson)),

            // GeoLine
            Arguments.of(deserializerArgumentSupplier(null, null, lineSupplier, GeoLineString::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, simpleProperties, lineSupplier, GeoLineString::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, arrayProperties, lineSupplier, GeoLineString::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, objectProperties, lineSupplier, GeoLineString::fromJson)),

            // GeoPolygon
            Arguments.of(deserializerArgumentSupplier(null, null, polygonSupplier, GeoPolygon::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, simpleProperties, polygonSupplier, GeoPolygon::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, arrayProperties, polygonSupplier, GeoPolygon::fromJson)),
            Arguments
                .of(deserializerArgumentSupplier(boundingBox, objectProperties, polygonSupplier, GeoPolygon::fromJson)),

            // GeoPointCollection
            Arguments.of(deserializerArgumentSupplier(null, null, multiPointSupplier, GeoPointCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiPointSupplier,
                GeoPointCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiPointSupplier,
                GeoPointCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiPointSupplier,
                GeoPointCollection::fromJson)),

            // GeoLineCollection
            Arguments
                .of(deserializerArgumentSupplier(null, null, multiLineSupplier, GeoLineStringCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiLineSupplier,
                GeoLineStringCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiLineSupplier,
                GeoLineStringCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiLineSupplier,
                GeoLineStringCollection::fromJson)),

            // GeoPolygonCollection
            Arguments
                .of(deserializerArgumentSupplier(null, null, multiPolygonSupplier, GeoPolygonCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiPolygonSupplier,
                GeoPolygonCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiPolygonSupplier,
                GeoPolygonCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiPolygonSupplier,
                GeoPolygonCollection::fromJson)),

            // GeoCollection
            Arguments.of(deserializerArgumentSupplier(null, null, collectionSupplier, GeoCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, collectionSupplier,
                GeoCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, collectionSupplier,
                GeoCollection::fromJson)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier,
                GeoCollection::fromJson)));
    }

    private static <T extends GeoObject> Object[] deserializerArgumentSupplier(GeoBoundingBox boundingBox,
        Map<String, Object> properties, BiFunction<GeoBoundingBox, Map<String, Object>, T> geoSupplier,
        IOExceptionCheckedFunction<JsonReader, T> deserializer) {
        T geoObject = geoSupplier.apply(boundingBox, properties);
        return new Object[] { GeoSerializationTestHelpers.geoToJson(geoObject), deserializer, geoObject };
    }
}
