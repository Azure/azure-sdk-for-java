// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests;

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
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@code DateTimeDeserializer}.
 */
public class CustomSerializerTests {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    @ParameterizedTest
    @MethodSource("offsetDateTimeDeserializationSupplier")
    public void deserializeOffsetDateTime(String dateTimeJson, OffsetDateTime expected) throws IOException {
        assertEquals(expected, ADAPTER.deserialize(dateTimeJson, OffsetDateTime.class, SerializerEncoding.JSON));
    }

    private static Stream<Arguments> offsetDateTimeDeserializationSupplier() {
        return offsetDateTimeSupplier(true);
    }

    @ParameterizedTest
    @MethodSource("offsetDateTimeSerializationSupplier")
    public void serializeOffsetDateTime(String expected, OffsetDateTime dateTime) throws IOException {
        assertEquals(expected, ADAPTER.serialize(dateTime, SerializerEncoding.JSON));
    }

    private static Stream<Arguments> offsetDateTimeSerializationSupplier() {
        return offsetDateTimeSupplier(false);
    }

    private static Stream<Arguments> offsetDateTimeSupplier(boolean deserialization) {
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        OffsetDateTime nonUtcTimeZone = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-7));

        if (deserialization) {
            return Stream.of(Arguments.of("\"0001-01-01T00:00:00\"", minValue),
                Arguments.of(String.valueOf(minValue.toEpochSecond()), minValue),
                Arguments.of("\"0001-01-01T00:00:00Z\"", minValue), Arguments.of("\"1970-01-01T00:00:00\"", unixEpoch),
                Arguments.of("\"1970-01-01T00:00:00Z\"", unixEpoch),
                Arguments.of("\"2020-01-01T00:00:00-07:00\"", nonUtcTimeZone));
        } else {
            return Stream.of(Arguments.of("\"0001-01-01T00:00:00Z\"", minValue),
                Arguments.of("\"0001-01-01T00:00:00Z\"", minValue), Arguments.of("\"1970-01-01T00:00:00Z\"", unixEpoch),
                Arguments.of("\"1970-01-01T00:00:00Z\"", unixEpoch),
                Arguments.of("\"2020-01-01T07:00:00Z\"", nonUtcTimeZone));
        }
    }

    @ParameterizedTest
    @MethodSource("toStringTestSupplier")
    public void toStringTest(Duration duration, String expected) throws IOException {
        assertEquals(expected, ADAPTER.serialize(duration, SerializerEncoding.JSON));
    }

    private static Stream<Arguments> toStringTestSupplier() {
        return Stream.of(Arguments.of(Duration.ofMillis(0), "\"PT0S\""),
            Arguments.of(Duration.ofMillis(1), "\"PT0.001S\""), Arguments.of(Duration.ofMillis(9), "\"PT0.009S\""),
            Arguments.of(Duration.ofMillis(10), "\"PT0.01S\""), Arguments.of(Duration.ofMillis(11), "\"PT0.011S\""),
            Arguments.of(Duration.ofMillis(99), "\"PT0.099S\""), Arguments.of(Duration.ofMillis(100), "\"PT0.1S\""),
            Arguments.of(Duration.ofMillis(101), "\"PT0.101S\""), Arguments.of(Duration.ofMillis(999), "\"PT0.999S\""),
            Arguments.of(Duration.ofMillis(1000), "\"PT1S\""), Arguments.of(Duration.ofSeconds(1), "\"PT1S\""),
            Arguments.of(Duration.ofSeconds(9), "\"PT9S\""), Arguments.of(Duration.ofSeconds(10), "\"PT10S\""),
            Arguments.of(Duration.ofSeconds(11), "\"PT11S\""), Arguments.of(Duration.ofSeconds(59), "\"PT59S\""),
            Arguments.of(Duration.ofSeconds(60), "\"PT1M\""), Arguments.of(Duration.ofSeconds(61), "\"PT1M1S\""),
            Arguments.of(Duration.ofMinutes(1), "\"PT1M\""), Arguments.of(Duration.ofMinutes(9), "\"PT9M\""),
            Arguments.of(Duration.ofMinutes(10), "\"PT10M\""), Arguments.of(Duration.ofMinutes(11), "\"PT11M\""),
            Arguments.of(Duration.ofMinutes(59), "\"PT59M\""), Arguments.of(Duration.ofMinutes(60), "\"PT1H\""),
            Arguments.of(Duration.ofMinutes(61), "\"PT1H1M\""), Arguments.of(Duration.ofHours(1), "\"PT1H\""),
            Arguments.of(Duration.ofHours(9), "\"PT9H\""), Arguments.of(Duration.ofHours(10), "\"PT10H\""),
            Arguments.of(Duration.ofHours(11), "\"PT11H\""), Arguments.of(Duration.ofHours(23), "\"PT23H\""),
            Arguments.of(Duration.ofHours(24), "\"P1D\""), Arguments.of(Duration.ofHours(25), "\"P1DT1H\""),
            Arguments.of(Duration.ofDays(1), "\"P1D\""), Arguments.of(Duration.ofDays(9), "\"P9D\""),
            Arguments.of(Duration.ofDays(10), "\"P10D\""), Arguments.of(Duration.ofDays(11), "\"P11D\""),
            Arguments.of(Duration.ofDays(99), "\"P99D\""), Arguments.of(Duration.ofDays(100), "\"P100D\""),
            Arguments.of(Duration.ofDays(101), "\"P101D\""));
    }

    @ParameterizedTest
    @MethodSource("invalidGeoJsonDeserializationSupplier")
    public void invalidGeoJsonDeserializationThrowsIllegalStateException(String invalidGeoJson) {
        Assertions.assertThrows(IOException.class,
            () -> ADAPTER.deserialize(invalidGeoJson, GeoObject.class, SerializerEncoding.JSON));
    }

    private static Stream<String> invalidGeoJsonDeserializationSupplier() {
        return Stream.of("{\"coordinates\":[0,0]}", // Missing type
            "{\"type\":\"Point\"}", // Missing coordinates
            "{\"type\":\"Custom\",\"coordinates\":[0,0]}", // Invalid/unknown type
            "{\"type\":\"Point\",\"coordinates\":[1]}", // Invalid coordinates count
            "{\"type\":\"Point\",\"coordinates\":[4,4,4,4]}", // Invalid coordinates count
            "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[2,2]}", // Invalid bounding box
            "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[8,8,8,8,8,8,8,8]}", // Invalid bounding box
            "{\"type\":\"GeometryCollection\"}" // Collection without geometries
        );
    }

    @ParameterizedTest
    @MethodSource("geoJsonDeserializationSupplier")
    public <T extends GeoObject> void geoJsonDeserialization(String json, Class<T> type, T expectedGeo)
        throws IOException {
        Assertions.assertEquals(expectedGeo, ADAPTER.deserialize(json, type, SerializerEncoding.JSON));
    }

    private static Stream<Arguments> geoJsonDeserializationSupplier() {
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

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygonCollection> multiPolygonSuppluer
            = (box, properties) -> new GeoPolygonCollection(
                Arrays.asList(polygonSupplier.apply(null, null), polygonSupplier.apply(null, null)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoCollection> collectionSupplier
            = (box, properties) -> new GeoCollection(Arrays.asList(pointSupplier.apply(null, null),
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
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier)));
    }

    private static Object[] deserializerArgumentSupplier(GeoBoundingBox boundingBox, Map<String, Object> properties,
        BiFunction<GeoBoundingBox, Map<String, Object>, ? extends GeoObject> geoSupplier) {
        GeoObject geoObject = geoSupplier.apply(boundingBox, properties);
        return new Object[] { GeoSerializationTestHelpers.geoToJson(geoObject), geoObject.getClass(), geoObject };
    }

    @ParameterizedTest
    @MethodSource("geoJsonSerializationSupplier")
    public <T extends GeoObject> void geoJsonSerialization(T geo, String expectedJson) throws IOException {
        String actualJson = ADAPTER.serialize(geo, SerializerEncoding.JSON);
        Assertions.assertEquals(expectedJson, actualJson);
    }

    private static Stream<Arguments> geoJsonSerializationSupplier() {
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
                Arrays.asList(pointSupplier.apply(null, null), pointSupplier.apply(box, properties)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoLineStringCollection> multiLineSupplier
            = (box, properties) -> new GeoLineStringCollection(
                Arrays.asList(lineSupplier.apply(null, null), lineSupplier.apply(box, properties)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygonCollection> multiPolygonSuppluer
            = (box, properties) -> new GeoPolygonCollection(
                Arrays.asList(polygonSupplier.apply(null, null), polygonSupplier.apply(box, properties)), box,
                properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoCollection> collectionSupplier
            = (box, properties) -> new GeoCollection(
                Arrays.asList(pointSupplier.apply(null, null), multiPointSupplier.apply(box, properties)), box,
                properties);

        return Stream.of(
            // GeoPoint
            Arguments.of(serializerArgumentSupplier(null, null, pointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, pointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, pointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, pointSupplier)),

            // GeoLine
            Arguments.of(serializerArgumentSupplier(null, null, lineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, lineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, lineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, lineSupplier)),

            // GeoPolygon
            Arguments.of(serializerArgumentSupplier(null, null, polygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, polygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, polygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, polygonSupplier)),

            // GeoPointCollection
            Arguments.of(serializerArgumentSupplier(null, null, multiPointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiPointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiPointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiPointSupplier)),

            // GeoLineCollection
            Arguments.of(serializerArgumentSupplier(null, null, multiLineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiLineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiLineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiLineSupplier)),

            // GeoPolygonCollection
            Arguments.of(serializerArgumentSupplier(null, null, multiPolygonSuppluer)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiPolygonSuppluer)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiPolygonSuppluer)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiPolygonSuppluer)),

            // GeoCollection
            Arguments.of(serializerArgumentSupplier(null, null, collectionSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, collectionSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, collectionSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier)));
    }

    private static Object[] serializerArgumentSupplier(GeoBoundingBox boundingBox, Map<String, Object> properties,
        BiFunction<GeoBoundingBox, Map<String, Object>, ? extends GeoObject> geoSupplier) {
        GeoObject geoObject = geoSupplier.apply(boundingBox, properties);
        return new Object[] { geoObject, GeoSerializationTestHelpers.geoToJson(geoObject) };
    }
}
