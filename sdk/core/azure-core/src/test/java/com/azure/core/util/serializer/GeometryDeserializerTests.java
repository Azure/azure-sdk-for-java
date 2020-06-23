// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.models.spatial.CollectionGeometry;
import com.azure.core.models.spatial.Geometry;
import com.azure.core.models.spatial.GeometryBoundingBox;
import com.azure.core.models.spatial.GeometryPosition;
import com.azure.core.models.spatial.LineGeometry;
import com.azure.core.models.spatial.MultiLineGeometry;
import com.azure.core.models.spatial.MultiPointGeometry;
import com.azure.core.models.spatial.MultiPolygonGeometry;
import com.azure.core.models.spatial.PointGeometry;
import com.azure.core.models.spatial.PolygonGeometry;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.geometryToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link GeometryDeserializer}.
 */
public class GeometryDeserializerTests {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper().registerModule(GeometryDeserializer.MODULE);
    }

    @Test
    public void jsonWithoutTypeThrows() {
        String missingType = "{\"coordinates\":[0,0]}";
        assertThrows(IllegalStateException.class, () -> MAPPER.readValue(missingType, Geometry.class));
    }

    @Test
    public void jsonWithoutCoordinatesThrows() {
        String missingCoordinates = "{\"type\":\"Point\"}";
        assertThrows(IllegalStateException.class, () -> MAPPER.readValue(missingCoordinates, PointGeometry.class));
    }

    @Test
    public void unknownGeometryTypeThrows() {
        String unknownType = "{\"type\":\"Custom\",\"coordinates\":[0,0]}";
        assertThrows(IllegalStateException.class, () -> MAPPER.readValue(unknownType, Geometry.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"type\":\"Point\",\"coordinates\":[1]}",
        "{\"type\":\"Point\",\"coordinates\":[4,4,4,4]}"
    })
    public void invalidCoordinateCountThrows(String invalidCoordinateCount) {
        assertThrows(IllegalStateException.class, () -> MAPPER.readValue(invalidCoordinateCount, PointGeometry.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[2,2]}",
        "{\"type\":\"Point\",\"coordinates\":[0,0],\"bbox\":[8,8,8,8,8,8,8,8]}"
    })
    public void invalidBoundBoxThrows(String invalidBoundBox) {
        assertThrows(IllegalStateException.class, () -> MAPPER.readValue(invalidBoundBox, PointGeometry.class));
    }

    @Test
    public void collectionWithoutGeometriesThrows() {
        String invalidCollection = "{\"type\":\"GeometryCollection\"}";
        assertThrows(IllegalStateException.class, () -> MAPPER.readValue(invalidCollection, CollectionGeometry.class));
    }

    @ParameterizedTest
    @MethodSource("deserializeSupplier")
    public <T extends Geometry> void deserialize(String json, Class<T> type, T expectedGeometry) throws IOException {
        assertEquals(expectedGeometry, MAPPER.readValue(json, type));
    }

    private static Stream<Arguments> deserializeSupplier() {
        GeometryBoundingBox boundingBox = new GeometryBoundingBox(0, 0, 1, 1, 0D, 1D);
        Map<String, Object> simpleProperties = Collections.singletonMap("key", "value");
        Map<String, Object> arrayProperties = Collections.singletonMap("text", Arrays.asList("hello", "world"));

        Map<String, Object> crs = new HashMap<>();
        crs.put("type", "name");
        crs.put("properties", Collections.singletonMap("name", "EPSG:432"));
        Map<String, Object> objectProperties = Collections.singletonMap("crs", crs);

        BiFunction<GeometryBoundingBox, Map<String, Object>, PointGeometry> pointSupplier =
            (box, properties) -> new PointGeometry(new GeometryPosition(0, 0, 0D), box, properties);

        List<GeometryPosition> positions = Arrays.asList(new GeometryPosition(0, 0, 1D),
            new GeometryPosition(1, 1, 1D));
        BiFunction<GeometryBoundingBox, Map<String, Object>, LineGeometry> lineSupplier =
            (box, properties) -> new LineGeometry(positions, box, properties);

        List<LineGeometry> rings = Collections.singletonList(new LineGeometry(Arrays.asList(
            new GeometryPosition(0, 0, 1D), new GeometryPosition(0, 1, 1D),
            new GeometryPosition(1, 1, 1D), new GeometryPosition(0, 0, 1D)
        )));
        BiFunction<GeometryBoundingBox, Map<String, Object>, PolygonGeometry> polygonSupplier =
            (box, properties) -> new PolygonGeometry(rings, box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, MultiPointGeometry> multiPointSupplier =
            (box, properties) -> new MultiPointGeometry(Arrays.asList(pointSupplier.apply(null, null),
                pointSupplier.apply(null, null)), box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, MultiLineGeometry> multiLineSupplier =
            (box, properties) -> new MultiLineGeometry(Arrays.asList(lineSupplier.apply(null, null),
                lineSupplier.apply(null, null)), box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, MultiPolygonGeometry> multiPolygonSuppluer =
            (box, properties) -> new MultiPolygonGeometry(Arrays.asList(polygonSupplier.apply(null, null),
                polygonSupplier.apply(null, null)), box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, CollectionGeometry> collectionSupplier =
            (box, properties) -> new CollectionGeometry(Arrays.asList(pointSupplier.apply(null, null),
                multiLineSupplier.apply(box, properties), polygonSupplier.apply(box, properties)), box, properties);

        return Stream.of(
            // Point geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, pointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, pointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, pointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, pointSupplier)),

            // Line geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, lineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, lineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, lineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, lineSupplier)),

            // Polygon geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, polygonSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, polygonSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, polygonSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, polygonSupplier)),

            // Multi point geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, multiPointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiPointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiPointSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiPointSupplier)),

            // Multi line geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, multiLineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiLineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiLineSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiLineSupplier)),

            // Multi polygon geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, multiPolygonSuppluer)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, multiPolygonSuppluer)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, multiPolygonSuppluer)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, multiPolygonSuppluer)),

            // Collection geometry.
            Arguments.of(deserializerArgumentSupplier(null, null, collectionSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, simpleProperties, collectionSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, arrayProperties, collectionSupplier)),
            Arguments.of(deserializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier))
        );
    }

    private static Object[] deserializerArgumentSupplier(GeometryBoundingBox boundingBox,
        Map<String, Object> properties,
        BiFunction<GeometryBoundingBox, Map<String, Object>, ? extends Geometry> geometrySupplier) {
        Geometry geometry = geometrySupplier.apply(boundingBox, properties);
        return new Object[]{geometryToJson(geometry), geometry.getClass(), geometry};
    }
}
