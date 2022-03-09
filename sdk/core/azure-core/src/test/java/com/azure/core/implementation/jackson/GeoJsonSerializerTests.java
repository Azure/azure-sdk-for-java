// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLineStringCollection;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoObjectType;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Tests {@link GeoJsonSerializer}.
 */
public class GeoJsonSerializerTests {
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();

    @Test
    public void unknownGeoTypeThrows() {
        Assertions.assertThrows(IOException.class,
            () -> ADAPTER.serialize(new CustomGeoObject(null, null), SerializerEncoding.JSON));
    }

    private static final class CustomGeoObject extends GeoObject {
        protected CustomGeoObject(GeoBoundingBox boundingBox, Map<String, Object> properties) {
            super(boundingBox, properties);
        }

        @Override
        public GeoObjectType getType() {
            return null;
        }
    }

    @ParameterizedTest
    @MethodSource("serializeSupplier")
    public <T extends GeoObject> void serialize(T geo, String expectedJson) throws IOException {
        String actualJson = ADAPTER.serialize(geo, SerializerEncoding.JSON);
        Assertions.assertEquals(expectedJson, actualJson);
    }

    private static Stream<Arguments> serializeSupplier() {
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
                pointSupplier.apply(box, properties)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoLineStringCollection> multiLineSupplier =
            (box, properties) -> new GeoLineStringCollection(Arrays.asList(lineSupplier.apply(null, null),
                lineSupplier.apply(box, properties)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoPolygonCollection> multiPolygonSuppluer =
            (box, properties) -> new GeoPolygonCollection(Arrays.asList(polygonSupplier.apply(null, null),
                polygonSupplier.apply(box, properties)), box, properties);

        BiFunction<GeoBoundingBox, Map<String, Object>, GeoCollection> collectionSupplier =
            (box, properties) -> new GeoCollection(Arrays.asList(pointSupplier.apply(null, null),
                multiPointSupplier.apply(box, properties)), box, properties);

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
            Arguments.of(serializerArgumentSupplier(boundingBox, objectProperties, collectionSupplier))
        );
    }

    private static Object[] serializerArgumentSupplier(GeoBoundingBox boundingBox, Map<String, Object> properties,
        BiFunction<GeoBoundingBox, Map<String, Object>, ? extends GeoObject> geoSupplier) {
        GeoObject geoObject = geoSupplier.apply(boundingBox, properties);
        return new Object[]{geoObject, GeoSerializationTestHelpers.geoToJson(geoObject)};
    }
}
