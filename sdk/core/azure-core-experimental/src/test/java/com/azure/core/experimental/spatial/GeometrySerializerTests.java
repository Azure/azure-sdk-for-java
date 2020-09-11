// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

import static com.azure.core.experimental.spatial.GeometrySerializationTestHelpers.geometryToJson;

/**
 * Tests {@link GeometrySerializer}.
 */
public class GeometrySerializerTests {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper().registerModule(GeometrySerializer.MODULE);
    }

    @Test
    public void unknownGeometryTypeThrows() {
        Assertions.assertThrows(IOException.class, () -> MAPPER.writeValueAsString(new CustomGeometry(null, null)));
    }

    private static final class CustomGeometry extends Geometry {
        protected CustomGeometry(GeometryBoundingBox boundingBox, Map<String, Object> properties) {
            super(boundingBox, properties);
        }
    }

    @ParameterizedTest
    @MethodSource("serializeSupplier")
    public <T extends Geometry> void serialize(T geometry, String expectedJson) throws IOException {
        String actualJson = MAPPER.writeValueAsString(geometry);
        Assertions.assertEquals(expectedJson, actualJson);
    }

    private static Stream<Arguments> serializeSupplier() {
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
                pointSupplier.apply(box, properties)), box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, MultiLineGeometry> multiLineSupplier =
            (box, properties) -> new MultiLineGeometry(Arrays.asList(lineSupplier.apply(null, null),
                lineSupplier.apply(box, properties)), box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, MultiPolygonGeometry> multiPolygonSuppluer =
            (box, properties) -> new MultiPolygonGeometry(Arrays.asList(polygonSupplier.apply(null, null),
                polygonSupplier.apply(box, properties)), box, properties);

        BiFunction<GeometryBoundingBox, Map<String, Object>, CollectionGeometry> collectionSupplier =
            (box, properties) -> new CollectionGeometry(Arrays.asList(pointSupplier.apply(null, null),
                multiPointSupplier.apply(box, properties)), box, properties);

        return Stream.of(
            // Point geometry.
            Arguments.of(serializerArgumentSupplier(null, null, pointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, pointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, pointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, pointSupplier)),

            // Line geometry.
            Arguments.of(serializerArgumentSupplier(null, null, lineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, lineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, lineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, lineSupplier)),

            // Polygon geometry.
            Arguments.of(serializerArgumentSupplier(null, null, polygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, polygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, polygonSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, polygonSupplier)),

            // Multi point geometry.
            Arguments.of(serializerArgumentSupplier(null, null, multiPointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiPointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiPointSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiPointSupplier)),

            // Multi line geometry.
            Arguments.of(serializerArgumentSupplier(null, null, multiLineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiLineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiLineSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiLineSupplier)),

            // Multi polygon geometry.
            Arguments.of(serializerArgumentSupplier(null, null, multiPolygonSuppluer)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, multiPolygonSuppluer)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, multiPolygonSuppluer)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, multiPolygonSuppluer)),

            // Collection geometry.
            Arguments.of(serializerArgumentSupplier(null, null, collectionSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, simpleProperties, collectionSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, arrayProperties, collectionSupplier)),
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier))
        );
    }

    private static Object[] serializerArgumentSupplier(GeometryBoundingBox boundingBox, Map<String, Object> properties,
        BiFunction<GeometryBoundingBox, Map<String, Object>, ? extends Geometry> geometrySupplier) {
        Geometry geometry = geometrySupplier.apply(boundingBox, properties);
        return new Object[]{geometry, geometryToJson(geometry)};
    }
}
