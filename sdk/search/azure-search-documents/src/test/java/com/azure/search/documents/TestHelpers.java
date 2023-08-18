// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.azure.search.documents.SearchTestBase.API_KEY;
import static com.azure.search.documents.SearchTestBase.ENDPOINT;
import static com.azure.search.documents.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.documents.SearchTestBase.HOTELS_TESTS_INDEX_DATA_JSON;
import static com.azure.search.documents.SearchTestBase.SERVICE_THROTTLE_SAFE_RETRY_POLICY;
import static com.azure.search.documents.implementation.util.Utility.MAP_STRING_OBJECT_TYPE_REFERENCE;
import static com.azure.search.documents.implementation.util.Utility.getDefaultSerializerAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class contains helper methods for running Azure Cognitive Search tests.
 */
public final class TestHelpers {
    private static final TestMode TEST_MODE = setupTestMode();

    public static final ObjectMapper MAPPER = getDefaultSerializerAdapter().serializer();

    public static final String HOTEL_INDEX_NAME = "hotels";

    public static final String BLOB_DATASOURCE_NAME = "azs-java-live-blob";
    public static final String BLOB_DATASOURCE_TEST_NAME = "azs-java-test-blob";
    public static final String SQL_DATASOURCE_NAME = "azs-java-test-sql";
    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final TypeReference<List<Map<String, Object>>> LIST_TYPE_REFERENCE =
        new TypeReference<List<Map<String, Object>>>() {
        };

    /**
     * Assert whether two objects are equal.
     *
     * @param expected The expected object.
     * @param actual The actual object.
     */
    public static void assertObjectEquals(Object expected, Object actual) {
        try {
            assertEquals(getDefaultSerializerAdapter().serialize(expected, SerializerEncoding.JSON),
                getDefaultSerializerAdapter().serialize(actual, SerializerEncoding.JSON));
        } catch (IOException ex) {
            fail("There is something wrong happen in serializer.");
        }
    }

