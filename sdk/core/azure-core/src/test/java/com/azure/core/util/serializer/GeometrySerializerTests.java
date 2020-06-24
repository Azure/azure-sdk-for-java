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
 * Tests {@link GeometrySerializer}.
 */
public class GeometrySerializerTests {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper().registerModule(GeometrySerializer.MODULE);
    }

    @Test
    public void unknownGeometryTypeThrows() {
        assertThrows(IOException.class, () -> MAPPER.writeValueAsString(new CustomGeometry(null, null)));
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
        assertEquals(expectedJson, actualJson);
    }

    private static Stream<Arguments> serializeSupplier() {
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

        MultiPointGeometry multiPoint = new MultiPointGeometry(Arrays.asList(point, pointWithProperties));

        MultiPointGeometry multiPointWithProperties = new MultiPointGeometry(Arrays.asList(point, pointWithProperties),
            boundingBox, properties);

        MultiLineGeometry multiLine = new MultiLineGeometry(Arrays.asList(line, lineWithProperties));

        MultiLineGeometry multiLineWithProperties = new MultiLineGeometry(Arrays.asList(line, lineWithProperties),
            boundingBox, properties);

        MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(Arrays.asList(polygon, polygonWithProperties));

        MultiPolygonGeometry multiPolygonWithProperties  = new MultiPolygonGeometry(Arrays.asList(polygon,
            polygonWithProperties), boundingBox, properties);

        CollectionGeometry collection = new CollectionGeometry(Arrays.asList(point, multiPointWithProperties));

        CollectionGeometry collectionWithProperties = new CollectionGeometry(Arrays.asList(point,
            multiPointWithProperties), boundingBox, properties);

        return Stream.of(
            // Point geometry.
            Arguments.of(point, pointToJson(point)),

            // Point geometry with properties.
            Arguments.of(pointWithProperties, pointToJson(pointWithProperties)),

            // Line geometry.
            Arguments.of(line, lineToJson(line)),

            // Line geometry with properties.
            Arguments.of(lineWithProperties, lineToJson(lineWithProperties)),

            // Polygon geometry.
            Arguments.of(polygon, polygonToJson(polygon)),

            // Polygon geometry with properties.
            Arguments.of(polygonWithProperties, polygonToJson(polygonWithProperties)),

            // Multi point geometry.
            Arguments.of(multiPoint, multiPointToJson(multiPoint)),

            // Multi point geometry with properties.
            Arguments.of(multiPointWithProperties, multiPointToJson(multiPointWithProperties)),

            // Multi line geometry.
            Arguments.of(multiLine, multiLineToJson(multiLine)),

            // Multi line geometry with properties.
            Arguments.of(multiLineWithProperties, multiLineToJson(multiLineWithProperties)),

            // Multi polygon geometry.
            Arguments.of(multiPolygon, multiPolygonToJson(multiPolygon)),

            // Multi polygon geometry with properties.
            Arguments.of(multiPolygonWithProperties, multiPolygonToJson(multiPolygonWithProperties)),

            // Collection geometry.
            Arguments.of(collection, collectionToJson(collection)),

            // Collection geometry with properties.
            Arguments.of(collectionWithProperties, collectionToJson(collectionWithProperties))
        );
    }
}
