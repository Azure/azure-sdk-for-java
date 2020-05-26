// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.search.documents.models.GeoPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GeoPointDeserializerTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    public static void setupClass() {
        SimpleModule module = new SimpleModule();
        UntypedObjectDeserializer defaultDeserializer = new UntypedObjectDeserializer(null, null);
        module.addDeserializer(Object.class, new GeoPointDeserializer(defaultDeserializer));
        MAPPER.registerModule(module);
    }

    @Test
    public void deserializesSimpleGeoJSON() throws Exception {
        String input = "{"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122.131577,47.678581]"
            + "}";

        GeoPoint expected = GeoPoint.create(47.678581, -122.131577);
        Object actual = MAPPER.readValue(input, Object.class);

        assertEquals(expected, actual);
    }

    @Test
    public void deserializesGeoJSONwithCrs() throws Exception {
        String input = "{"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122.131577,47.678581],"
            + "\"crs\":{"
            + "\"type\":\"name\","
            + "\"properties\":{"
            + "\"name\":\"EPSG:4326\""
            + "}"
            + "}}";

        GeoPoint expected = GeoPoint.create(47.678581, -122.131577);
        Object actual = MAPPER.readValue(input, Object.class);

        assertEquals(expected, actual);
    }

    @Test
    public void returnsDynamicOnInvalidGeoJSON() throws Exception {
        String input = "{"
            + "\"type\":\"INVALID_POINT\","
            + "\"coordinates\":[-122.131577,47.678581]"
            + "}";

        GeoPoint unexpected = GeoPoint.create(47.678581, -122.131577);
        Object actual = MAPPER.readValue(input, Object.class);

        assertNotEquals(unexpected, actual);
        assertEquals(LinkedHashMap.class, actual.getClass());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnsDynamicOnInvalidCrs() throws Exception {
        String input = "{"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122.131577,47.678581],"
            + "\"crs\":{"
            + "\"type\":\"INVALID_CRS\","
            + "\"properties\":{"
            + "\"name\":\"EPSG:4326\""
            + "}"
            + "}}";

        GeoPoint unexpected = GeoPoint.create(47.678581, -122.131577);
        Object actual = MAPPER.readValue(input, Object.class);

        assertNotEquals(unexpected, actual);
        assertEquals(LinkedHashMap.class, actual.getClass());

        Map<String, Object> actualMap = (Map<String, Object>) actual;
        Map<String, Object> actualCrs = (Map<String, Object>) actualMap.get("crs");
        Map<String, Object> actualProperties = (Map<String, Object>) actualCrs.get("properties");
        assertEquals("EPSG:4326", actualProperties.get("name"));
    }

    @Test
    public void deserializesPropertyAsGeoJSON() throws Exception {
        String input = "{"
            + "\"location\": {"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122.131577,47.678581]"
            + "}}";

        GeoPoint expected = GeoPoint.create(47.678581, -122.131577);
        Map<?, ?> obj = MAPPER.readValue(input, Map.class);
        Object actual = obj.get("location");

        assertEquals(expected, actual);
    }

    @Test
    public void deserializesListOfGeoJSON() throws Exception {
        String input = "["
            + "{"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122.131577,47.678581]"
            + "},"
            + "{"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122,47]"
            + "}"
            + "]";

        GeoPoint expected1 = GeoPoint.create(47.678581, -122.131577);
        GeoPoint expected2 = GeoPoint.create(47, -122);

        List<?> list = MAPPER.readValue(input, List.class);

        Object actual1 = list.get(0);
        Object actual2 = list.get(1);

        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    @Test
    public void deserializesListOfPartialGeoJSON() throws Exception {
        String input = "["
            + "{"
            + "\"type\":\"Point\","
            + "\"coordinates\":[-122.131577,47.678581]"
            + "},"
            + "{"
            + "\"type\":\"INVALID_POINT\","
            + "\"coordinates\":[-122,47]"
            + "}"
            + "]";

        GeoPoint expected1 = GeoPoint.create(47.678581, -122.131577);

        List<?> list = MAPPER.readValue(input, List.class);

        Object actual1 = list.get(0);
        Object actual2 = list.get(1);

        assertEquals(expected1, actual1);
        assertEquals(LinkedHashMap.class, actual2.getClass());
    }
}
