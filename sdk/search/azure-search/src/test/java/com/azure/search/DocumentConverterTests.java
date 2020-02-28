// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.implementation.SerializationUtil;
import com.azure.search.models.GeoPoint;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Functional tests that ensure expected behavior of deserializing a document.
 */
public class DocumentConverterTests {

    private final String testDateString = "2016-10-10T17:41:05.123-07:00";
    private final OffsetDateTime testDate = OffsetDateTime.of(2016, 10, 10, 17, 41, 5, 123 * 1_000_000, ZoneOffset.of("-07:00"));

    private Document deserialize(String json) {
        // Deserialization of the search result is done with azure-core (using Jackson as well)
        // the result object is a map of key:value, get deserialized directly into the Document object
        // Document is simply a Hash Map.
        // in this case we simulate creation of the object created by azure-core

        JacksonAdapter adapter = new JacksonAdapter();
        SerializationUtil.configureMapper(adapter.serializer());

        Document doc = new Document();
        try {
            doc = adapter.deserialize(json, Document.class, SerializerEncoding.JSON);
            cleanupODataAnnotation(doc);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private void cleanupODataAnnotation(Document document) {
        // Skip OData @search annotations. These are deserialized separately.
        List<String> keys = document.keySet().stream().filter(key -> key.startsWith("@search")).collect(Collectors.toList());
        keys.forEach(document::remove);
    }

    @Test
    public void annotationsAreExcludedFromDocument() {
        String json = "{ \"@search.score\": 3.14, \"field1\": \"value1\", \"field2\": 123, \"@search.someOtherAnnotation\": { \"a\": \"b\" }, \"field3\": 2.78 }";
        Document expectedDoc = new Document();
        expectedDoc.put("field1", "value1");
        expectedDoc.put("field2", 123);
        expectedDoc.put("field3", 2.78);

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadNullValues() {
        String json = "{\"field1\": null,\"field2\": [ \"hello\", null ], \"field3\": [ null, 123, null ], \"field4\": [ null, { \"name\": \"Bob\" } ]}";
        Document expectedDoc = new Document();
        expectedDoc.put("field1", null);
        expectedDoc.put("field2", Arrays.asList("hello", null));
        expectedDoc.put("field3", Arrays.asList(null, 123, null));
        expectedDoc.put("field4", Arrays.asList(null, new Document(Collections.singletonMap("name", "Bob"))));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadPrimitiveTypes() {
        Map<String, Object> values = new HashMap<>();
        values.put("123", 123);
        values.put("9999999999999", 9_999_999_999_999L);
        values.put("3.14", 3.14);
        values.put("\"hello\"", "hello");
        values.put("true", true);
        values.put("false", false);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String jsonValue = entry.getKey();
            Object expectedObject = entry.getValue();
            String json = "{\"field\" :".concat(jsonValue).concat("}");
            Document expectedDoc = new Document(Collections.singletonMap("field", expectedObject));

            Document actualDoc = deserialize(json);
            assertEquals(expectedDoc, actualDoc);
        }
    }

    @Test
    public void canReadArraysOfPrimitiveTypes() {
        Map<String, Object> values = new HashMap<>();
        values.put("[\"hello\", \"goodbye\"]", Arrays.asList("hello", "goodbye"));
        values.put("[123, 456]", Arrays.asList(123, 456));
        values.put("[9999999999999, -12]", Arrays.asList(9_999_999_999_999L, -12));
        values.put("[3.14, 2.78]", Arrays.asList(3.14, 2.78));
        values.put("[true, false]", Arrays.asList(true, false));

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String jsonArray = entry.getKey();
            Object expectedArray = entry.getValue();
            String json = "{\"field\" :".concat(jsonArray).concat("}");
            Document expectedDoc = new Document(Collections.singletonMap("field", expectedArray));

            Document actualDoc = deserialize(json);
            assertEquals(expectedDoc, actualDoc);
        }
    }


    @Test
    public void canReadGeoPoint() {
        String json = "{ \"field\": { \"type\": \"Point\", \"coordinates\": [-122.131577, 47.678581] } }";
        Document expectedDoc = new Document(Collections.singletonMap("field", GeoPoint.create(47.678581, -122.131577)));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadGeoPointCollection() {
        String json = "{ \"field\": [{ \"type\": \"Point\", \"coordinates\": [-122.131577, 47.678581] }, { \"type\": \"Point\", \"coordinates\": [-121, 49] }]}";
        Document expectedDoc = new Document(Collections.singletonMap("field",
            Arrays.asList(GeoPoint.create(47.678581, -122.131577), GeoPoint.create(49, -121))));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadComplexObject() {
        String json = "{\"name\" : \"Boots\", \"details\": {\"sku\" : 123, \"seasons\" : [\"fall\", \"winter\"]}}";
        Document innerDoc = new Document();
        innerDoc.put("sku", 123);
        innerDoc.put("seasons", Arrays.asList("fall", "winter"));

        Document expectedDoc = new Document();
        expectedDoc.put("name", "Boots");
        expectedDoc.put("details", innerDoc);

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadComplexCollection() {
        String json = "{\"stores\" : [{\"name\" : \"North\", \"address\" : {\"city\" : \"Vancouver\", \"country\": \"Canada\"}, \"location\": {\"type\" : \"Point\", \"coordinates\": [-121, 49]}},{\"name\" : \"South\", \"address\" : {\"city\": \"Seattle\", \"country\" : \"USA\"}, \"location\" : {\"type\" : \"Point\", \"coordinates\": [-122.5, 47.6]}}]}";

        Document storeAddress1 = new Document();
        storeAddress1.put("city", "Vancouver");
        storeAddress1.put("country", "Canada");

        Document storeLocation1 = new Document();
        storeLocation1.put("type", "Point");
        storeLocation1.put("coordinates", Arrays.asList(-121, 49));

        Document store1 = new Document();
        store1.put("name", "North");
        store1.put("address", storeAddress1);
        store1.put("location", storeLocation1);

        Document storeAddress2 = new Document();
        storeAddress2.put("city", "Seattle");
        storeAddress2.put("country", "USA");

        Document storeLocation2 = new Document();
        storeLocation2.put("type", "Point");
        storeLocation2.put("coordinates", Arrays.asList(-122.5, 47.6));

        Document store2 = new Document();
        store2.put("name", "South");
        store2.put("address", storeAddress2);
        store2.put("location", storeLocation2);

        Document expectedDoc = new Document(Collections.singletonMap("stores", Arrays.asList(store1, store2)));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void canReadArraysOfMixedTypes() {
        // Azure Cognitive Search won't return payloads like this; This test is only for pinning purposes.
        String json = "{\"field\": [\"hello\", 123, 3.14, { \"type\": \"Point\", \"coordinates\": [-122.131577, 47.678581] }, { \"name\": \"Arthur\", \"quest\": null }] }";

        GeoPoint point = GeoPoint.create(47.678581, -122.131577);
        Document innerDoc = new Document();
        innerDoc.put("name", "Arthur");
        innerDoc.put("quest", null);
        List<Object> value = Arrays.asList("hello", 123, 3.14, point, innerDoc);

        Document expectedDoc = new Document();
        expectedDoc.put("field", value);

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void dateTimeStringsAreReadAsDateTime() {
        String json = "{\"field1\":\"".concat(testDateString).concat("\",\"field2\" : [\"").concat(testDateString).concat("\", \"").concat(testDateString).concat("\"]}");
        Document expectedDoc = new Document();
        expectedDoc.put("field1", testDate);
        expectedDoc.put("field2", Arrays.asList(testDate, testDate));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void emptyArraysReadAsObjectArrays() {
        String json = "{ \"field\": [] }";

        // With no elements, we can't tell what type of collection it is, so we default to object.
        Document expectedDoc = new Document();
        expectedDoc.put("field", new ArrayList<>());

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void arraysWithOnlyNullsReadAsStringArrays() {
        String json = "{ \"field\": [null, null] }";

        // With only null elements, we can't tell what type of collection it is. For backward compatibility, we assume type string.
        // This shouldn't happen in practice anyway since Azure Cognitive Search generally doesn't allow nulls in collections.
        Document expectedDoc = new Document();
        List<String> emptyStringList = Arrays.asList(null, null);
        expectedDoc.put("field", emptyStringList);

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void specialDoublesAreReadAsStrings() {
        String json = "{\"field1\" : \"NaN\", \"field2\": \"INF\", \"field3\": \"-INF\", \"field4\": [\"NaN\", \"INF\", \"-INF\"], \"field5\": {\"value\":\"-INF\"}}";
        Document expectedDoc = new Document();
        expectedDoc.put("field1", "NaN");
        expectedDoc.put("field2", "INF");
        expectedDoc.put("field3", "-INF");
        expectedDoc.put("field4", Arrays.asList("NaN", "INF", "-INF"));
        expectedDoc.put("field5", new Document(Collections.singletonMap("value", "-INF")));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void dateTimeStringsInArraysAreReadAsDateTime() {
        String json = "{ \"field\": [ \"hello\", \"".concat(testDateString).concat("\", \"123\" ] }}");
        Document expectedDoc = new Document(Collections.singletonMap("field", Arrays.asList("hello", testDate, "123")));

        Document actualDoc = deserialize(json);
        assertEquals(expectedDoc, actualDoc);
    }
}
