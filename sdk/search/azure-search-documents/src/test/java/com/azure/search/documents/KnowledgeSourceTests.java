// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.FabricDataAgentKnowledgeSource;
import com.azure.search.documents.indexes.models.FabricDataAgentKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FabricOntologyKnowledgeSource;
import com.azure.search.documents.indexes.models.FabricOntologyKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeSource;

import com.azure.search.documents.indexes.models.KnowledgeSourceKind;
import com.azure.search.documents.indexes.models.KnowledgeSourceSynchronizationStatus;
import com.azure.search.documents.indexes.models.McpServerAuthentication;
import com.azure.search.documents.indexes.models.McpServerFoundryConnectionAuthentication;
import com.azure.search.documents.indexes.models.McpServerFoundryConnectionParameters;
import com.azure.search.documents.indexes.models.McpServerHeaders;
import com.azure.search.documents.indexes.models.McpServerJsonOutputParsing;
import com.azure.search.documents.indexes.models.McpServerKnowledgeSource;
import com.azure.search.documents.indexes.models.McpServerKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.McpServerNoneOutputParsing;
import com.azure.search.documents.indexes.models.McpServerOutputParsingJsonParameters;
import com.azure.search.documents.indexes.models.McpServerOutputParsingSplitParameters;
import com.azure.search.documents.indexes.models.McpServerSplitOutputParsing;
import com.azure.search.documents.indexes.models.McpServerStoredHeadersAuthentication;
import com.azure.search.documents.indexes.models.McpServerStoredHeadersParameters;
import com.azure.search.documents.indexes.models.McpServerTool;
import com.azure.search.documents.indexes.models.McpServerToolInclusionMode;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexFieldReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.indexes.models.TextSplitMode;
import com.azure.search.documents.indexes.models.WebKnowledgeSource;
import com.azure.search.documents.indexes.models.WebKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
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
import java.util.Collections;
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
            // Delete Knowledge Sources created during tests.
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
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void createKnowledgeSourceRemoteSharePointSync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
    }

    @Test
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void createKnowledgeSourceRemoteSharePointAsync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
    }

    @Test
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void createKnowledgeSourceRemoteSharePointCustomParametersSync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
    }

    @Test
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void createKnowledgeSourceRemoteSharePointCustomParametersAsync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
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
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void getKnowledgeSourceRemoteSharePointSync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
    }

    @Test
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void getKnowledgeSourceRemoteSharePointAsync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
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
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void updateKnowledgeSourceRemoteSharePointSync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
    }

    @Test
    @Disabled("RemoteSharePointKnowledgeSource removed in 2026-04-01 API version")
    public void updateKnowledgeSourceRemoteSharePointAsync() {
        // Disabled: RemoteSharePointKnowledgeSource was removed from the 2026-04-01 API version.
    }

    @Test
    public void statusPayloadMapsToModelsWithNullables() throws IOException {
        // Sample status payload with nullables for first sync
        String statusJson = "{\"synchronizationStatus\": \"creating\",\"synchronizationInterval\": \"PT24H\","
            + "\"currentSynchronizationState\": null,\"lastSynchronizationState\": null,\"statistics\": {"
            + "\"totalSynchronization\": 0,\"averageSynchronizationDuration\": \"PT0S\","
            + "\"averageItemsProcessedPerSynchronization\": 0}}";

        try (JsonReader reader = JsonProviders.createReader(statusJson)) {
            KnowledgeSourceStatus status = KnowledgeSourceStatus.fromJson(reader);

            assertNotNull(status);
            assertEquals(KnowledgeSourceSynchronizationStatus.CREATING, status.getSynchronizationStatus());

            if (status.getSynchronizationInterval() != null) {
                assertEquals(Duration.ofHours(24), status.getSynchronizationInterval());
            }

            assertNull(status.getCurrentSynchronizationState());
            assertNull(status.getLastSynchronizationState());

            // Statistics object exists with actual available fields
            assertNotNull(status.getStatistics());
            assertEquals(0, status.getStatistics().getTotalSynchronization());
            assertEquals(Duration.ZERO, status.getStatistics().getAverageSynchronizationDuration());
            assertEquals(0, status.getStatistics().getAverageItemsProcessedPerSynchronization());
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
    public void createFabricOntologyKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), created.getName());
        FabricOntologyKnowledgeSource createdSource = assertInstanceOf(FabricOntologyKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.FABRIC_ONTOLOGY, createdSource.getKind());
        if (interceptorManager.isLiveMode()) {
            assertEquals(FABRIC_WORKSPACE_ID, createdSource.getFabricOntologyParameters().getWorkspaceId());
            assertEquals(FABRIC_ONTOLOGY_ID, createdSource.getFabricOntologyParameters().getOntologyId());
        }
    }

    @Test
    public void createFabricOntologyKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            assertEquals(knowledgeSource.getName(), created.getName());
            FabricOntologyKnowledgeSource createdSource
                = assertInstanceOf(FabricOntologyKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.FABRIC_ONTOLOGY, createdSource.getKind());
            if (interceptorManager.isLiveMode()) {
                assertEquals(FABRIC_WORKSPACE_ID, createdSource.getFabricOntologyParameters().getWorkspaceId());
                assertEquals(FABRIC_ONTOLOGY_ID, createdSource.getFabricOntologyParameters().getOntologyId());
            }
        }).verifyComplete();
    }

    @Test
    public void getFabricOntologyKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params)
                .setDescription("Fabric Ontology for testing");

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertEquals(knowledgeSource.getName(), retrieved.getName());

        FabricOntologyKnowledgeSource retrievedSource
            = assertInstanceOf(FabricOntologyKnowledgeSource.class, retrieved);
        assertEquals("Fabric Ontology for testing", retrievedSource.getDescription());
        if (interceptorManager.isLiveMode()) {
            assertEquals(FABRIC_WORKSPACE_ID, retrievedSource.getFabricOntologyParameters().getWorkspaceId());
            assertEquals(FABRIC_ONTOLOGY_ID, retrievedSource.getFabricOntologyParameters().getOntologyId());
        }
    }

    @Test
    public void getFabricOntologyKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params)
                .setDescription("Fabric Ontology for testing");

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono).assertNext(retrieved -> {
            assertEquals(knowledgeSource.getName(), retrieved.getName());

            FabricOntologyKnowledgeSource retrievedSource
                = assertInstanceOf(FabricOntologyKnowledgeSource.class, retrieved);
            assertEquals("Fabric Ontology for testing", retrievedSource.getDescription());
            if (interceptorManager.isLiveMode()) {
                assertEquals(FABRIC_WORKSPACE_ID, retrievedSource.getFabricOntologyParameters().getWorkspaceId());
                assertEquals(FABRIC_ONTOLOGY_ID, retrievedSource.getFabricOntologyParameters().getOntologyId());
            }
        }).verifyComplete();
    }

    @Test
    public void updateFabricOntologyKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        knowledgeSource.setDescription("Updated Fabric Ontology description");
        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);

        assertEquals("Updated Fabric Ontology description", updated.getDescription());
        FabricOntologyKnowledgeSource updatedSource = assertInstanceOf(FabricOntologyKnowledgeSource.class, updated);
        if (interceptorManager.isLiveMode()) {
            assertEquals(FABRIC_WORKSPACE_ID, updatedSource.getFabricOntologyParameters().getWorkspaceId());
        }
    }

    @Test
    public void updateFabricOntologyKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createUpdateAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient
                .createOrUpdateKnowledgeSource(created.setDescription("Updated Fabric Ontology description")))
            .flatMap(updated -> searchIndexClient.getKnowledgeSource(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono).assertNext(retrieved -> {
            assertEquals("Updated Fabric Ontology description", retrieved.getDescription());
            FabricOntologyKnowledgeSource retrievedSource
                = assertInstanceOf(FabricOntologyKnowledgeSource.class, retrieved);
            if (interceptorManager.isLiveMode()) {
                assertEquals(FABRIC_WORKSPACE_ID, retrievedSource.getFabricOntologyParameters().getWorkspaceId());
            }
        }).verifyComplete();
    }

    @Test
    public void deleteFabricOntologyKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertNotNull(retrieved);

        searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> searchIndexClient.getKnowledgeSource(knowledgeSource.getName()));
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteFabricOntologyKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

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
    public void listFabricOntologyKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        long initialCount = searchIndexClient.listKnowledgeSources().stream().count();

        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID);
        FabricOntologyKnowledgeSource knowledgeSource
            = new FabricOntologyKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        Map<String, KnowledgeSource> knowledgeSourcesByName = searchIndexClient.listKnowledgeSources()
            .stream()
            .collect(Collectors.toMap(KnowledgeSource::getName, Function.identity()));

        assertEquals(initialCount + 1, knowledgeSourcesByName.size());
        assertTrue(knowledgeSourcesByName.containsKey(knowledgeSource.getName()));

        KnowledgeSource listed = knowledgeSourcesByName.get(knowledgeSource.getName());
        FabricOntologyKnowledgeSource listedSource = assertInstanceOf(FabricOntologyKnowledgeSource.class, listed);
        assertEquals(KnowledgeSourceKind.FABRIC_ONTOLOGY, listedSource.getKind());
    }

    @Test
    public void fabricOntologyKnowledgeSourceSerializationRoundTrip() throws IOException {
        FabricOntologyKnowledgeSourceParameters params
            = new FabricOntologyKnowledgeSourceParameters("workspace-id-123", "ontology-id-456");
        FabricOntologyKnowledgeSource original
            = new FabricOntologyKnowledgeSource("test-fabric-ks", params).setDescription("Serialization test");

        // Serialize
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            original.toJson(writer);
        }
        String json = outputStream.toString();

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"fabricOntology\""), "JSON should contain kind 'fabricOntology'");
        assertTrue(json.contains("\"workspace-id-123\""), "JSON should contain workspaceId");
        assertTrue(json.contains("\"ontology-id-456\""), "JSON should contain ontologyId");
        assertTrue(json.contains("\"test-fabric-ks\""), "JSON should contain name");

        // Deserialize
        try (JsonReader reader = JsonProviders.createReader(json)) {
            KnowledgeSource deserialized = KnowledgeSource.fromJson(reader);

            FabricOntologyKnowledgeSource deserializedSource
                = assertInstanceOf(FabricOntologyKnowledgeSource.class, deserialized);
            assertEquals("test-fabric-ks", deserializedSource.getName());
            assertEquals("Serialization test", deserializedSource.getDescription());
            assertEquals(KnowledgeSourceKind.FABRIC_ONTOLOGY, deserializedSource.getKind());
            assertEquals("workspace-id-123", deserializedSource.getFabricOntologyParameters().getWorkspaceId());
            assertEquals("ontology-id-456", deserializedSource.getFabricOntologyParameters().getOntologyId());
        }
    }

    @Test
    public void createFabricDataAgentKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), created.getName());
        FabricDataAgentKnowledgeSource createdSource = assertInstanceOf(FabricDataAgentKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.FABRIC_DATA_AGENT, createdSource.getKind());
        if (interceptorManager.isLiveMode()) {
            assertEquals(FABRIC_WORKSPACE_ID, createdSource.getFabricDataAgentParameters().getWorkspaceId());
            assertEquals(FABRIC_DATA_AGENT_ID, createdSource.getFabricDataAgentParameters().getDataAgentId());
        }
    }

    @Test
    public void createFabricDataAgentKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            assertEquals(knowledgeSource.getName(), created.getName());
            FabricDataAgentKnowledgeSource createdSource
                = assertInstanceOf(FabricDataAgentKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.FABRIC_DATA_AGENT, createdSource.getKind());
            if (interceptorManager.isLiveMode()) {
                assertEquals(FABRIC_WORKSPACE_ID, createdSource.getFabricDataAgentParameters().getWorkspaceId());
                assertEquals(FABRIC_DATA_AGENT_ID, createdSource.getFabricDataAgentParameters().getDataAgentId());
            }
        }).verifyComplete();
    }

    @Test
    public void getFabricDataAgentKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params)
                .setDescription("Fabric Data Agent for testing");

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertEquals(knowledgeSource.getName(), retrieved.getName());

        FabricDataAgentKnowledgeSource retrievedSource
            = assertInstanceOf(FabricDataAgentKnowledgeSource.class, retrieved);
        assertEquals("Fabric Data Agent for testing", retrievedSource.getDescription());
        if (interceptorManager.isLiveMode()) {
            assertEquals(FABRIC_WORKSPACE_ID, retrievedSource.getFabricDataAgentParameters().getWorkspaceId());
            assertEquals(FABRIC_DATA_AGENT_ID, retrievedSource.getFabricDataAgentParameters().getDataAgentId());
        }
    }

    @Test
    public void getFabricDataAgentKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params)
                .setDescription("Fabric Data Agent for testing");

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono).assertNext(retrieved -> {
            assertEquals(knowledgeSource.getName(), retrieved.getName());

            FabricDataAgentKnowledgeSource retrievedSource
                = assertInstanceOf(FabricDataAgentKnowledgeSource.class, retrieved);
            assertEquals("Fabric Data Agent for testing", retrievedSource.getDescription());
            if (interceptorManager.isLiveMode()) {
                assertEquals(FABRIC_WORKSPACE_ID, retrievedSource.getFabricDataAgentParameters().getWorkspaceId());
                assertEquals(FABRIC_DATA_AGENT_ID, retrievedSource.getFabricDataAgentParameters().getDataAgentId());
            }
        }).verifyComplete();
    }

    @Test
    public void updateFabricDataAgentKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        knowledgeSource.setDescription("Updated Fabric Data Agent description");
        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);

        assertEquals("Updated Fabric Data Agent description", updated.getDescription());
        FabricDataAgentKnowledgeSource updatedSource = assertInstanceOf(FabricDataAgentKnowledgeSource.class, updated);
        if (interceptorManager.isLiveMode()) {
            assertEquals(FABRIC_WORKSPACE_ID, updatedSource.getFabricDataAgentParameters().getWorkspaceId());
            assertEquals(FABRIC_DATA_AGENT_ID, updatedSource.getFabricDataAgentParameters().getDataAgentId());
        }
    }

    @Test
    public void updateFabricDataAgentKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createUpdateAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient
                .createOrUpdateKnowledgeSource(created.setDescription("Updated Fabric Data Agent description")))
            .flatMap(updated -> searchIndexClient.getKnowledgeSource(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono).assertNext(retrieved -> {
            assertEquals("Updated Fabric Data Agent description", retrieved.getDescription());
            FabricDataAgentKnowledgeSource retrievedSource
                = assertInstanceOf(FabricDataAgentKnowledgeSource.class, retrieved);
            if (interceptorManager.isLiveMode()) {
                assertEquals(FABRIC_WORKSPACE_ID, retrievedSource.getFabricDataAgentParameters().getWorkspaceId());
                assertEquals(FABRIC_DATA_AGENT_ID, retrievedSource.getFabricDataAgentParameters().getDataAgentId());
            }
        }).verifyComplete();
    }

    @Test
    public void deleteFabricDataAgentKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertNotNull(retrieved);

        searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> searchIndexClient.getKnowledgeSource(knowledgeSource.getName()));
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteFabricDataAgentKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

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
    public void listFabricDataAgentKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        long initialCount = searchIndexClient.listKnowledgeSources().stream().count();

        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID);
        FabricDataAgentKnowledgeSource knowledgeSource
            = new FabricDataAgentKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        Map<String, KnowledgeSource> knowledgeSourcesByName = searchIndexClient.listKnowledgeSources()
            .stream()
            .collect(Collectors.toMap(KnowledgeSource::getName, Function.identity()));

        assertEquals(initialCount + 1, knowledgeSourcesByName.size());
        assertTrue(knowledgeSourcesByName.containsKey(knowledgeSource.getName()));

        KnowledgeSource listed = knowledgeSourcesByName.get(knowledgeSource.getName());
        FabricDataAgentKnowledgeSource listedSource = assertInstanceOf(FabricDataAgentKnowledgeSource.class, listed);
        assertEquals(KnowledgeSourceKind.FABRIC_DATA_AGENT, listedSource.getKind());
    }

    @Test
    public void fabricDataAgentKnowledgeSourceSerializationRoundTrip() throws IOException {
        FabricDataAgentKnowledgeSourceParameters params
            = new FabricDataAgentKnowledgeSourceParameters("workspace-id-789", "agent-id-012");
        FabricDataAgentKnowledgeSource original
            = new FabricDataAgentKnowledgeSource("test-fabric-da-ks", params).setDescription("Serialization test");

        // Serialize
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            original.toJson(writer);
        }
        String json = outputStream.toString();

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"fabricDataAgent\""), "JSON should contain kind 'fabricDataAgent'");
        assertTrue(json.contains("\"workspace-id-789\""), "JSON should contain workspaceId");
        assertTrue(json.contains("\"agent-id-012\""), "JSON should contain dataAgentId");
        assertTrue(json.contains("\"test-fabric-da-ks\""), "JSON should contain name");

        // Deserialize
        try (JsonReader reader = JsonProviders.createReader(json)) {
            KnowledgeSource deserialized = KnowledgeSource.fromJson(reader);

            FabricDataAgentKnowledgeSource deserializedSource
                = assertInstanceOf(FabricDataAgentKnowledgeSource.class, deserialized);
            assertEquals("test-fabric-da-ks", deserializedSource.getName());
            assertEquals("Serialization test", deserializedSource.getDescription());
            assertEquals(KnowledgeSourceKind.FABRIC_DATA_AGENT, deserializedSource.getKind());
            assertEquals("workspace-id-789", deserializedSource.getFabricDataAgentParameters().getWorkspaceId());
            assertEquals("agent-id-012", deserializedSource.getFabricDataAgentParameters().getDataAgentId());
        }
    }

    @Test
    public void createMcpServerKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), created.getName());
        McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.MCP_SERVER, createdSource.getKind());
        assertEquals("https://mcp.contoso.com/sse", createdSource.getMcpServerParameters().getServerURL());
        assertEquals(1, createdSource.getMcpServerParameters().getTools().size());
        assertEquals("search_code", createdSource.getMcpServerParameters().getTools().get(0).getName());
    }

    @Test
    public void createMcpServerKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            assertEquals(knowledgeSource.getName(), created.getName());
            McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.MCP_SERVER, createdSource.getKind());
            assertEquals("https://mcp.contoso.com/sse", createdSource.getMcpServerParameters().getServerURL());
            assertEquals(1, createdSource.getMcpServerParameters().getTools().size());
            assertEquals("search_code", createdSource.getMcpServerParameters().getTools().get(0).getName());
        }).verifyComplete();
    }

    @Test
    public void createMcpServerKnowledgeSourceWithFoundryAuthSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        McpServerFoundryConnectionParameters foundryParams
            = new McpServerFoundryConnectionParameters().setConnectionId("my-foundry-connection");
        McpServerFoundryConnectionAuthentication auth = new McpServerFoundryConnectionAuthentication(foundryParams);

        McpServerTool tool = new McpServerTool().setName("search_code")
            .setOutputParsing(new McpServerJsonOutputParsing(
                new McpServerOutputParsingJsonParameters("$.results[*]").setIncludeContext(true)));

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse", Collections.singletonList(tool))
                .setAuthentication(auth);
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.MCP_SERVER, createdSource.getKind());

        // Verify auth
        McpServerAuthentication createdAuth = createdSource.getMcpServerParameters().getAuthentication();
        assertNotNull(createdAuth);
        McpServerFoundryConnectionAuthentication foundryAuth
            = assertInstanceOf(McpServerFoundryConnectionAuthentication.class, createdAuth);
        assertEquals("my-foundry-connection", foundryAuth.getFoundryConnectionParameters().getConnectionId());

        // Verify tool with JSON output parsing
        McpServerTool createdTool = createdSource.getMcpServerParameters().getTools().get(0);
        assertEquals("search_code", createdTool.getName());
        McpServerJsonOutputParsing jsonParsing
            = assertInstanceOf(McpServerJsonOutputParsing.class, createdTool.getOutputParsing());
        assertEquals("$.results[*]", jsonParsing.getJsonParameters().getDocumentsPath());
        assertEquals(true, jsonParsing.getJsonParameters().isIncludeContext());
    }

    @Test
    public void createMcpServerKnowledgeSourceWithFoundryAuthAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();

        McpServerFoundryConnectionParameters foundryParams
            = new McpServerFoundryConnectionParameters().setConnectionId("my-foundry-connection");
        McpServerFoundryConnectionAuthentication auth = new McpServerFoundryConnectionAuthentication(foundryParams);

        McpServerTool tool = new McpServerTool().setName("search_code")
            .setOutputParsing(new McpServerJsonOutputParsing(
                new McpServerOutputParsingJsonParameters("$.results[*]").setIncludeContext(true)));

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse", Collections.singletonList(tool))
                .setAuthentication(auth);
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.MCP_SERVER, createdSource.getKind());

            McpServerFoundryConnectionAuthentication foundryAuth
                = assertInstanceOf(McpServerFoundryConnectionAuthentication.class,
                    createdSource.getMcpServerParameters().getAuthentication());
            assertEquals("my-foundry-connection", foundryAuth.getFoundryConnectionParameters().getConnectionId());

            McpServerTool createdTool = createdSource.getMcpServerParameters().getTools().get(0);
            McpServerJsonOutputParsing jsonParsing
                = assertInstanceOf(McpServerJsonOutputParsing.class, createdTool.getOutputParsing());
            assertEquals("$.results[*]", jsonParsing.getJsonParameters().getDocumentsPath());
            assertEquals(true, jsonParsing.getJsonParameters().isIncludeContext());
        }).verifyComplete();
    }

    @Test
    public void createMcpServerKnowledgeSourceWithStoredHeadersSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("x-api-key", "my-api-key");
        McpServerStoredHeadersParameters storedHeadersParams = new McpServerStoredHeadersParameters()
            .setHeaders(new McpServerHeaders().setAdditionalProperties(headers));
        McpServerStoredHeadersAuthentication auth = new McpServerStoredHeadersAuthentication(storedHeadersParams);

        McpServerTool tool = new McpServerTool().setName("get_issues")
            .setInclusionMode(McpServerToolInclusionMode.ALWAYS)
            .setMaxOutputTokens(500);

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse", Collections.singletonList(tool))
                .setAuthentication(auth);
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
        assertEquals(KnowledgeSourceKind.MCP_SERVER, createdSource.getKind());

        // Verify stored headers auth (values are write-only/masked on read, so just check type)
        McpServerStoredHeadersAuthentication storedAuth = assertInstanceOf(McpServerStoredHeadersAuthentication.class,
            createdSource.getMcpServerParameters().getAuthentication());
        assertNotNull(storedAuth.getStoredHeadersParameters());

        // Verify tool params
        McpServerTool createdTool = createdSource.getMcpServerParameters().getTools().get(0);
        assertEquals("get_issues", createdTool.getName());
        assertEquals(McpServerToolInclusionMode.ALWAYS, createdTool.getInclusionMode());
        assertEquals(500, createdTool.getMaxOutputTokens());
    }

    @Test
    public void createMcpServerKnowledgeSourceWithStoredHeadersAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();

        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("x-api-key", "my-api-key");
        McpServerStoredHeadersParameters storedHeadersParams = new McpServerStoredHeadersParameters()
            .setHeaders(new McpServerHeaders().setAdditionalProperties(headers));
        McpServerStoredHeadersAuthentication auth = new McpServerStoredHeadersAuthentication(storedHeadersParams);

        McpServerTool tool = new McpServerTool().setName("get_issues")
            .setInclusionMode(McpServerToolInclusionMode.ALWAYS)
            .setMaxOutputTokens(500);

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse", Collections.singletonList(tool))
                .setAuthentication(auth);
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
            assertEquals(KnowledgeSourceKind.MCP_SERVER, createdSource.getKind());

            McpServerStoredHeadersAuthentication storedAuth = assertInstanceOf(
                McpServerStoredHeadersAuthentication.class, createdSource.getMcpServerParameters().getAuthentication());
            assertNotNull(storedAuth.getStoredHeadersParameters());

            McpServerTool createdTool = createdSource.getMcpServerParameters().getTools().get(0);
            assertEquals("get_issues", createdTool.getName());
            assertEquals(McpServerToolInclusionMode.ALWAYS, createdTool.getInclusionMode());
            assertEquals(500, createdTool.getMaxOutputTokens());
        }).verifyComplete();
    }

    @Test
    public void createMcpServerKnowledgeSourceWithSplitOutputParsingSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        McpServerOutputParsingSplitParameters splitParams
            = new McpServerOutputParsingSplitParameters().setTextSplitMode(TextSplitMode.PAGES)
                .setMaximumPageLength(2000)
                .setPageOverlapLength(200)
                .setMaximumPagesToTake(50);

        McpServerTool tool = new McpServerTool().setName("get_docs")
            .setOutputParsing(new McpServerSplitOutputParsing().setSplitParameters(splitParams));

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse", Collections.singletonList(tool));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
        McpServerTool createdTool = createdSource.getMcpServerParameters().getTools().get(0);
        assertEquals("get_docs", createdTool.getName());

        McpServerSplitOutputParsing splitParsing
            = assertInstanceOf(McpServerSplitOutputParsing.class, createdTool.getOutputParsing());
        assertNotNull(splitParsing.getSplitParameters());
        assertEquals(TextSplitMode.PAGES, splitParsing.getSplitParameters().getTextSplitMode());
        assertEquals(2000, splitParsing.getSplitParameters().getMaximumPageLength());
        assertEquals(200, splitParsing.getSplitParameters().getPageOverlapLength());
        assertEquals(50, splitParsing.getSplitParameters().getMaximumPagesToTake());
    }

    @Test
    public void createMcpServerKnowledgeSourceWithSplitOutputParsingAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();

        McpServerOutputParsingSplitParameters splitParams
            = new McpServerOutputParsingSplitParameters().setTextSplitMode(TextSplitMode.PAGES)
                .setMaximumPageLength(2000)
                .setPageOverlapLength(200)
                .setMaximumPagesToTake(50);

        McpServerTool tool = new McpServerTool().setName("get_docs")
            .setOutputParsing(new McpServerSplitOutputParsing().setSplitParameters(splitParams));

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse", Collections.singletonList(tool));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
            McpServerTool createdTool = createdSource.getMcpServerParameters().getTools().get(0);
            assertEquals("get_docs", createdTool.getName());

            McpServerSplitOutputParsing splitParsing
                = assertInstanceOf(McpServerSplitOutputParsing.class, createdTool.getOutputParsing());
            assertNotNull(splitParsing.getSplitParameters());
            assertEquals(TextSplitMode.PAGES, splitParsing.getSplitParameters().getTextSplitMode());
            assertEquals(2000, splitParsing.getSplitParameters().getMaximumPageLength());
            assertEquals(200, splitParsing.getSplitParameters().getPageOverlapLength());
            assertEquals(50, splitParsing.getSplitParameters().getMaximumPagesToTake());
        }).verifyComplete();
    }

    @Test
    public void createMcpServerKnowledgeSourceWithMultipleToolsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        McpServerTool tool1 = new McpServerTool().setName("search_code")
            .setOutputParsing(new McpServerJsonOutputParsing(new McpServerOutputParsingJsonParameters("$.results[*]")))
            .setInclusionMode(McpServerToolInclusionMode.RERANKED);

        McpServerTool tool2 = new McpServerTool().setName("get_issues")
            .setOutputParsing(new McpServerNoneOutputParsing())
            .setInclusionMode(McpServerToolInclusionMode.ALWAYS)
            .setMaxOutputTokens(1000);

        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", java.util.Arrays.asList(tool1, tool2));
        McpServerKnowledgeSource knowledgeSource
            = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params).setDescription("Multi-tool MCP source");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);

        McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
        assertEquals("Multi-tool MCP source", createdSource.getDescription());
        assertEquals(2, createdSource.getMcpServerParameters().getTools().size());

        Map<String, McpServerTool> toolMap = createdSource.getMcpServerParameters()
            .getTools()
            .stream()
            .collect(Collectors.toMap(McpServerTool::getName, Function.identity()));

        McpServerTool createdTool1 = toolMap.get("search_code");
        assertNotNull(createdTool1);
        assertInstanceOf(McpServerJsonOutputParsing.class, createdTool1.getOutputParsing());
        assertEquals(McpServerToolInclusionMode.RERANKED, createdTool1.getInclusionMode());

        McpServerTool createdTool2 = toolMap.get("get_issues");
        assertNotNull(createdTool2);
        assertInstanceOf(McpServerNoneOutputParsing.class, createdTool2.getOutputParsing());
        assertEquals(McpServerToolInclusionMode.ALWAYS, createdTool2.getInclusionMode());
        assertEquals(1000, createdTool2.getMaxOutputTokens());
    }

    @Test
    public void createMcpServerKnowledgeSourceWithMultipleToolsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();

        McpServerTool tool1 = new McpServerTool().setName("search_code")
            .setOutputParsing(new McpServerJsonOutputParsing(new McpServerOutputParsingJsonParameters("$.results[*]")))
            .setInclusionMode(McpServerToolInclusionMode.RERANKED);

        McpServerTool tool2 = new McpServerTool().setName("get_issues")
            .setOutputParsing(new McpServerNoneOutputParsing())
            .setInclusionMode(McpServerToolInclusionMode.ALWAYS)
            .setMaxOutputTokens(1000);

        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", java.util.Arrays.asList(tool1, tool2));
        McpServerKnowledgeSource knowledgeSource
            = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params).setDescription("Multi-tool MCP source");

        StepVerifier.create(searchIndexClient.createKnowledgeSource(knowledgeSource)).assertNext(created -> {
            McpServerKnowledgeSource createdSource = assertInstanceOf(McpServerKnowledgeSource.class, created);
            assertEquals("Multi-tool MCP source", createdSource.getDescription());
            assertEquals(2, createdSource.getMcpServerParameters().getTools().size());

            Map<String, McpServerTool> toolMap = createdSource.getMcpServerParameters()
                .getTools()
                .stream()
                .collect(Collectors.toMap(McpServerTool::getName, Function.identity()));

            assertNotNull(toolMap.get("search_code"));
            assertInstanceOf(McpServerJsonOutputParsing.class, toolMap.get("search_code").getOutputParsing());

            McpServerTool createdTool2 = toolMap.get("get_issues");
            assertNotNull(createdTool2);
            assertInstanceOf(McpServerNoneOutputParsing.class, createdTool2.getOutputParsing());
            assertEquals(McpServerToolInclusionMode.ALWAYS, createdTool2.getInclusionMode());
            assertEquals(1000, createdTool2.getMaxOutputTokens());
        }).verifyComplete();
    }

    @Test
    public void getMcpServerKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        McpServerKnowledgeSource retrievedSource = assertInstanceOf(McpServerKnowledgeSource.class, retrieved);
        assertEquals(KnowledgeSourceKind.MCP_SERVER, retrievedSource.getKind());
        assertEquals("https://mcp.contoso.com/sse", retrievedSource.getMcpServerParameters().getServerURL());
        assertEquals("search_code", retrievedSource.getMcpServerParameters().getTools().get(0).getName());
    }

    @Test
    public void getMcpServerKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient.getKnowledgeSource(created.getName()));

        StepVerifier.create(createAndGetMono).assertNext(retrieved -> {
            McpServerKnowledgeSource retrievedSource = assertInstanceOf(McpServerKnowledgeSource.class, retrieved);
            assertEquals(KnowledgeSourceKind.MCP_SERVER, retrievedSource.getKind());
            assertEquals("https://mcp.contoso.com/sse", retrievedSource.getMcpServerParameters().getServerURL());
            assertEquals("search_code", retrievedSource.getMcpServerParameters().getTools().get(0).getName());
        }).verifyComplete();
    }

    @Test
    public void updateMcpServerKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        knowledgeSource.setDescription("Updated MCP description");
        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);

        assertEquals("Updated MCP description", updated.getDescription());
        McpServerKnowledgeSource updatedSource = assertInstanceOf(McpServerKnowledgeSource.class, updated);
        assertEquals("https://mcp.contoso.com/sse", updatedSource.getMcpServerParameters().getServerURL());
    }

    @Test
    public void updateMcpServerKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        Mono<KnowledgeSource> createUpdateAndGetMono = searchIndexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> searchIndexClient
                .createOrUpdateKnowledgeSource(created.setDescription("Updated MCP description")))
            .flatMap(updated -> searchIndexClient.getKnowledgeSource(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono).assertNext(retrieved -> {
            assertEquals("Updated MCP description", retrieved.getDescription());
            McpServerKnowledgeSource retrievedSource = assertInstanceOf(McpServerKnowledgeSource.class, retrieved);
            assertEquals("https://mcp.contoso.com/sse", retrievedSource.getMcpServerParameters().getServerURL());
        }).verifyComplete();
    }

    @Test
    public void deleteMcpServerKnowledgeSourceSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

        searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSource.getName());
        assertNotNull(retrieved);

        searchIndexClient.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> searchIndexClient.getKnowledgeSource(knowledgeSource.getName()));
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteMcpServerKnowledgeSourceAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        McpServerKnowledgeSourceParameters params = new McpServerKnowledgeSourceParameters(
            "https://mcp.contoso.com/sse", Collections.singletonList(new McpServerTool().setName("search_code")));
        McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(randomKnowledgeSourceName(), params);

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
    public void mcpServerKnowledgeSourceSerializationRoundTrip() throws IOException {
        McpServerFoundryConnectionParameters foundryParams
            = new McpServerFoundryConnectionParameters().setConnectionId("my-foundry-connection");
        McpServerFoundryConnectionAuthentication auth = new McpServerFoundryConnectionAuthentication(foundryParams);

        McpServerTool tool1 = new McpServerTool().setName("search_code")
            .setOutputParsing(new McpServerJsonOutputParsing(
                new McpServerOutputParsingJsonParameters("$.results[*]").setIncludeContext(true)))
            .setInclusionMode(McpServerToolInclusionMode.RERANKED);

        McpServerTool tool2 = new McpServerTool().setName("get_issues")
            .setOutputParsing(new McpServerNoneOutputParsing())
            .setInclusionMode(McpServerToolInclusionMode.ALWAYS)
            .setMaxOutputTokens(500);

        McpServerKnowledgeSourceParameters params
            = new McpServerKnowledgeSourceParameters("https://mcp.contoso.com/sse",
                java.util.Arrays.asList(tool1, tool2)).setAuthentication(auth);
        McpServerKnowledgeSource original
            = new McpServerKnowledgeSource("test-mcp-ks", params).setDescription("Serialization test");

        // Serialize
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            original.toJson(writer);
        }
        String json = outputStream.toString();

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"mcpServer\""), "JSON should contain kind 'mcpServer'");
        assertTrue(json.contains("\"https://mcp.contoso.com/sse\""), "JSON should contain serverURL");
        assertTrue(json.contains("\"search_code\""), "JSON should contain tool name");
        assertTrue(json.contains("\"get_issues\""), "JSON should contain second tool name");
        assertTrue(json.contains("\"my-foundry-connection\""), "JSON should contain connectionId");
        assertTrue(json.contains("\"$.results[*]\""), "JSON should contain documentsPath");

        // Deserialize
        try (JsonReader reader = JsonProviders.createReader(json)) {
            KnowledgeSource deserialized = KnowledgeSource.fromJson(reader);

            McpServerKnowledgeSource deserializedSource
                = assertInstanceOf(McpServerKnowledgeSource.class, deserialized);
            assertEquals("test-mcp-ks", deserializedSource.getName());
            assertEquals("Serialization test", deserializedSource.getDescription());
            assertEquals(KnowledgeSourceKind.MCP_SERVER, deserializedSource.getKind());
            assertEquals("https://mcp.contoso.com/sse", deserializedSource.getMcpServerParameters().getServerURL());
            assertEquals(2, deserializedSource.getMcpServerParameters().getTools().size());

            // Verify auth round-trips
            McpServerFoundryConnectionAuthentication deserializedAuth
                = assertInstanceOf(McpServerFoundryConnectionAuthentication.class,
                    deserializedSource.getMcpServerParameters().getAuthentication());
            assertEquals("my-foundry-connection", deserializedAuth.getFoundryConnectionParameters().getConnectionId());

            // Verify tools round-trip
            McpServerTool deserializedTool1 = deserializedSource.getMcpServerParameters().getTools().get(0);
            assertEquals("search_code", deserializedTool1.getName());
            McpServerJsonOutputParsing jsonParsing
                = assertInstanceOf(McpServerJsonOutputParsing.class, deserializedTool1.getOutputParsing());
            assertEquals("$.results[*]", jsonParsing.getJsonParameters().getDocumentsPath());
            assertEquals(true, jsonParsing.getJsonParameters().isIncludeContext());

            McpServerTool deserializedTool2 = deserializedSource.getMcpServerParameters().getTools().get(1);
            assertEquals("get_issues", deserializedTool2.getName());
            assertInstanceOf(McpServerNoneOutputParsing.class, deserializedTool2.getOutputParsing());
            assertEquals(McpServerToolInclusionMode.ALWAYS, deserializedTool2.getInclusionMode());
            assertEquals(500, deserializedTool2.getMaxOutputTokens());
        }
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
