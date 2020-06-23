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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.collectionToJson;
import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.lineToJson;
import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.multiLineToJson;
import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.multiPointToJson;
import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.multiPolygonToJson;
import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.pointToJson;
import static com.azure.core.util.serializer.GeometrySerializationTestHelpers.polygonToJson;
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
        Map<String, Object> properties = Collections.singletonMap("key", "value");

        PointGeometry point = new PointGeometry(new GeometryPosition(0, 0));
        PointGeometry pointWithProperties = new PointGeometry(new GeometryPosition(0, 0, 0D), boundingBox, properties);

        List<GeometryPosition> positions = Arrays.asList(new GeometryPosition(0, 0), new GeometryPosition(1, 1));
        LineGeometry line = new LineGeometry(positions);

        List<GeometryPosition> positions1 = Arrays.asList(new GeometryPosition(0, 0, 1D),
            new GeometryPosition(1, 1, 1D));
        LineGeometry lineWithProperties = new LineGeometry(positions1, boundingBox, properties);

        List<LineGeometry> rings = Collections.singletonList(new LineGeometry(Arrays.asList(
            new GeometryPosition(0, 0), new GeometryPosition(0, 1),
            new GeometryPosition(1, 1), new GeometryPosition(0, 0)
        )));
        PolygonGeometry polygon = new PolygonGeometry(rings);

        List<LineGeometry> rings1 = Collections.singletonList(new LineGeometry(Arrays.asList(
            new GeometryPosition(0, 0, 1D), new GeometryPosition(0, 1, 1D),
            new GeometryPosition(1, 1, 1D), new GeometryPosition(0, 0, 1D)
        )));
        PolygonGeometry polygonWithProperties = new PolygonGeometry(rings1, boundingBox, properties);

        MultiPointGeometry multiPoint = new MultiPointGeometry(Arrays.asList(point,
            new PointGeometry(new GeometryPosition(0, 0, 0D))));

        MultiPointGeometry multiPointWithProperties = new MultiPointGeometry(Arrays.asList(point,
            new PointGeometry(new GeometryPosition(0, 0, 0D))), boundingBox, properties);

        MultiLineGeometry multiLine = new MultiLineGeometry(Arrays.asList(line, new LineGeometry(positions1)));

        MultiLineGeometry multiLineWithProperties = new MultiLineGeometry(Arrays.asList(line,
            new LineGeometry(positions1)), boundingBox, properties);

        MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(Arrays.asList(polygon,
            new PolygonGeometry(rings1)));

        MultiPolygonGeometry multiPolygonWithProperties  = new MultiPolygonGeometry(Arrays.asList(polygon,
            new PolygonGeometry(rings1)), boundingBox, properties);

        CollectionGeometry collection = new CollectionGeometry(Arrays.asList(point, multiPointWithProperties));

        CollectionGeometry collectionWithProperties = new CollectionGeometry(Arrays.asList(point,
            multiPointWithProperties), boundingBox, properties);

        return Stream.of(
            // Point geometry.
            Arguments.of(pointToJson(point), PointGeometry.class, point),

            // Point geometry with properties.
            Arguments.of(pointToJson(pointWithProperties), PointGeometry.class, pointWithProperties),

            // Line geometry.
            Arguments.of(lineToJson(line), LineGeometry.class, line),

            // Line geometry with properties.
            Arguments.of(lineToJson(lineWithProperties), LineGeometry.class, lineWithProperties),

            // Polygon geometry.
            Arguments.of(polygonToJson(polygon), PolygonGeometry.class, polygon),

            // Polygon geometry with properties.
            Arguments.of(polygonToJson(polygonWithProperties), PolygonGeometry.class, polygonWithProperties),

            // Multi point geometry.
            Arguments.of(multiPointToJson(multiPoint), MultiPointGeometry.class, multiPoint),

            // Multi point geometry with properties.
            Arguments.of(multiPointToJson(multiPointWithProperties), MultiPointGeometry.class,
                multiPointWithProperties),

            // Multi line geometry.
            Arguments.of(multiLineToJson(multiLine), MultiLineGeometry.class, multiLine),

            // Multi line geometry with properties.
            Arguments.of(multiLineToJson(multiLineWithProperties), MultiLineGeometry.class, multiLineWithProperties),

            // Multi polygon geometry.
            Arguments.of(multiPolygonToJson(multiPolygon), MultiPolygonGeometry.class, multiPolygon),

            // Multi polygon geometry with properties.
            Arguments.of(multiPolygonToJson(multiPolygonWithProperties), MultiPolygonGeometry.class,
                multiPolygonWithProperties),

            // Collection geometry.
            Arguments.of(collectionToJson(collection), CollectionGeometry.class, collection),

            // Collection geometry with properties.
            Arguments.of(collectionToJson(collectionWithProperties), CollectionGeometry.class, collectionWithProperties)
        );
    }
}
