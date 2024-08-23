// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.identity.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.test.environment.models.NonNullableModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.azure.search.documents.SearchTestBase.ENDPOINT;
import static com.azure.search.documents.SearchTestBase.SERVICE_THROTTLE_SAFE_RETRY_POLICY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class contains helper methods for running Azure AI Search tests.
 */
public final class TestHelpers {
    private static TestMode testMode = setupTestMode();

    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    public static final String HOTEL_INDEX_NAME = "hotels";

    public static final String BLOB_DATASOURCE_NAME = "azs-java-live-blob";
    public static final String BLOB_DATASOURCE_TEST_NAME = "azs-java-test-blob";
    public static final String SQL_DATASOURCE_NAME = "azs-java-test-sql";
    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final Map<String, byte[]> LOADED_FILE_DATA = new ConcurrentHashMap<>();

    /**
     * Assert whether two objects are equal.
     *
     * @param expected The expected object.
     * @param actual The actual object.
     */
    public static void assertObjectEquals(Object expected, Object actual) {
        assertArrayEquals(SERIALIZER.serializeToBytes(expected), SERIALIZER.serializeToBytes(actual));
    }

    /**
     * Assert whether two objects are equal.
     *
     * @param expected The expected object.
     * @param actual The actual object.
     * @param ignoreDefaults Set to true if it needs to ignore default value of expected object.
     * @param ignoredFields Varargs of ignored fields.
     */
    public static void assertObjectEquals(Object expected, Object actual, boolean ignoreDefaults,
        String... ignoredFields) {
        Set<String> ignored = (ignoredFields == null)
            ? Collections.emptySet()
            : new HashSet<>(Arrays.asList(ignoredFields));

        assertObjectEqualsInternal(expected, actual, ignoreDefaults, ignored);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "UseOfObsoleteDateTimeApi"})
    private static void assertObjectEqualsInternal(Object expected, Object actual, boolean ignoredDefaults,
        Set<String> ignoredFields) {
        if (expected == null) {
            assertNull(actual);
        } else if (isComparableType(expected.getClass())) {
            if (expected instanceof Number) {
                assertEquals(((Number) expected).doubleValue(), ((Number) actual).doubleValue());
            } else {
                assertEquals(expected, actual);
            }
        } else if (expected instanceof OffsetDateTime) {
            assertEquals(0, ((OffsetDateTime) expected).compareTo(OffsetDateTime.parse(actual.toString())));
        } else if (expected instanceof Date) {
            assertDateEquals((Date) expected, (Date) actual);
        } else if (expected instanceof Map) {
            assertMapEqualsInternal((Map) expected, (Map) actual, ignoredDefaults, ignoredFields);
        } else {
            byte[] expectedJson;
            byte[] actualJson;
            if (expected instanceof JsonSerializable<?>) {
                expectedJson = serializeJsonSerializable((JsonSerializable<?>) expected);
                actualJson = (actual instanceof JsonSerializable<?>)
                    ? serializeJsonSerializable((JsonSerializable<?>) actual)
                    : SERIALIZER.serializeToBytes(actual);
            } else {
                expectedJson = SERIALIZER.serializeToBytes(expected);
                actualJson = SERIALIZER.serializeToBytes(actual);
            }

            try (JsonReader expectedReader = JsonProviders.createReader(expectedJson);
                 JsonReader actualReader = JsonProviders.createReader(actualJson)) {

                assertMapEqualsInternal(expectedReader.readMap(JsonReader::readUntyped),
                    actualReader.readMap(JsonReader::readUntyped), ignoredDefaults, ignoredFields);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    private static byte[] serializeJsonSerializable(JsonSerializable<?> jsonSerializable) {
        if (jsonSerializable == null) {
            return new byte[0];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            jsonSerializable.toJson(writer).flush();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Determines if two lists of documents are equal by comparing their keys.
     *
     * @param group1 The first list of documents.
     * @param group2 The second list documents.
     * @return True of false if the documents are equal or not equal, respectively.
     */
    public static boolean equalDocumentSets(List<NonNullableModel> group1, List<NonNullableModel> group2) {
        List<String> group1Keys = produceKeyList(group1, TestHelpers::extractKeyFromDocument);
        List<String> group2Keys = produceKeyList(group2, TestHelpers::extractKeyFromDocument);
        return group1Keys.containsAll(group2Keys);
    }

    private static <T> List<String> produceKeyList(List<T> objList, Function<T, String> extractKeyFunc) {
        List<String> keyList = new ArrayList<>();
        for (T obj : objList) {
            keyList.add(extractKeyFunc.apply(obj));
        }
        return keyList;
    }

    private static String extractKeyFromDocument(NonNullableModel document) {
        return document.key();
    }

    /**
     * Assert whether two maps are equal, map key must be String.
     *
     * @param expectedMap The expected map.
     * @param actualMap The actual map.
     */
    public static void assertMapEquals(Map<String, Object> expectedMap, Map<String, Object> actualMap,
        boolean ignoreDefaults, String... ignoredFields) {
        Set<String> ignored = (ignoredFields == null)
            ? Collections.emptySet()
            : new HashSet<>(Arrays.asList(ignoredFields));

        assertMapEqualsInternal(expectedMap, actualMap, ignoreDefaults, ignored);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void assertMapEqualsInternal(Map<String, Object> expectedMap, Map<String, Object> actualMap,
        boolean ignoreDefaults, Set<String> ignoredFields) {
        for (Map.Entry<String, Object> entry : expectedMap.entrySet()) {
            String expectedKey = entry.getKey();
            Object expectedValue = entry.getValue();

            if (shouldSkipField(expectedKey, expectedValue, ignoreDefaults, ignoredFields)) {
                continue;
            }

            assertTrue(actualMap.containsKey(expectedKey));
            Object actualValue = actualMap.get(expectedKey);
            if (expectedValue == null) {
                assertNull(actualValue);
            } else {
                if (isComparableType(expectedValue.getClass())) {
                    if (expectedValue instanceof Number) {
                        assertEquals(((Number) entry.getValue()).doubleValue(), ((Number) actualValue).doubleValue());
                    } else {
                        assertEquals(entry.getValue(), actualValue);
                    }
                } else if (expectedValue instanceof List) {
                    assertListEquals((List) expectedValue, (List) actualValue, ignoreDefaults, ignoredFields);
                } else {
                    assertObjectEqualsInternal(expectedValue, actualValue, ignoreDefaults, ignoredFields);
                }
            }
        }
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private static void assertDateEquals(Date expect, Date actual) {
        assertEquals(0, expect.toInstant().atOffset(ZoneOffset.UTC)
            .compareTo(actual.toInstant().atOffset(ZoneOffset.UTC)));
    }

    private static void assertListEquals(List<Object> expected, List<Object> actual, boolean ignoreDefaults,
        Set<String> ignoredFields) {
        for (int i = 0; i < expected.size(); i++) {
            assertObjectEqualsInternal(expected.get(i), actual.get(i), ignoreDefaults, ignoredFields);
        }
    }

    private static boolean isComparableType(Class<?> clazz) {
        return clazz.isPrimitive() // Primitive types are always comparable.
            || clazz.isEnum() // Enums are comparable
            || ExpandableStringEnum.class.isAssignableFrom(clazz) // And so are ExpandableStringEnums
            || Byte.class.isAssignableFrom(clazz) // Boxed primitives are also comparable
            || Boolean.class.isAssignableFrom(clazz)
            || Character.class.isAssignableFrom(clazz)
            || Short.class.isAssignableFrom(clazz)
            || Integer.class.isAssignableFrom(clazz)
            || Long.class.isAssignableFrom(clazz)
            || Float.class.isAssignableFrom(clazz)
            || Double.class.isAssignableFrom(clazz)
            || String.class.isAssignableFrom(clazz) // And so are Strings
            || (clazz.isArray() && isComparableType(Array.newInstance(clazz, 0).getClass())); // Array of comparable
    }

    private static boolean shouldSkipField(String fieldName, Object value, boolean ignoreDefaults,
        Set<String> ignoredFields) {
        if (ignoredFields != null && ignoredFields.contains(fieldName)) {
            return true;
        }

        if (ignoreDefaults) {
            if (value == null) {
                return true;
            }
            if (value instanceof Boolean && !((boolean) value)) {
                return true;
            }

            return value instanceof Number && ((Number) value).doubleValue() == 0.0D;
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

    public static void verifyHttpResponseError(Throwable ex, int statusCode, String expectedMessage) {
        if (ex instanceof HttpResponseException) {
            assertEquals(statusCode, ((HttpResponseException) ex).getResponse().getStatusCode());

            if (expectedMessage != null) {
                assertTrue(ex.getMessage().contains(expectedMessage));
            }
        } else {
            fail("Expected exception to be instanceof HttpResponseException", ex);
        }
    }

    public static void waitForIndexing() {
        // Wait 2 seconds to allow index request to finish.
        sleepIfRunningAgainstService(2000);
    }

    public static void sleepIfRunningAgainstService(long millis) {
        testMode = setupTestMode();
        if (testMode == TestMode.PLAYBACK) {
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

    public static List<Map<String, Object>> uploadDocumentsJson(SearchAsyncClient client, String dataJson) {
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
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(filename))) {
            return jsonReader.readArray(reader -> reader.readMap(JsonReader::readUntyped));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Map<String, Object> convertStreamToMap(byte[] source) {
        try (JsonReader jsonReader = JsonProviders.createReader(source)) {
            return jsonReader.readMap(JsonReader::readUntyped);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T convertMapToValue(Map<String, Object> value, Class<T> clazz) {
        return SERIALIZER.deserializeFromBytes(SERIALIZER.serializeToBytes(value), TypeReference.createInstance(clazz));
    }

    public static SearchIndexClient setupSharedIndex(String indexName, String indexDefinition, String indexData) {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(indexDefinition))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
                .endpoint(ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            searchIndexClient.createOrUpdateIndex(createTestIndex(indexName, baseIndex));

            if (indexData != null) {
                uploadDocumentsJson(searchIndexClient.getSearchClient(indexName), indexData);
            }

            return searchIndexClient;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Retrieve the appropriate TokenCredential based on the test mode.
     *
     * @return The appropriate token credential
     */
    public static TokenCredential getTestTokenCredential() {
        if (testMode == TestMode.PLAYBACK) {
            return new MockTokenCredential();
        } else if (testMode == TestMode.RECORD) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new AzurePowerShellCredentialBuilder().build();
        }
    }

    static SearchIndex createTestIndex(String testIndexName, SearchIndex baseIndex) {
        return new SearchIndex(testIndexName)
            .setFields(baseIndex.getFields())
            .setScoringProfiles(baseIndex.getScoringProfiles())
            .setDefaultScoringProfile(baseIndex.getDefaultScoringProfile())
            .setCorsOptions(baseIndex.getCorsOptions())
            .setSuggesters(baseIndex.getSuggesters())
            .setAnalyzers(baseIndex.getAnalyzers())
            .setTokenizers(baseIndex.getTokenizers())
            .setTokenFilters(baseIndex.getTokenFilters())
            .setCharFilters(baseIndex.getCharFilters())
            .setEncryptionKey(baseIndex.getEncryptionKey())
            .setSimilarity(baseIndex.getSimilarity())
            .setSemanticSearch(baseIndex.getSemanticSearch())
            .setETag(baseIndex.getETag());
    }

    public static HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((httpRequest, context) -> false)
            .assertSync()
            .build();
    }

    public static SearchIndexClient createSharedSearchIndexClient() {
        return new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(getTestTokenCredential())
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
            .httpClient(buildSyncAssertingClient(HttpClient.createDefault()))
            .buildClient();
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

    static byte[] loadResource(String fileName) {
        return LOADED_FILE_DATA.computeIfAbsent(fileName, fName -> {
            try {
                URI fileUri = AutocompleteTests.class.getClassLoader()
                    .getResource(fileName)
                    .toURI();

                return Files.readAllBytes(Paths.get(fileUri));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
