// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.ContentColumnMapping;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.EmbeddingColumnMapping;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FileUploadMetadata;
import com.azure.search.documents.indexes.models.IndexedSqlKnowledgeSource;
import com.azure.search.documents.indexes.models.IndexedSqlKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.KnowledgeSourceKind;
import com.azure.search.documents.indexes.models.KnowledgeSourceSynchronizationStatus;
import com.azure.search.documents.indexes.models.ListingSearchType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexFieldReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.indexes.models.UpdateKnowledgeSourceFileRequest;
import com.azure.search.documents.indexes.models.UploadKnowledgeSourceFileMultipartRequest;
import com.azure.search.documents.indexes.models.WebKnowledgeSource;
import com.azure.search.documents.indexes.models.WebKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.models.AiServices;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.models.ContentFileDetails;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Knowledge Source operations.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class KnowledgeSourceTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "shared-knowledge-source-index";
    private static final String BLOB_CONNECTION_STRING = "ResourceId=/subscriptions/" + SUBSCRIPTION_ID
        + "/resourceGroups/" + RESOURCE_GROUP + "/providers/Microsoft.Storage/storageAccounts/" + STORAGE_ACCOUNT_NAME;
    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        // Set up any necessary configurations or resources before all tests.
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupIndex();
    }

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        interceptorManager.addMatchers(new com.azure.core.test.models.BodilessMatcher());
    }

    @AfterEach
    public void cleanup() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            // Delete Knowledge Bases first (they reference Knowledge Sources).
            searchIndexClient.listKnowledgeBases()
                .forEach(knowledgeBase -> searchIndexClient.deleteKnowledgeBase(knowledgeBase.getName()));
            // Then delete Knowledge Sources.
            searchIndexClient.listKnowledgeSources()
                .forEach(knowledgeSource -> searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName()));
        }
    }

    @AfterAll
    protected static void cleanupClass() {
        // Clean up any resources after all tests.
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void createKnowledgeSourceSearchIndexSync() {
        // Test creating a knowledge source.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), created.getName());

        SearchIndexKnowledgeSource createdSource = assertInstanceOf(SearchIndexKnowledgeSource.class, created);
        assertEquals(HOTEL_INDEX_NAME, createdSource.getSearchIndexParameters().getSearchIndexName());
    }

    @Test
    public void createKnowledgeSourceSearchIndexAsync() {
        // Test creating a knowledge source.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            assertEquals(knowledgeSource.getName(), created.getName());

            SearchIndexKnowledgeSource createdSource = assertInstanceOf(SearchIndexKnowledgeSource.class, created);
            assertEquals(HOTEL_INDEX_NAME, createdSource.getSearchIndexParameters().getSearchIndexName());
        }).verifyComplete();
    }

    @Test
    public void getKnowledgeSourceSearchIndexSync() {
        // Test getting a knowledge source.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertEquals(knowledgeSource.getName(), retrieved.getName());

        SearchIndexKnowledgeSource retrievedSource = assertInstanceOf(SearchIndexKnowledgeSource.class, retrieved);
        assertEquals(HOTEL_INDEX_NAME, retrievedSource.getSearchIndexParameters().getSearchIndexName());
    }

    @Test
    public void getKnowledgeSourceSearchIndexAsync() {
        // Test getting a knowledge source.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono).assertNext(retrieved -> {
            assertEquals(knowledgeSource.getName(), retrieved.getName());

            SearchIndexKnowledgeSource retrievedSource = assertInstanceOf(SearchIndexKnowledgeSource.class, retrieved);
            assertEquals(HOTEL_INDEX_NAME, retrievedSource.getSearchIndexParameters().getSearchIndexName());
        }).verifyComplete();
    }

    @Test
    public void listKnowledgeSourcesSync() {
        // Test listing knowledge sources.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        long currentCount = searchIndexClient.listKnowledgeSources().stream().count();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        KnowledgeSource knowledgeSource2 = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        searchIndexClient.createKnowledgeSource(knowledgeSource);
        searchIndexClient.createKnowledgeSource(knowledgeSource2);
        Map<String, KnowledgeSource> knowledgeSourcesByName = searchIndexClient.listKnowledgeSources()
            .stream()
            .collect(Collectors.toMap(KnowledgeSource::getName, Function.identity()));

        assertEquals(2, knowledgeSourcesByName.size() - currentCount);
        KnowledgeSource listedSource = knowledgeSourcesByName.get(knowledgeSource.getName());
        assertNotNull(listedSource);
        KnowledgeSource listedSource2 = knowledgeSourcesByName.get(knowledgeSource2.getName());
        assertNotNull(listedSource2);
    }

    @Test
    public void listKnowledgeSourceAsync() {
        // Test listing knowledge sources.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        KnowledgeSource knowledgeSource2 = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        Mono<Tuple2<Long, Map<String, KnowledgeSource>>> tuple2Mono = searchIndexClient.listKnowledgeSources()
            .count()
            .flatMap(currentCount -> Mono
                .when(searchIndexClient.createKnowledgeSource(knowledgeSource),
                    searchIndexClient.createKnowledgeSource(knowledgeSource2))
                .then(searchIndexClient.listKnowledgeSources().collectMap(KnowledgeSource::getName))
                .map(map -> Tuples.of(currentCount, map)));

        StepVerifier.create(tuple2Mono).assertNext(tuple -> {
            Map<String, KnowledgeSource> knowledgeSourcesByName = tuple.getT2();
            assertEquals(2, knowledgeSourcesByName.size() - tuple.getT1());
            KnowledgeSource listedSource = knowledgeSourcesByName.get(knowledgeSource.getName());
            assertNotNull(listedSource);
            KnowledgeSource listedSource2 = knowledgeSourcesByName.get(knowledgeSource2.getName());
            assertNotNull(listedSource2);
        }).verifyComplete();
    }

    @Test
    public void deleteKnowledgeSourceSync() {
        // Test deleting a knowledge source.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(),
            searchIndexClient.getKnowledgeSource(knowledgeSource.getName()).getName());
        searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName());
        assertThrows(HttpResponseException.class,
            () -> searchIndexClient.getKnowledgeSource(knowledgeSource.getName()));
    }

    @Test
    public void deleteKnowledgeSourceAsync() {
        // Test deleting a knowledge source.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono)
            .assertNext(retrieved -> assertEquals(knowledgeSource.getName(), retrieved.getName()))
            .verifyComplete();

        StepVerifier.create(searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName())).verifyComplete();

        StepVerifier.create(searchIndexClient.getKnowledgeSource(knowledgeSource.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void updateKnowledgeSourceSearchIndexSync() {
        // Test updating a knowledge source.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        searchIndexClient.createKnowledgeSource(knowledgeSource);
        String newDescription = "Updated description";
        knowledgeSource.setDescription(newDescription);
        searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);
        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertEquals(newDescription, retrieved.getDescription());
    }

    @Test
    public void updateKnowledgeSourceSearchIndexAsync() {
        // Test updating a knowledge source.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        String newDescription = "Updated description";

        Mono<KnowledgeSource> createUpdateAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.createOrUpdateKnowledgeSource(created.setDescription(newDescription)))
            .flatMap(updated -> searchIndexClient.getKnowledgeSource(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono)
            .assertNext(retrieved -> assertEquals(newDescription, retrieved.getDescription()))
            .verifyComplete();
    }

    @Test
    public void statusPayloadMapsToModelsWithNullables() throws IOException {
        // Sample status payload with nullables for first sync
        String statusJson = "{\"synchronizationStatus\": \"creating\",\"synchronizationInterval\": \"1d\","
            + "\"currentSynchronizationState\": null,\"lastSynchronizationState\": null,\"statistics\": {"
            + "\"totalSynchronization\": 0,\"averageSynchronizationDuration\": \"PT0S\","
            + "\"averageItemsProcessedPerSynchronization\": 0}}";

        try (JsonReader reader = JsonProviders.createReader(statusJson)) {
            KnowledgeSourceStatus status = KnowledgeSourceStatus.fromJson(reader);

            assertNotNull(status);
            assertEquals(KnowledgeSourceSynchronizationStatus.CREATING, status.getSynchronizationStatus());
            assertEquals(Duration.ofDays(1), status.getSynchronizationInterval());

            assertNull(status.getCurrentSynchronizationState());
            assertNull(status.getLastSynchronizationState());

            // Statistics object exists with actual available fields
            assertNotNull(status.getStatistics());
            assertEquals(0, status.getStatistics().getTotalSynchronization());
            assertEquals(Duration.ZERO, status.getStatistics().getAverageSynchronizationDuration());
            assertEquals(0, status.getStatistics().getAverageItemsProcessedPerSynchronization());
        }

        Map<String, Duration> supportedIntervals = new LinkedHashMap<>();
        supportedIntervals.put("2h", Duration.ofHours(2));
        supportedIntervals.put("30m", Duration.ofMinutes(30));
        supportedIntervals.put("45s", Duration.ofSeconds(45));
        supportedIntervals.put("P1D", Duration.ofDays(1));
        supportedIntervals.put("PT30M", Duration.ofMinutes(30));
        for (Map.Entry<String, Duration> interval : supportedIntervals.entrySet()) {
            assertEquals(interval.getValue(), deserializeStatus(interval.getKey()).getSynchronizationInterval());
        }

        assertNull(deserializeStatus(null).getSynchronizationInterval());
        assertThrows(DateTimeParseException.class, () -> deserializeStatus("1w"));
    }

    private static KnowledgeSourceStatus deserializeStatus(String synchronizationInterval) throws IOException {
        String serializedValue = synchronizationInterval == null ? "null" : "\"" + synchronizationInterval + "\"";
        String statusJson
            = "{\"synchronizationStatus\":\"active\",\"synchronizationInterval\":" + serializedValue + "}";
        try (JsonReader reader = JsonProviders.createReader(statusJson)) {
            return KnowledgeSourceStatus.fromJson(reader);
        }
    }

    @Test
    public void putNewKnowledgeSourceReturns201() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        try {
            KnowledgeSource created = client.createKnowledgeSource(knowledgeSource);
            assertNotNull(created);
            assertEquals(knowledgeSource.getName(), created.getName());
        } finally {
            client.deleteKnowledgeSource(knowledgeSource.getName());
        }
    }

    @Test
    public void putExistingKnowledgeSourceReturns200() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        String newDescription = "Updated description";

        try {
            client.createKnowledgeSource(knowledgeSource);

            knowledgeSource.setDescription(newDescription);
            KnowledgeSource updated = client.createOrUpdateKnowledgeSource(knowledgeSource);
            assertNotNull(updated);
            assertEquals(newDescription, updated.getDescription());

            KnowledgeSource retrieved = client.getKnowledgeSource(knowledgeSource.getName());
            assertEquals(newDescription, retrieved.getDescription());
        } finally {
            client.deleteKnowledgeSource(knowledgeSource.getName());
        }
    }

    @Test
    public void deleteKnowledgeSourceRemovesSource() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        client.createKnowledgeSource(knowledgeSource);
        client.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> client.getKnowledgeSource(knowledgeSource.getName()));
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Test
    public void listKnowledgeSourcesReturnsAllResources() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
        long initialCount = client.listKnowledgeSources().stream().count();

        KnowledgeSource ks1 = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        KnowledgeSource ks2 = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));
        try {
            client.createKnowledgeSource(ks1);
            client.createKnowledgeSource(ks2);

            Map<String, KnowledgeSource> knowledgeSourcesByName = client.listKnowledgeSources()
                .stream()
                .collect(Collectors.toMap(KnowledgeSource::getName, Function.identity()));

            assertEquals(initialCount + 2, knowledgeSourcesByName.size());
            assertTrue(knowledgeSourcesByName.containsKey(ks1.getName()));
            assertTrue(knowledgeSourcesByName.containsKey(ks2.getName()));
        } finally {
            client.deleteKnowledgeSource(ks1.getName());
            client.deleteKnowledgeSource(ks2.getName());
        }
    }

    @Test
    public void knowledgeSourceParametersSetsFieldsCorrectly() {
        SearchIndexKnowledgeSourceParameters params = new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME);

        assertEquals(HOTEL_INDEX_NAME, params.getSearchIndexName());

        params.setSemanticConfigurationName("semantic-config");
        assertEquals("semantic-config", params.getSemanticConfigurationName());

        params.setSourceDataFields(new SearchIndexFieldReference("field1"), new SearchIndexFieldReference("field2"));
        assertEquals(2, params.getSourceDataFields().size());
        assertEquals("field1", params.getSourceDataFields().get(0).getName());
        assertEquals("field2", params.getSourceDataFields().get(1).getName());

        params.setSearchFields(new SearchIndexFieldReference("searchField1"));
        assertEquals(1, params.getSearchFields().size());
        assertEquals("searchField1", params.getSearchFields().get(0).getName());

        SearchIndexKnowledgeSourceParameters result = params.setSemanticConfigurationName("another-config");
        assertSame(params, result);
    }

    @Test
    public void createWebKnowledgeSourceMinimal() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName());

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        assertEquals(webKS.getName(), created.getName());
        WebKnowledgeSource createdWeb = assertInstanceOf(WebKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.WEB, createdWeb.getKind());
    }

    @Test
    public void createWebKnowledgeSourceWithParameters() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        WebKnowledgeSourceParameters webParams = new WebKnowledgeSourceParameters();

        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName()).setWebParameters(webParams)
            .setDescription("Web KS with parameters");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        WebKnowledgeSource createdWeb = assertInstanceOf(WebKnowledgeSource.class, created);
        assertEquals("Web KS with parameters", createdWeb.getDescription());
        assertNotNull(createdWeb.getWebParameters());
    }

    @Test
    public void updateWebKnowledgeSourceDescription() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName());

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        String newDescription = "Updated Web KS description";
        WebKnowledgeSource updatedWeb = (WebKnowledgeSource) created.setDescription(newDescription);

        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(updatedWeb);

        WebKnowledgeSource retrievedWeb = assertInstanceOf(WebKnowledgeSource.class, updated);
        assertEquals(newDescription, retrievedWeb.getDescription());
    }

    @Test
    public void updateWebKnowledgeSourceParameters() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName());

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        WebKnowledgeSourceParameters newParams = new WebKnowledgeSourceParameters();
        WebKnowledgeSource updatedWeb = (WebKnowledgeSource) created;
        updatedWeb.setWebParameters(newParams);

        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(updatedWeb);

        WebKnowledgeSource retrievedWeb = assertInstanceOf(WebKnowledgeSource.class, updated);
        assertNotNull(retrievedWeb.getWebParameters());
    }

    @Test
    public void listWebKnowledgeSourcesIncludesWebType() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        WebKnowledgeSource webKs
            = new WebKnowledgeSource(randomKnowledgeSourceName()).setDescription("Web KS for listing test");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKs);

        Map<String, KnowledgeSource> knowledgeSourcesByName = searchIndexClient.listKnowledgeSources()
            .stream()
            .collect(Collectors.toMap(KnowledgeSource::getName, Function.identity()));

        assertTrue(knowledgeSourcesByName.containsKey(created.getName()));
        KnowledgeSource listed = knowledgeSourcesByName.get(created.getName());
        WebKnowledgeSource listedWeb = assertInstanceOf(WebKnowledgeSource.class, listed);
        assertEquals(KnowledgeSourceKind.WEB, listedWeb.getKind());
    }

    @Test
    public void deleteWebKnowledgeSourceRemovesResource() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        WebKnowledgeSource webKS
            = new WebKnowledgeSource(randomKnowledgeSourceName()).setDescription("Web KS to be deleted");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(created.getName());
        assertNotNull(retrieved);

        searchIndexClient.deleteKnowledgeSource(created.getName());

        HttpResponseException ex
            = assertThrows(HttpResponseException.class, () -> searchIndexClient.getKnowledgeSource(created.getName()));
        assertEquals(404, ex.getResponse().getStatusCode());
    }

    @Test
    public void createWebKnowledgeSourceWithNullParameters() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName()).setWebParameters(null);

        try {
            KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);
            WebKnowledgeSource createdWeb = assertInstanceOf(WebKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.WEB, createdWeb.getKind());
        } catch (HttpResponseException e) {
            assertTrue(e.getResponse().getStatusCode() >= 400 && e.getResponse().getStatusCode() < 500);
        }
    }

    @Test
    public void createKnowledgeSourceWithInvalidName() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        try {
            WebKnowledgeSource webKS = new WebKnowledgeSource("");
            HttpResponseException ex
                = assertThrows(HttpResponseException.class, () -> searchIndexClient.createKnowledgeSource(webKS));
            assertTrue(ex.getResponse().getStatusCode() >= 400 && ex.getResponse().getStatusCode() < 500);

        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected exception for null name
            assertTrue(true);
        }

        try {
            WebKnowledgeSource webKS = new WebKnowledgeSource(null);
            HttpResponseException ex2
                = assertThrows(HttpResponseException.class, () -> searchIndexClient.createKnowledgeSource(webKS));
            assertTrue(ex2.getResponse().getStatusCode() >= 400 && ex2.getResponse().getStatusCode() < 500);

        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected exception for null name
            assertTrue(true);
        }
    }

    @Test
    public void webKnowledgeSourceResponseShapeValidation() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        WebKnowledgeSource webKS
            = new WebKnowledgeSource(randomKnowledgeSourceName()).setDescription("Test for response modeling");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        WebKnowledgeSource createdWeb = assertInstanceOf(WebKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.WEB, createdWeb.getKind());
        assertNotNull(createdWeb.getName());
        assertEquals("Test for response modeling", createdWeb.getDescription());

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(created.getName());
        WebKnowledgeSource retrievedWeb = assertInstanceOf(WebKnowledgeSource.class, retrieved);
        assertEquals(KnowledgeSourceKind.WEB, retrievedWeb.getKind());
        assertEquals(createdWeb.getName(), retrievedWeb.getName());
        assertEquals("Test for response modeling", retrievedWeb.getDescription());
    }

    @Test
    public void webKnowledgeSourceJsonSerializationRoundTrip() {
        WebKnowledgeSource webKS
            = new WebKnowledgeSource(randomKnowledgeSourceName()).setDescription("JSON serialization test");

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
                webKS.toJson(writer);  // Real method from WebKnowledgeSource
            }
            String json = outputStream.toString();

            assertTrue(json.contains("\"kind\":\"web\""));
            assertTrue(json.contains("\"name\":"));
            assertTrue(json.contains(webKS.getName()));

            try (JsonReader reader = JsonProviders.createReader(json)) {
                WebKnowledgeSource deserialized = WebKnowledgeSource.fromJson(reader);  // Real static method
                assertEquals(KnowledgeSourceKind.WEB, deserialized.getKind());
                assertEquals(webKS.getName(), deserialized.getName());
                assertEquals(webKS.getDescription(), deserialized.getDescription());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    public void webKnowledgeSourceInheritsKnowledgeSourceBehavior() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        WebKnowledgeSource webKS
            = new WebKnowledgeSource(randomKnowledgeSourceName()).setDescription("Inheritance test")
                .setETag("test-etag");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        // Verify inherited properties work
        assertNotNull(created.getName());
        assertNotNull(created.getDescription());

        assertInstanceOf(WebKnowledgeSource.class, created);

        String newDescription = "Updated via base class";
        created.setDescription(newDescription);

        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(created);
        assertEquals(newDescription, updated.getDescription());
    }

    @Test
    public void createFileKnowledgeSourceMinimalSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), created.getName());
        FileKnowledgeSource createdSource = assertInstanceOf(FileKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.FILE, createdSource.getKind());
        assertNotNull(createdSource.getFileParameters());
    }

    @Test
    public void createFileKnowledgeSourceMinimalAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            assertEquals(knowledgeSource.getName(), created.getName());
            FileKnowledgeSource createdSource = assertInstanceOf(FileKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.FILE, createdSource.getKind());
            assertNotNull(createdSource.getFileParameters());
        }).verifyComplete();
    }

    @Test
    public void createFileKnowledgeSourceWithIngestionParamsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params)
            .setDescription("File KS with embedding model");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        FileKnowledgeSource createdSource = assertInstanceOf(FileKnowledgeSource.class, created);
        assertEquals("File KS with embedding model", createdSource.getDescription());
        assertNotNull(createdSource.getFileParameters().getIngestionParameters());
    }

    @Test
    public void createFileKnowledgeSourceWithIngestionParamsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params)
            .setDescription("File KS with embedding model");

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            FileKnowledgeSource createdSource = assertInstanceOf(FileKnowledgeSource.class, created);
            assertEquals("File KS with embedding model", createdSource.getDescription());
            assertNotNull(createdSource.getFileParameters().getIngestionParameters());
        }).verifyComplete();
    }

    @Test
    public void getFileKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource
            = new FileKnowledgeSource(randomKnowledgeSourceName(), params).setDescription("File KS for get test");

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertEquals(knowledgeSource.getName(), retrieved.getName());
        FileKnowledgeSource retrievedSource = assertInstanceOf(FileKnowledgeSource.class, retrieved);
        assertEquals("File KS for get test", retrievedSource.getDescription());
        assertEquals(KnowledgeSourceKind.FILE, retrievedSource.getKind());
    }

    @Test
    public void getFileKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource
            = new FileKnowledgeSource(randomKnowledgeSourceName(), params).setDescription("File KS for get test");

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()))).assertNext(retrieved -> {
                assertEquals(knowledgeSource.getName(), retrieved.getName());
                FileKnowledgeSource retrievedSource = assertInstanceOf(FileKnowledgeSource.class, retrieved);
                assertEquals("File KS for get test", retrievedSource.getDescription());
                assertEquals(KnowledgeSourceKind.FILE, retrievedSource.getKind());
            }).verifyComplete();
    }

    @Test
    public void updateFileKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        knowledgeSource.setDescription("Updated File KS description");
        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);

        assertEquals("Updated File KS description", updated.getDescription());
        FileKnowledgeSource updatedSource = assertInstanceOf(FileKnowledgeSource.class, updated);
        assertEquals(KnowledgeSourceKind.FILE, updatedSource.getKind());
    }

    @Test
    public void updateFileKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createUpdateAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient
                .createOrUpdateKnowledgeSource(created.setDescription("Updated File KS description")))
            .flatMap(updated -> searchIndexClient.getKnowledgeSource(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono).assertNext(retrieved -> {
            assertEquals("Updated File KS description", retrieved.getDescription());
            FileKnowledgeSource retrievedSource = assertInstanceOf(FileKnowledgeSource.class, retrieved);
            assertEquals(KnowledgeSourceKind.FILE, retrievedSource.getKind());
        }).verifyComplete();
    }

    @Test
    public void deleteFileKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertNotNull(retrieved);

        searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> searchIndexClient.getKnowledgeSource(knowledgeSource.getName()));
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteFileKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono)
            .assertNext(retrieved -> assertEquals(knowledgeSource.getName(), retrieved.getName()))
            .verifyComplete();

        StepVerifier.create(searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName())).verifyComplete();

        StepVerifier.create(searchIndexClient.getKnowledgeSource(knowledgeSource.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceMinimalSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), created.getName());
        IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.INDEXED_SQL, createdSource.getKind());
        assertNotNull(createdSource.getIndexedSqlParameters());
        assertEquals("dbo.Hotels", createdSource.getIndexedSqlParameters().getTableOrView());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceMinimalAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            assertEquals(knowledgeSource.getName(), created.getName());
            IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.INDEXED_SQL, createdSource.getKind());
            assertNotNull(createdSource.getIndexedSqlParameters());
            assertEquals("dbo.Hotels", createdSource.getIndexedSqlParameters().getTableOrView());
        }).verifyComplete();
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceWithContentColumnsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        params.setContentColumns(Arrays.asList(new ContentColumnMapping("title", "Title", "Edm.String"),
            new ContentColumnMapping("body", "Description", "Edm.String")));
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params)
            .setDescription("SQL KS with content columns");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
        assertEquals("SQL KS with content columns", createdSource.getDescription());
        assertNotNull(createdSource.getIndexedSqlParameters().getContentColumns());
        assertEquals(2, createdSource.getIndexedSqlParameters().getContentColumns().size());
        assertEquals("title", createdSource.getIndexedSqlParameters().getContentColumns().get(0).getName());
        assertEquals("Title", createdSource.getIndexedSqlParameters().getContentColumns().get(0).getSourceField());
        assertEquals("Edm.String",
            createdSource.getIndexedSqlParameters().getContentColumns().get(0).getSearchFieldType());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceWithContentColumnsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        params.setContentColumns(Arrays.asList(new ContentColumnMapping("title", "Title", "Edm.String"),
            new ContentColumnMapping("body", "Description", "Edm.String")));
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params)
            .setDescription("SQL KS with content columns");

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
            assertEquals("SQL KS with content columns", createdSource.getDescription());
            assertNotNull(createdSource.getIndexedSqlParameters().getContentColumns());
            assertEquals(2, createdSource.getIndexedSqlParameters().getContentColumns().size());
            assertEquals("title", createdSource.getIndexedSqlParameters().getContentColumns().get(0).getName());
            assertEquals("Title", createdSource.getIndexedSqlParameters().getContentColumns().get(0).getSourceField());
        }).verifyComplete();
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceWithEmbeddingColumnsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        params.setContentColumns(
            Collections.singletonList(new ContentColumnMapping("description", "Description", "Edm.String")));
        params.setEmbeddingColumns(
            Collections.singletonList(new EmbeddingColumnMapping("descriptionVector", "Description")));
        params.setIngestionParameters(new KnowledgeSourceIngestionParameters()
            .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                    .setDeploymentName("text-embedding-3-large")
                    .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
        assertNotNull(createdSource.getIndexedSqlParameters().getEmbeddingColumns());
        assertEquals(1, createdSource.getIndexedSqlParameters().getEmbeddingColumns().size());
        assertEquals("descriptionVector",
            createdSource.getIndexedSqlParameters().getEmbeddingColumns().get(0).getName());
        assertEquals("Description",
            createdSource.getIndexedSqlParameters().getEmbeddingColumns().get(0).getSourceField());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceWithEmbeddingColumnsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        params.setContentColumns(
            Collections.singletonList(new ContentColumnMapping("description", "Description", "Edm.String")));
        params.setEmbeddingColumns(
            Collections.singletonList(new EmbeddingColumnMapping("descriptionVector", "Description")));
        params.setIngestionParameters(new KnowledgeSourceIngestionParameters()
            .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                    .setDeploymentName("text-embedding-3-large")
                    .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
            assertNotNull(createdSource.getIndexedSqlParameters().getEmbeddingColumns());
            assertEquals(1, createdSource.getIndexedSqlParameters().getEmbeddingColumns().size());
            assertEquals("descriptionVector",
                createdSource.getIndexedSqlParameters().getEmbeddingColumns().get(0).getName());
            assertEquals("Description",
                createdSource.getIndexedSqlParameters().getEmbeddingColumns().get(0).getSourceField());
        }).verifyComplete();
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceWithHighWaterMarkSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.HotelsView");
        params.setHighWaterMarkColumnName("RowVersion");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
        assertEquals("RowVersion", createdSource.getIndexedSqlParameters().getHighWaterMarkColumnName());
        assertEquals("dbo.HotelsView", createdSource.getIndexedSqlParameters().getTableOrView());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void createIndexedSqlKnowledgeSourceWithHighWaterMarkAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.HotelsView");
        params.setHighWaterMarkColumnName("RowVersion");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            IndexedSqlKnowledgeSource createdSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, created);
            assertEquals("RowVersion", createdSource.getIndexedSqlParameters().getHighWaterMarkColumnName());
            assertEquals("dbo.HotelsView", createdSource.getIndexedSqlParameters().getTableOrView());
        }).verifyComplete();
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void getIndexedSqlKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        params.setContentColumns(
            Collections.singletonList(new ContentColumnMapping("hotelName", "HotelName", "Edm.String")));
        IndexedSqlKnowledgeSource knowledgeSource
            = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params).setDescription("SQL KS for get test");

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertEquals(knowledgeSource.getName(), retrieved.getName());
        IndexedSqlKnowledgeSource retrievedSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, retrieved);
        assertEquals("SQL KS for get test", retrievedSource.getDescription());
        assertEquals("dbo.Hotels", retrievedSource.getIndexedSqlParameters().getTableOrView());
        assertNotNull(retrievedSource.getIndexedSqlParameters().getContentColumns());
        assertEquals("hotelName", retrievedSource.getIndexedSqlParameters().getContentColumns().get(0).getName());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void getIndexedSqlKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        params.setContentColumns(
            Collections.singletonList(new ContentColumnMapping("hotelName", "HotelName", "Edm.String")));
        IndexedSqlKnowledgeSource knowledgeSource
            = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params).setDescription("SQL KS for get test");

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()))).assertNext(retrieved -> {
                assertEquals(knowledgeSource.getName(), retrieved.getName());
                IndexedSqlKnowledgeSource retrievedSource
                    = assertInstanceOf(IndexedSqlKnowledgeSource.class, retrieved);
                assertEquals("SQL KS for get test", retrievedSource.getDescription());
                assertEquals("dbo.Hotels", retrievedSource.getIndexedSqlParameters().getTableOrView());
                assertNotNull(retrievedSource.getIndexedSqlParameters().getContentColumns());
                assertEquals("hotelName",
                    retrievedSource.getIndexedSqlParameters().getContentColumns().get(0).getName());
            }).verifyComplete();
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void updateIndexedSqlKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        knowledgeSource.setDescription("Updated SQL KS description");
        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);

        assertEquals("Updated SQL KS description", updated.getDescription());
        IndexedSqlKnowledgeSource updatedSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, updated);
        assertEquals("dbo.Hotels", updatedSource.getIndexedSqlParameters().getTableOrView());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void updateIndexedSqlKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createUpdateAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient
                .createOrUpdateKnowledgeSource(created.setDescription("Updated SQL KS description")))
            .flatMap(updated -> searchIndexClient.getKnowledgeSource(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono).assertNext(retrieved -> {
            assertEquals("Updated SQL KS description", retrieved.getDescription());
            IndexedSqlKnowledgeSource retrievedSource = assertInstanceOf(IndexedSqlKnowledgeSource.class, retrieved);
            assertEquals("dbo.Hotels", retrievedSource.getIndexedSqlParameters().getTableOrView());
        }).verifyComplete();
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void deleteIndexedSqlKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertNotNull(retrieved);

        searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> searchIndexClient.getKnowledgeSource(knowledgeSource.getName()));
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Disabled("Requires a real Azure SQL database connection")
    @Test
    public void deleteIndexedSqlKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedSqlKnowledgeSourceParameters params = new IndexedSqlKnowledgeSourceParameters(
            "Server=tcp:fakeserver.database.windows.net,1433;Database=testdb;User ID=reader;Password=fakePass;",
            "dbo.Hotels");
        IndexedSqlKnowledgeSource knowledgeSource = new IndexedSqlKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono)
            .assertNext(retrieved -> assertEquals(knowledgeSource.getName(), retrieved.getName()))
            .verifyComplete();

        StepVerifier.create(searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName())).verifyComplete();

        StepVerifier.create(searchIndexClient.getKnowledgeSource(knowledgeSource.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void createWebKnowledgeSourceWithRetrieveDefaultsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        WebKnowledgeSourceParameters webParams
            = new WebKnowledgeSourceParameters().setCount(5).setFreshness("Day").setLanguage("en").setMarket("en-US");
        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName()).setWebParameters(webParams);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(webKS);

        WebKnowledgeSource createdWeb = assertInstanceOf(WebKnowledgeSource.class, created);
        WebKnowledgeSourceParameters createdParams = createdWeb.getWebParameters();
        assertNotNull(createdParams);
        assertEquals(5, createdParams.getCount());
        assertEquals("Day", createdParams.getFreshness());
        assertEquals("en", createdParams.getLanguage());
        assertEquals("en-US", createdParams.getMarket());
    }

    @Test
    public void createWebKnowledgeSourceWithRetrieveDefaultsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        WebKnowledgeSourceParameters webParams
            = new WebKnowledgeSourceParameters().setCount(5).setFreshness("Day").setLanguage("en").setMarket("en-US");
        WebKnowledgeSource webKS = new WebKnowledgeSource(randomKnowledgeSourceName()).setWebParameters(webParams);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(webKS)).assertNext(created -> {
            WebKnowledgeSource createdWeb = assertInstanceOf(WebKnowledgeSource.class, created);
            WebKnowledgeSourceParameters createdParams = createdWeb.getWebParameters();
            assertNotNull(createdParams);
            assertEquals(5, createdParams.getCount());
            assertEquals("Day", createdParams.getFreshness());
            assertEquals("en", createdParams.getLanguage());
            assertEquals("en-US", createdParams.getMarket());
        }).verifyComplete();
    }

    @Test
    public void listFilesOnEmptyFileKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        List<KnowledgeSourceFile> files = searchIndexClient.listKnowledgeSourceFiles(knowledgeSource.getName())
            .stream()
            .collect(Collectors.toList());
        assertNotNull(files);
        assertTrue(files.isEmpty(), "Newly created File KS should have no files");
    }

    @Test
    public void listFilesOnEmptyFileKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSourceParameters params
            = new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl("https://fake-aoai.openai.azure.com")
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<List<KnowledgeSourceFile>> createAndListMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.listKnowledgeSourceFiles(created.getName()).collectList());

        StepVerifier.create(createAndListMono).assertNext(files -> {
            assertNotNull(files);
            assertTrue(files.isEmpty(), "Newly created File KS should have no files");
        }).verifyComplete();
    }

    @Test
    public void createFileKnowledgeSourceWithStandardExtractionAndCorsSync() {
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(new TestProxySanitizer("$..aiServices.uri", null,
                "https://your-endpoint.cognitiveservices.azure.com", TestProxySanitizerType.BODY_KEY));
        }

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSourceIngestionParameters ingestionParameters = createFileKnowledgeSourceIngestionParameters()
            .setContentExtractionMode(KnowledgeSourceContentExtractionMode.STANDARD)
            .setAiServices(new AiServices(AI_SERVICES_ENDPOINT).setApiKey(AI_SERVICES_API_KEY));
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(),
            new FileKnowledgeSourceParameters().setIngestionParameters(ingestionParameters))
                .setCorsOptions(new CorsOptions("https://app.contoso.com").setMaxAgeInSeconds(300L));

        FileKnowledgeSource created
            = assertInstanceOf(FileKnowledgeSource.class, searchIndexClient.createKnowledgeSource(knowledgeSource));

        assertEquals(KnowledgeSourceContentExtractionMode.STANDARD,
            created.getFileParameters().getIngestionParameters().getContentExtractionMode());
        assertEquals(AI_SERVICES_ENDPOINT,
            created.getFileParameters().getIngestionParameters().getAiServices().getUrl());
        assertEquals(Collections.singletonList("https://app.contoso.com"),
            created.getCorsOptions().getAllowedOrigins());
        assertEquals(300L, created.getCorsOptions().getMaxAgeInSeconds());
    }

    @Test
    public void createFileKnowledgeSourceWithStandardExtractionWithoutAiServicesFailsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSourceIngestionParameters ingestionParameters = createFileKnowledgeSourceIngestionParameters()
            .setContentExtractionMode(KnowledgeSourceContentExtractionMode.STANDARD);
        FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(randomKnowledgeSourceName(),
            new FileKnowledgeSourceParameters().setIngestionParameters(ingestionParameters));

        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> searchIndexClient.createKnowledgeSource(knowledgeSource));

        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @Test
    @Disabled("Requires a configured Azure OpenAI embedding deployment")
    public void uploadFileKnowledgeSourceFileWithMetadataSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSource knowledgeSource = createFileKnowledgeSource(searchIndexClient);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("team", "kr");
        metadata.put("source", "build26-notes");

        KnowledgeSourceFile uploaded = searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSource.getName(),
            createFileUploadRequest("notes/build26/kr-features.md",
                "# File Knowledge Source updates\n\nThis document describes the August File KS upload improvements.",
                metadata));

        assertNotNull(uploaded.getFileId());
        assertEquals("notes/build26/kr-features.md", uploaded.getFileName());
        assertEquals("notes/build26/", uploaded.getPrefix());
        assertEquals(metadata, uploaded.getMetadata());
        assertEquals("markdown", uploaded.getParsingMode().toString());
        assertNotNull(uploaded.getExtractionMode());
        assertNotNull(uploaded.getCreatedAt());
        assertNotNull(uploaded.getLastUpdatedAt());
        assertNull(uploaded.getErrorMessage());
    }

    @Test
    @Disabled("Requires a configured Azure OpenAI embedding deployment")
    public void listFileKnowledgeSourceFilesByPrefixSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSource knowledgeSource = createFileKnowledgeSource(searchIndexClient);
        searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSource.getName(),
            createFileUploadRequest("notes/build26/kr-features.md",
                "# Build 26\n\nKnowledge retrieval features planned for the Build 26 release.",
                Collections.emptyMap()));
        searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSource.getName(),
            createFileUploadRequest("notes/build27/kr-features.md",
                "# Build 27\n\nKnowledge retrieval features planned for the Build 27 release.",
                Collections.emptyMap()));

        List<KnowledgeSourceFile> files = searchIndexClient
            .listKnowledgeSourceFiles(knowledgeSource.getName(), "notes/build26/", null, null, ListingSearchType.PREFIX)
            .stream()
            .collect(Collectors.toList());

        assertEquals(1, files.size());
        assertEquals("notes/build26/kr-features.md", files.get(0).getFileName());
        assertEquals("notes/build26/", files.get(0).getPrefix());
    }

    @Test
    @Disabled("Requires a configured Azure OpenAI embedding deployment")
    public void updateFileKnowledgeSourceFileInPlaceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FileKnowledgeSource knowledgeSource = createFileKnowledgeSource();
        Map<String, String> originalMetadata = Collections.singletonMap("version", "1");
        Map<String, String> updatedMetadata = Collections.singletonMap("version", "2");

        Mono<KnowledgeSourceFile> uploadAndUpdate = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .then(searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSource.getName(),
                createFileUploadRequest("notes/build26/kr-features.md",
                    "# Version 1\n\nThe original File KS document content.", originalMetadata)))
            .flatMap(
                uploaded -> searchIndexClient
                    .updateKnowledgeSourceFile(uploaded.getFileId(), knowledgeSource.getName(),
                        new UpdateKnowledgeSourceFileRequest(
                            new FileUploadMetadata().setFileName(uploaded.getFileName()).setMetadata(updatedMetadata),
                            createFileContent("# Version 2\n\nThe updated File KS document content.",
                                "kr-features.md")))
                    .map(updated -> {
                        assertEquals(uploaded.getFileId(), updated.getFileId());
                        return updated;
                    }));

        StepVerifier.create(uploadAndUpdate).assertNext(updated -> {
            assertEquals("notes/build26/kr-features.md", updated.getFileName());
            assertEquals("notes/build26/", updated.getPrefix());
            assertEquals(updatedMetadata, updated.getMetadata());
            assertNotNull(updated.getExtractionMode());
            assertTrue(!updated.getLastUpdatedAt().isBefore(updated.getCreatedAt()));
        }).verifyComplete();
    }

    @Test
    public void uploadFileKnowledgeSourceFileRejectsPathTraversalSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FileKnowledgeSource knowledgeSource = createFileKnowledgeSource(searchIndexClient);

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSource.getName(),
                createFileUploadRequest("../kr-features.md",
                    "# Invalid path\n\nThis upload must be rejected because its path traverses a parent directory.",
                    Collections.emptyMap())));

        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @Test
    public void createSearchIndexKnowledgeSourceWithBaseFilterSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        SearchIndexKnowledgeSourceParameters params
            = new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME).setBaseFilter("Category eq 'Budget'");
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        SearchIndexKnowledgeSource createdSource = assertInstanceOf(SearchIndexKnowledgeSource.class, created);
        assertEquals("Category eq 'Budget'", createdSource.getSearchIndexParameters().getBaseFilter());
    }

    @Test
    public void createSearchIndexKnowledgeSourceWithBaseFilterAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        SearchIndexKnowledgeSourceParameters params
            = new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME).setBaseFilter("Category eq 'Budget'");
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            SearchIndexKnowledgeSource createdSource = assertInstanceOf(SearchIndexKnowledgeSource.class, created);
            assertEquals("Category eq 'Budget'", createdSource.getSearchIndexParameters().getBaseFilter());
        }).verifyComplete();
    }

    private FileKnowledgeSource createFileKnowledgeSource(SearchIndexClient client) {
        FileKnowledgeSource knowledgeSource = createFileKnowledgeSource();
        client.createKnowledgeSource(knowledgeSource);
        return knowledgeSource;
    }

    private FileKnowledgeSource createFileKnowledgeSource() {
        return new FileKnowledgeSource(randomKnowledgeSourceName(),
            new FileKnowledgeSourceParameters().setIngestionParameters(createFileKnowledgeSourceIngestionParameters()));
    }

    private KnowledgeSourceIngestionParameters createFileKnowledgeSourceIngestionParameters() {
        return new KnowledgeSourceIngestionParameters()
            .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer()
                .setAzureOpenAIParameters(new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
                    .setDeploymentName(OPENAI_EMBEDDING_DEPLOYMENT_NAME)
                    .setModelName(AzureOpenAIModelName.fromString(OPENAI_EMBEDDING_MODEL_NAME))
                    .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY))))
            .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL);
    }

    private static UploadKnowledgeSourceFileMultipartRequest createFileUploadRequest(String fileName, String contents,
        Map<String, String> metadata) {
        FileUploadMetadata fileMetadata = new FileUploadMetadata().setFileName(fileName).setMetadata(metadata);
        String contentFileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        return new UploadKnowledgeSourceFileMultipartRequest(fileMetadata,
            createFileContent(contents, contentFileName));
    }

    private static ContentFileDetails createFileContent(String contents, String fileName) {
        return new ContentFileDetails(BinaryData.fromString(contents)).setFilename(fileName)
            .setContentType(fileName.endsWith(".md") ? "text/markdown; charset=utf-8" : "text/plain; charset=utf-8");
    }

    private String randomKnowledgeSourceName() {
        return testResourceNamer.randomName("knowledge-source-", 63).toLowerCase();
    }

    private static SearchIndexClient setupIndex() {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            SemanticConfiguration semanticConfiguration = new SemanticConfiguration("semantic-config",
                new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                    .setContentFields(new SemanticField("Description"))
                    .setKeywordsFields(new SemanticField("Category")));
            SemanticSearch semanticSearch = new SemanticSearch().setDefaultConfigurationName("semantic-config")
                .setConfigurations(semanticConfiguration);
            searchIndexClient.createOrUpdateIndex(
                TestHelpers.createTestIndex(HOTEL_INDEX_NAME, baseIndex).setSemanticSearch(semanticSearch));

            return searchIndexClient;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
