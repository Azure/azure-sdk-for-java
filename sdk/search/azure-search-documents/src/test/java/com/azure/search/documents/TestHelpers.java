// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.json.ReadValueCallback;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
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
import java.util.stream.Collectors;

import static com.azure.search.documents.SearchTestBase.SEARCH_ENDPOINT;
import static com.azure.search.documents.SearchTestBase.SERVICE_THROTTLE_SAFE_RETRY_POLICY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Set<String> ignored
            = (ignoredFields == null) ? Collections.emptySet() : new HashSet<>(Arrays.asList(ignoredFields));

        assertObjectEqualsInternal(expected, actual, ignoreDefaults, ignored);
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "UseOfObsoleteDateTimeApi" })
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
     * Assert whether two maps are equal, map key must be String.
     *
     * @param expectedMap The expected map.
     * @param actualMap The actual map.
     */
    public static void assertMapEquals(Map<String, Object> expectedMap, Map<String, Object> actualMap,
        boolean ignoreDefaults, String... ignoredFields) {
        Set<String> ignored
            = (ignoredFields == null) ? Collections.emptySet() : new HashSet<>(Arrays.asList(ignoredFields));

        assertMapEqualsInternal(expectedMap, actualMap, ignoreDefaults, ignored);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        assertEquals(0,
            expect.toInstant().atOffset(ZoneOffset.UTC).compareTo(actual.toInstant().atOffset(ZoneOffset.UTC)));
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

    public static void assertHttpResponseException(Runnable exceptionThrower, int statusCode) {
        assertHttpResponseException(exceptionThrower, statusCode, null);
    }

    public static void assertHttpResponseException(Runnable exceptionThrower, int statusCode, String expectedMessage) {
        Throwable ex = assertThrows(Throwable.class, exceptionThrower::run);
        verifyHttpResponseError(ex, statusCode, expectedMessage);
    }

    public static void verifyHttpResponseError(Throwable throwable, int statusCode, String expectedMessage) {
        HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable,
            "Expected exception to be instanceof HttpResponseException");
        assertEquals(statusCode, ex.getResponse().getStatusCode());

        if (expectedMessage != null) {
            assertTrue(throwable.getMessage().contains(expectedMessage));
        }
    }

    public static void waitForIndexing() {
        // Wait 5 seconds to allow index request to finish.
        sleepIfRunningAgainstService(5000);
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

    public static void uploadDocuments(SearchClient client, List<JsonSerializable<?>> uploadDoc) {
        uploadDocumentsRaw(client,
            uploadDoc.stream().map(TestHelpers::convertToMapStringObject).collect(Collectors.toList()));
        waitForIndexing();
    }

    public static void uploadDocuments(SearchAsyncClient client, List<JsonSerializable<?>> uploadDoc) {
        uploadDocumentsRaw(client,
            uploadDoc.stream().map(TestHelpers::convertToMapStringObject).collect(Collectors.toList()));
        waitForIndexing();
    }

    public static void uploadDocument(SearchClient client, JsonSerializable<?> uploadDoc) {
        uploadDocumentRaw(client, convertToMapStringObject(uploadDoc));
        waitForIndexing();
    }

    public static void uploadDocument(SearchAsyncClient client, JsonSerializable<?> uploadDoc) {
        uploadDocumentRaw(client, convertToMapStringObject(uploadDoc));
        waitForIndexing();
    }

    public static void uploadDocumentRaw(SearchClient client, Map<String, Object> document) {
        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, document)));
        waitForIndexing();
    }

    public static void uploadDocumentRaw(SearchAsyncClient client, Map<String, Object> document) {
        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, document))).block();
        waitForIndexing();
    }

    public static void uploadDocumentsRaw(SearchClient client, List<Map<String, Object>> documents) {
        client.indexDocuments(new IndexDocumentsBatch(documents.stream()
            .map(doc -> createIndexAction(IndexActionType.UPLOAD, doc))
            .collect(Collectors.toList())));
        waitForIndexing();
    }

    public static void uploadDocumentsRaw(SearchAsyncClient client, List<Map<String, Object>> documents) {
        client.indexDocuments(new IndexDocumentsBatch(
            documents.stream().map(doc -> createIndexAction(IndexActionType.UPLOAD, doc)).collect(Collectors.toList())))
            .block();
        waitForIndexing();
    }

    public static Map<String, Object> convertToMapStringObject(JsonSerializable<?> pojo) {
        try (JsonReader jsonReader = JsonProviders.createReader(pojo.toJsonBytes())) {
            return jsonReader.readMap(JsonReader::readUntyped);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T convertFromMapStringObject(Map<String, Object> additionalProperties,
        ReadValueCallback<JsonReader, T> converter) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                jsonWriter.writeMap(additionalProperties, JsonWriter::writeUntyped);
            }

            try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
                return converter.read(jsonReader);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static List<Map<String, Object>> uploadDocumentsJson(SearchClient client, String dataJson) {
        List<Map<String, Object>> documents = readJsonFileToList(dataJson);
        uploadDocumentsRaw(client, documents);

        return documents;
    }

    public static List<Map<String, Object>> uploadDocumentsJson(SearchAsyncClient client, String dataJson) {
        List<Map<String, Object>> documents = readJsonFileToList(dataJson);
        uploadDocumentsRaw(client, documents);

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

    public static SearchIndexClient setupSharedIndex(String indexName, String indexDefinition, String indexData) {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(indexDefinition))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            searchIndexClient.createIndex(createTestIndex(indexName, baseIndex));

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
        if (testMode == TestMode.LIVE) {
            TokenCredential pipelineCredential = tryGetPipelineCredential();
            if (pipelineCredential != null) {
                return pipelineCredential;
            }
            return new AzureCliCredentialBuilder().build();
        } else if (testMode == TestMode.RECORD) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }

    /**
     * Attempts to speculate an {@link AzurePipelinesCredential} from the environment if the running context is within
     * Azure DevOps. If not, returns null.
     *
     * @return The AzurePipelinesCredential if running in Azure DevOps, or null.
     */
    @SuppressWarnings("deprecation")
    private static TokenCredential tryGetPipelineCredential() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        String serviceConnectionId = configuration.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        String clientId = configuration.get("AZURESUBSCRIPTION_CLIENT_ID");
        String tenantId = configuration.get("AZURESUBSCRIPTION_TENANT_ID");
        String systemAccessToken = configuration.get("SYSTEM_ACCESSTOKEN");

        if (CoreUtils.isNullOrEmpty(serviceConnectionId)
            || CoreUtils.isNullOrEmpty(clientId)
            || CoreUtils.isNullOrEmpty(tenantId)
            || CoreUtils.isNullOrEmpty(systemAccessToken)) {
            return null;
        }

        return new AzurePipelinesCredentialBuilder().systemAccessToken(systemAccessToken)
            .clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .build();
    }

    static SearchIndex createTestIndex(String testIndexName, SearchIndex baseIndex) {
        return new SearchIndex(testIndexName, baseIndex.getFields()).setScoringProfiles(baseIndex.getScoringProfiles())
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
        return new AssertingHttpClientBuilder(httpClient).skipRequest((ignoredRequest, ignoredContext) -> false)
            .assertSync()
            .build();
    }

    public static SearchIndexClient createSharedSearchIndexClient() {
        return new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(getTestTokenCredential())
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
            .httpClient(buildSyncAssertingClient(HttpClient.createDefault()))
            .buildClient();
    }

    static byte[] loadResource(String fileName) {
        return LOADED_FILE_DATA.computeIfAbsent(fileName, fName -> {
            try {
                URI fileUri = AutocompleteTests.class.getClassLoader().getResource(fName).toURI();

                return Files.readAllBytes(Paths.get(fileUri));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public static IndexAction createIndexAction(IndexActionType actionType, Map<String, Object> additionalProperties) {
        return new IndexAction().setActionType(actionType).setAdditionalProperties(additionalProperties);
    }

    public static IndexAction convertToIndexAction(JsonSerializable<?> pojo, IndexActionType actionType) {
        return new IndexAction().setActionType(actionType).setAdditionalProperties(convertToMapStringObject(pojo));
    }

    public static RequestOptions ifMatch(String eTag) {
        return new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, eTag);
    }

}
