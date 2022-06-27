// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.DefaultJsonReader;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.search.documents.SearchTestBase.API_KEY;
import static com.azure.search.documents.SearchTestBase.ENDPOINT;
import static com.azure.search.documents.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.documents.SearchTestBase.HOTELS_TESTS_INDEX_DATA_JSON;
import static com.azure.search.documents.SearchTestBase.SERVICE_THROTTLE_SAFE_RETRY_POLICY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class contains helper methods for running Azure Cognitive Search tests.
 */
public final class TestHelpers {
    private static final TestMode TEST_MODE = setupTestMode();

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
     * @param ignoredFields Ignored fields.
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
                actualJson = serializeJsonSerializable((JsonSerializable<?>) actual);
            } else {
                expectedJson = SERIALIZER.serializeToBytes(expected);
                actualJson = SERIALIZER.serializeToBytes(actual);
            }

            Map<String, Object> expectedMap = (Map<String, Object>) JsonUtils.readUntypedField(
                DefaultJsonReader.fromBytes(expectedJson));
            Map<String, Object> actualMap = (Map<String, Object>) JsonUtils.readUntypedField(
                DefaultJsonReader.fromBytes(actualJson));

            assertMapEqualsInternal(expectedMap, actualMap, ignoredDefaults, ignoredFields);
        }
    }

    private static byte[] serializeJsonSerializable(JsonSerializable<?> jsonSerializable) {
        if (jsonSerializable == null) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        jsonSerializable.toJson(DefaultJsonWriter.fromStream(outputStream));

        return outputStream.toByteArray();
    }

    /**
     * Assert whether two maps are equal, map key must be String.
     *
     * @param expectedMap The expected map.
     * @param actualMap The actual map.
     * @param ignoreDefaults Set to true if it needs to ignore default value of expected object.
     * @param ignoredFields Ignored fields.
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

    private static void assertListEquals(List<Object> expected, List<Object> actual, boolean ignoredDefaults,
        Set<String> ignoredFields) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertObjectEqualsInternal(expected.get(i), actual.get(i), ignoredDefaults, ignoredFields);
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
        // Wait 3 seconds to allow index request to finish.
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

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readJsonFileToList(String filename) {
        JsonReader reader = DefaultJsonReader.fromBytes(loadResource(filename));

        return JsonUtils.readArray(reader, reader1 -> (Map<String, Object>) JsonUtils.readUntypedField(reader1));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertStreamToMap(byte[] source) {
        return (Map<String, Object>) JsonUtils.readUntypedField(DefaultJsonReader.fromBytes(source));
    }

//    private static <T> T deserializeToType(InputStream stream, TypeReference<T> type) {
//        try {
//            return getDefaultSerializerAdapter().deserialize(stream, type.getJavaType(), SerializerEncoding.JSON);
//        } catch (IOException e) {
//            throw Exceptions.propagate(e);
//        }
//    }

    public static <T> T convertMapToValue(Map<String, Object> value, Class<T> clazz) {
        return SERIALIZER.deserializeFromBytes(SERIALIZER.serializeToBytes(value), TypeReference.createInstance(clazz));
    }

    public static SearchIndexClient setupSharedIndex(String indexName) {
        SearchIndex baseIndex = SearchIndex.fromJson(
            DefaultJsonReader.fromBytes(loadResource(HOTELS_TESTS_INDEX_DATA_JSON)));

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
            .buildClient();

        searchIndexClient.createOrUpdateIndex(createTestIndex(indexName, baseIndex));
        uploadDocumentsJson(searchIndexClient.getSearchClient(indexName), HOTELS_DATA_JSON);

        return searchIndexClient;
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
            .setNormalizers(baseIndex.getNormalizers())
            .setEncryptionKey(baseIndex.getEncryptionKey())
            .setSimilarity(baseIndex.getSimilarity())
            .setSemanticSettings(baseIndex.getSemanticSettings())
            .setETag(baseIndex.getETag());
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
                URI fileUri = AutocompleteSyncTests.class.getClassLoader()
                    .getResource(fileName)
                    .toURI();

                return Files.readAllBytes(Paths.get(fileUri));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
