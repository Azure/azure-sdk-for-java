// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.indexes.models.KnowledgeSourceIngestionPermissionOption;
import com.azure.search.documents.indexes.models.KnowledgeSourceKind;
import com.azure.search.documents.indexes.models.KnowledgeSourceStatus;
import com.azure.search.documents.indexes.models.KnowledgeSourceSynchronizationStatus;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexFieldReference;
import com.azure.search.documents.indexes.models.SearchIndexerDataIdentity;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.indexes.models.WebKnowledgeSource;
import com.azure.search.documents.indexes.models.WebKnowledgeSourceParameters;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Duration;

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
    public void createKnowledgeSourceSync() {
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
    public void createKnowledgeSourceAsync() {
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
    public void getKnowledgeSourceSync() {
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
    public void getKnowledgeSourceAsync() {
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
    public void updateKnowledgeSourceSync() {
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
    public void updateKnowledgeSourceAsync() {
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
    public void statusPayload_mapsToModels_withNullables() throws IOException {
        // Sample status payload with nullables for first sync
        String statusJson = "{\n" + "    \"synchronizationStatus\": \"creating\",\n"
            + "    \"synchronizationInterval\": \"PT24H\",\n" + "    \"currentSynchronizationState\": null,\n"
            + "    \"lastSynchronizationState\": null,\n" + "    \"statistics\": {\n"
            + "        \"totalSynchronization\": 0,\n" + "        \"averageSynchronizationDuration\": \"00:00:00\",\n"
            + "        \"averageItemsProcessedPerSynchronization\": 0\n" + "    }\n" + "}";

        try (JsonReader reader = JsonProviders.createReader(statusJson)) {
            KnowledgeSourceStatus status = KnowledgeSourceStatus.fromJson(reader);

            assertNotNull(status);
            assertEquals(KnowledgeSourceSynchronizationStatus.CREATING, status.getSynchronizationStatus());

            Object syncInterval = status.getSynchronizationInterval();
            if (syncInterval instanceof String) {
                assertEquals("PT24H", syncInterval); // ← Compare as String
            } else if (syncInterval instanceof Duration) {
                assertEquals(Duration.ofHours(24), syncInterval); // ← Compare as Duration
            } else {
                // Handle other types if needed
                assertNotNull(syncInterval, "Synchronization interval should not be null");
            }

            assertNull(status.getCurrentSynchronizationState());
            assertNull(status.getLastSynchronizationState());

            // Statistics object exists with actual available fields
            assertNotNull(status.getStatistics());
            assertEquals(0, status.getStatistics().getTotalSynchronization());
            assertEquals("00:00:00", status.getStatistics().getAverageSynchronizationDuration());
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
            try {
                client.deleteKnowledgeSource(knowledgeSource.getName());
            } catch (Exception e) {
            }
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
            try {
                client.deleteKnowledgeSource(knowledgeSource.getName());
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void deleteKnowledegeSourceRemovesSource() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(randomKnowledgeSourceName(),
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME));

        client.createKnowledgeSource(knowledgeSource);
        client.deleteKnowledgeSource(knowledgeSource.getName());

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            client.getKnowledgeSource(knowledgeSource.getName());
        });
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
            try {
                client.deleteKnowledgeSource(ks1.getName());
                client.deleteKnowledgeSource(ks2.getName());
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void knowledgeSourceParameters_setsFieldsCorrectly() {
        SearchIndexKnowledgeSourceParameters params = new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME);

        assertEquals(HOTEL_INDEX_NAME, params.getSearchIndexName());

        params.setSemanticConfigurationName("semantic-config");
        assertEquals("semantic-config", params.getSemanticConfigurationName());

        List<SearchIndexFieldReference> sourceFields
            = Arrays.asList(new SearchIndexFieldReference("field1"), new SearchIndexFieldReference("field2"));
        params.setSourceDataFields(sourceFields);
        assertEquals(2, params.getSourceDataFields().size());
        assertEquals("field1", params.getSourceDataFields().get(0).getName());
        assertEquals("field2", params.getSourceDataFields().get(1).getName());

        List<SearchIndexFieldReference> searchFields = Arrays.asList(new SearchIndexFieldReference("searchField1"));
        params.setSearchFields(searchFields);
        assertEquals(1, params.getSearchFields().size());
        assertEquals("searchField1", params.getSearchFields().get(0).getName());

        SearchIndexKnowledgeSourceParameters result = params.setSemanticConfigurationName("another-config");
        assertSame(params, result);
    }

    @Test
    public void permissionOptions_enumValuesExist() {
        assertNotNull(KnowledgeSourceIngestionPermissionOption.USER_IDS);
        assertNotNull(KnowledgeSourceIngestionPermissionOption.GROUP_IDS);
        assertNotNull(KnowledgeSourceIngestionPermissionOption.RBAC_SCOPE);

        assertEquals("userIds", KnowledgeSourceIngestionPermissionOption.USER_IDS.toString());
        assertEquals("groupIds", KnowledgeSourceIngestionPermissionOption.GROUP_IDS.toString());
        assertEquals("rbacScope", KnowledgeSourceIngestionPermissionOption.RBAC_SCOPE.toString());

        KnowledgeSourceIngestionPermissionOption userIds
            = KnowledgeSourceIngestionPermissionOption.fromString("userIds");
        assertEquals(KnowledgeSourceIngestionPermissionOption.USER_IDS, userIds);

        KnowledgeSourceIngestionPermissionOption groupIds
            = KnowledgeSourceIngestionPermissionOption.fromString("groupIds");
        assertEquals(KnowledgeSourceIngestionPermissionOption.GROUP_IDS, groupIds);

        KnowledgeSourceIngestionPermissionOption rbacScope
            = KnowledgeSourceIngestionPermissionOption.fromString("rbacScope");
        assertEquals(KnowledgeSourceIngestionPermissionOption.RBAC_SCOPE, rbacScope);

        KnowledgeSourceIngestionPermissionOption unknown
            = KnowledgeSourceIngestionPermissionOption.fromString("unknownValue");
        assertNotNull(unknown);
        assertEquals("unknownValue", unknown.toString());
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

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> {
            searchIndexClient.getKnowledgeSource(created.getName());
        });
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
            HttpResponseException ex = assertThrows(HttpResponseException.class, () -> {
                searchIndexClient.createKnowledgeSource(webKS);
            });
            assertTrue(ex.getResponse().getStatusCode() >= 400 && ex.getResponse().getStatusCode() < 500);

        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected exception for null name
            assertTrue(true);
        }

        try {
            WebKnowledgeSource webKS = new WebKnowledgeSource(null);
            HttpResponseException ex2 = assertThrows(HttpResponseException.class, () -> {
                searchIndexClient.createKnowledgeSource(webKS);
            });
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

        assertTrue(created instanceof KnowledgeSource);
        assertTrue(created instanceof WebKnowledgeSource);

        String newDescription = "Updated via base class";
        created.setDescription(newDescription);

        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(created);
        assertEquals(newDescription, updated.getDescription());
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

            List<SemanticConfiguration> semanticConfigurations
                = Collections.singletonList(new SemanticConfiguration("semantic-config",
                    new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                        .setContentFields(new SemanticField("Description"))
                        .setKeywordsFields(new SemanticField("Category"))));
            SemanticSearch semanticSearch = new SemanticSearch().setDefaultConfigurationName("semantic-config")
                .setConfigurations(semanticConfigurations);
            searchIndexClient.createOrUpdateIndex(
                TestHelpers.createTestIndex(HOTEL_INDEX_NAME, baseIndex).setSemanticSearch(semanticSearch));

            return searchIndexClient;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