    /**
     * Assert whether two objects are equal.
     *
     * @param expected The expected object.
     * @param actual The actual object.
     * @param ignoredDefaults Set to true if it needs to ignore default value of expected object.
     * @param ignoredFields Varargs of ignored fields.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "UseOfObsoleteDateTimeApi"})
    public static void assertObjectEquals(Object expected, Object actual, boolean ignoredDefaults,
        String... ignoredFields) {
        if (isComparableType(expected)) {
            assertEquals(expected, actual);
        } else if (expected instanceof OffsetDateTime) {
            assertEquals(0, ((OffsetDateTime) expected).compareTo(OffsetDateTime.parse(actual.toString())));
        } else if (expected instanceof Date) {
            assertDateEquals((Date) expected, (Date) actual);
        } else if (expected instanceof Map) {
            assertMapEquals((Map) expected, (Map) actual, ignoredDefaults, ignoredFields);
        } else {
            ObjectNode expectedNode = MAPPER.valueToTree(expected);
            ObjectNode actualNode = MAPPER.valueToTree(actual);
            assertOnMapIterator(expectedNode.fields(), actualNode, ignoredDefaults, ignoredFields);
        }
    }

    /**
     * Assert whether two maps are equal, map key must be String.
     *
     * @param expectedMap The expected map.
     * @param actualMap The actual map.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void assertMapEquals(Map<String, Object> expectedMap, Map<String, Object> actualMap,
        boolean ignoreDefaults, String... ignoredFields) {
        expectedMap.forEach((key, value) -> {
            if (value != null && actualMap.get(key) != null) {
                if (isComparableType(value)) {
                    assertEquals(value, actualMap.get(key));
                } else if (value instanceof List) {
                    assertListEquals((List) value, (List) actualMap.get(key), ignoreDefaults, ignoredFields);
                } else {
                    assertObjectEquals(value, actualMap.get(key), ignoreDefaults, ignoredFields);
                }
            }
        });
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public static void assertDateEquals(Date expect, Date actual) {
        assertEquals(0, expect.toInstant().atOffset(ZoneOffset.UTC)
            .compareTo(actual.toInstant().atOffset(ZoneOffset.UTC)));
    }

    public static void assertListEquals(List<Object> expected, List<Object> actual, boolean ignoredDefaults,
        String... ignoredFields) {
        for (int i = 0; i < expected.size(); i++) {
            assertObjectEquals(expected.get(i), actual.get(i), ignoredDefaults, ignoredFields);
        }
    }

    private static boolean isComparableType(Object obj) {
        return obj.getClass().isPrimitive() || obj.getClass().isArray() || obj instanceof Integer
            || obj instanceof Long || obj instanceof String || obj instanceof Boolean || obj instanceof Double;
    }

    private static void assertOnMapIterator(Iterator<Map.Entry<String, JsonNode>> expectedNode,
        ObjectNode actualNode, boolean ignoreDefaults, String[] ignoredFields) {
        Set<String> ignoredFieldSet = new HashSet<>(Arrays.asList(ignoredFields));
        while (expectedNode.hasNext()) {
            assertTrue(actualNode.fields().hasNext());
            Map.Entry<String, JsonNode> expectedField = expectedNode.next();
            String fieldName = expectedField.getKey();
            if (shouldSkipField(fieldName, expectedField.getValue(), ignoreDefaults, ignoredFieldSet)) {
                continue;
            }
            if (expectedField.getValue().isValueNode()) {
                assertEquals(expectedField.getValue(), actualNode.get(expectedField.getKey()),
                    String.format("The key %s of the map has different values", expectedField.getKey()));
            } else if (expectedField.getValue().isArray()) {
                Iterator<JsonNode> expectedArray = expectedField.getValue().elements();
                Iterator<JsonNode> actualArray = actualNode.get(expectedField.getKey()).elements();
                while (expectedArray.hasNext()) {
                    assertTrue(actualArray.hasNext());
                    JsonNode a = expectedArray.next();
                    JsonNode b = actualArray.next();
                    if (ignoredFieldSet.contains(fieldName)) {
                        continue;
                    }
                    if (shouldSkipField(null, a, true, null)) {
                        continue;
                    }
                    assertEquals(a.asText(), b.asText());
                }
            } else {
                assertObjectEquals(expectedField.getValue(), actualNode.get(fieldName), ignoreDefaults,
                    ignoredFields);
            }
        }
    }

    private static boolean shouldSkipField(String fieldName, JsonNode fieldValue,
        boolean ignoreDefaults, Set<String> ignoredFields) {
        if (ignoredFields != null && ignoredFields.contains(fieldName)) {
            return true;
        }

        if (ignoreDefaults) {
            if (fieldValue.isNull()) {
                return true;
            }
            if (fieldValue.isBoolean() && !fieldValue.asBoolean()) {
                return true;
            }
            return fieldValue.isNumber() && fieldValue.asDouble() == 0.0D;
        }
        return false;
    }

    public static void assertHttpResponseException(Runnable exceptionThrower, int statusCode, String expectedMessage) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            verifyHttpResponseError(ex, statusCode, expectedMessage);
        }
    }

    public static void assertHttpResponseExceptionAsync(Publisher<?> exceptionThrower) {
        StepVerifier.create(exceptionThrower)
            .verifyErrorSatisfies(error -> verifyHttpResponseError(error, HttpURLConnection.HTTP_BAD_REQUEST,
                "Invalid expression: Could not find a property named 'ThisFieldDoesNotExist' on type 'search.document'."));
    }

    private static void verifyHttpResponseError(Throwable ex, int statusCode, String expectedMessage) {

        assertEquals(HttpResponseException.class, ex.getClass());

        assertEquals(statusCode, ((HttpResponseException) ex).getResponse().getStatusCode());

        if (expectedMessage != null) {
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    public static void waitForIndexing() {
        // Wait 2 seconds to allow index request to finish.
        sleepIfRunningAgainstService(3000);
    }

    public static void sleepIfRunningAgainstService(long millis) {
        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static TestMode setupTestMode() {
        try {
            return TestMode.valueOf(Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE", "PLAYBACK"));
        } catch (RuntimeException ex) {
            return TestMode.PLAYBACK;
        }
    }

    public static <T> void uploadDocuments(SearchClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc);
        waitForIndexing();
    }

    public static <T> void uploadDocuments(SearchAsyncClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc).block();
        waitForIndexing();
    }

    public static <T> void uploadDocument(SearchClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc));
        waitForIndexing();
    }

    public static <T> void uploadDocument(SearchAsyncClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc)).block();
        waitForIndexing();
    }

    public static List<Map<String, Object>> uploadDocumentsJson(SearchClient client, String dataJson) {
        List<Map<String, Object>> documents = readJsonFileToList(dataJson);
        uploadDocuments(client, documents);

        return documents;
    }

    public static HttpPipeline getHttpPipeline(SearchClient searchClient) {
        return searchClient.getHttpPipeline();
    }

    public static HttpPipeline getHttpPipeline(SearchAsyncClient searchAsyncClient) {
        return searchAsyncClient.getHttpPipeline();
    }

    public static List<Map<String, Object>> readJsonFileToList(String filename) {
        InputStream inputStream = Objects.requireNonNull(TestHelpers.class.getClassLoader()
            .getResourceAsStream(filename));

        return deserializeToType(inputStream, LIST_TYPE_REFERENCE);
    }

    public static Map<String, Object> convertStreamToMap(InputStream sourceStream) {
        return deserializeToType(sourceStream, MAP_STRING_OBJECT_TYPE_REFERENCE);
    }

    private static <T> T deserializeToType(InputStream stream, TypeReference<T> type) {
        try {
            return getDefaultSerializerAdapter().deserialize(stream, type.getJavaType(), SerializerEncoding.JSON);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    public static <T> T convertMapToValue(Map<String, Object> value, Class<T> clazz) {
        try {
            return Utility.convertValue(value, clazz);
        } catch (IOException ex) {
            throw Exceptions.propagate(ex);
        }
    }

    @SuppressWarnings("removal")
    public static SearchIndexClient setupSharedIndex(String indexName) {
        InputStream stream = Objects.requireNonNull(AutocompleteSyncTests.class
            .getClassLoader()
            .getResourceAsStream(HOTELS_TESTS_INDEX_DATA_JSON));

        try {
            SearchIndex index = MAPPER.readValue(stream, SearchIndex.class);

            Field searchIndexName = index.getClass().getDeclaredField("name");
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                searchIndexName.setAccessible(true);
                return null;
            });

            searchIndexName.set(index, indexName);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
                .endpoint(ENDPOINT)
                .credential(new AzureKeyCredential(API_KEY))
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            searchIndexClient.createOrUpdateIndex(index);
            uploadDocumentsJson(searchIndexClient.getSearchClient(indexName), HOTELS_DATA_JSON);

            return searchIndexClient;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String createGeographyPolygon(String... coordinates) {
        if (coordinates.length % 2 != 0) {
            throw new RuntimeException("'coordinates' must contain pairs of two.");
        }

        StringBuilder builder = new StringBuilder("geography'POLYGON((");

        for (int i = 0; i < coordinates.length; i += 2) {
            if (i != 0) {
                builder.append(',');
            }

            builder.append(coordinates[i])
                .append(' ')
                .append(coordinates[i + 1]);
        }

        return builder.append("))'").toString();
    }
}
