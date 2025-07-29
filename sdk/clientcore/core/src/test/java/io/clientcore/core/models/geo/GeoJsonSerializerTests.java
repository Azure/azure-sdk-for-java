// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Tests GeoJson serialization.
 */
public class GeoJsonSerializerTests {
    @ParameterizedTest
    @MethodSource("serializeSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public <T extends GeoObject> void serialize(T geo, String expectedJson) throws IOException {
        Assertions.assertEquals(expectedJson, geo.toJsonString());
    }

    private static Stream<Arguments> serializeSupplier() {
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

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygonCollection> multiPolygonSupplier
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
            Arguments.of(serializerArgumentSupplier(null, null, multiPolygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiPolygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiPolygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiPolygonSupplier)),

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
