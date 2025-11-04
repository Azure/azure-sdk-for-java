package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.IndexedOneLakeKnowledgeSource;
import com.azure.search.documents.indexes.models.IndexedOneLakeKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceKind;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KnowledgeSourceOneLakeTests extends SearchTestBase {
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
    public void canCreateKnowledgeSourceOneLakeSync() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("create-sync");
        KnowledgeSource createdKnowledgeSource = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(knowledgeSource.getName(), createdKnowledgeSource.getName());
        assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, createdKnowledgeSource.getKind());

        IndexedOneLakeKnowledgeSource createdOneLake
            = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, createdKnowledgeSource);
        IndexedOneLakeKnowledgeSourceParameters params = createdOneLake.getIndexedOneLakeParameters();
        assertNotNull(params);
        assertEquals("test-workspace-id", params.getFabricWorkspaceId());
        assertEquals("test-lakehouse-id", params.getLakehouseId());
        assertEquals("/test/path", params.getTargetPath());

    }

    @Test
    public void canCreateKnowledgeSourceOneLakeAsync() {
        SearchIndexAsyncClient asyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("create-async");

        StepVerifier.create(asyncClient.createKnowledgeSource(knowledgeSource)).assertNext(createdKnowledgeSource -> {
            assertEquals(knowledgeSource.getName(), createdKnowledgeSource.getName());
            assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, createdKnowledgeSource.getKind());

            IndexedOneLakeKnowledgeSource createdOneLake
                = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, createdKnowledgeSource);
            IndexedOneLakeKnowledgeSourceParameters params = createdOneLake.getIndexedOneLakeParameters();
            assertNotNull(params);
            assertEquals("test-workspace-id", params.getFabricWorkspaceId());
            assertEquals("test-lakehouse-id", params.getLakehouseId());
            assertEquals("/test/path", params.getTargetPath());
        }).verifyComplete();
    }

    @Test
    public void canReadKnowledgeSourceOneLakeSync() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("read-sync");
        KnowledgeSource createdKnowledgeSource = searchIndexClient.createKnowledgeSource(knowledgeSource);

        KnowledgeSource retrievedKnowledgeSource
            = searchIndexClient.getKnowledgeSource(createdKnowledgeSource.getName());

        assertEquals(createdKnowledgeSource.getName(), retrievedKnowledgeSource.getName());
        assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, retrievedKnowledgeSource.getKind());

        IndexedOneLakeKnowledgeSource retrievedOneLake
            = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, retrievedKnowledgeSource);
        IndexedOneLakeKnowledgeSourceParameters params = retrievedOneLake.getIndexedOneLakeParameters();
        assertEquals("test-workspace-id", params.getFabricWorkspaceId());
        assertEquals("test-lakehouse-id", params.getLakehouseId());
        assertEquals("/test/path", params.getTargetPath());
    }

    @Test
    public void canReadKnowledgeSourceOneLakeAsync() {
        SearchIndexAsyncClient asyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("read-async");

        Mono<Tuple2<KnowledgeSource, KnowledgeSource>> createAndRetrieveMono
            = asyncClient.createKnowledgeSource(knowledgeSource)
                .flatMap(created -> asyncClient.getKnowledgeSource(created.getName())
                    .map(retrieved -> Tuples.of(created, retrieved)));

        StepVerifier.create(createAndRetrieveMono).assertNext(sources -> {
            KnowledgeSource created = sources.getT1();
            KnowledgeSource retrieved = sources.getT2();

            assertEquals(created.getName(), retrieved.getName());
            assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, retrieved.getKind());

            IndexedOneLakeKnowledgeSource retrievedOneLake
                = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, retrieved);
            assertEquals("test-workspace-id", retrievedOneLake.getIndexedOneLakeParameters().getFabricWorkspaceId());
        }).verifyComplete();
    }

    @Test
    public void canUpdateKnowledgeSourceOneLakeSync() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("update-sync");
        KnowledgeSource createdKnowledgeSource = searchIndexClient.createKnowledgeSource(knowledgeSource);
        IndexedOneLakeKnowledgeSource createdOneLake
            = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, createdKnowledgeSource);

        IndexedOneLakeKnowledgeSourceParameters updatedParams
            = new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", "test-lakehouse-id")
                .setTargetPath("/updated/path");

        IndexedOneLakeKnowledgeSource updatedSource
            = new IndexedOneLakeKnowledgeSource(createdOneLake.getName(), updatedParams);

        KnowledgeSource updatedKnowledgeSource = searchIndexClient.createOrUpdateKnowledgeSource(updatedSource);

        IndexedOneLakeKnowledgeSource updatedOneLake
            = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, updatedKnowledgeSource);
        assertEquals("/updated/path", updatedOneLake.getIndexedOneLakeParameters().getTargetPath());
        assertEquals("test-workspace-id", updatedOneLake.getIndexedOneLakeParameters().getFabricWorkspaceId());
    }

    @Test
    public void canUpdateKnowledgeSourceOneLakeAsync() {
        SearchIndexAsyncClient asyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("update-async");

        Mono<KnowledgeSource> updateMono = asyncClient.createKnowledgeSource(knowledgeSource).map(created -> {
            IndexedOneLakeKnowledgeSource createdOneLake
                = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, created);

            IndexedOneLakeKnowledgeSourceParameters updatedParams
                = new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", "test-lakehouse-id")
                    .setTargetPath("/async/updated/path");

            return new IndexedOneLakeKnowledgeSource(createdOneLake.getName(), updatedParams);
        }).flatMap(asyncClient::createOrUpdateKnowledgeSource);

        StepVerifier.create(updateMono).assertNext(updated -> {
            IndexedOneLakeKnowledgeSource updatedOneLake
                = assertInstanceOf(IndexedOneLakeKnowledgeSource.class, updated);
            assertEquals("/async/updated/path", updatedOneLake.getIndexedOneLakeParameters().getTargetPath());
            assertEquals("test-workspace-id", updatedOneLake.getIndexedOneLakeParameters().getFabricWorkspaceId());
        }).verifyComplete();
    }

    @Test
    public void canDeleteOneLakeKnowledgeSourceSync() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("delete-sync");
        KnowledgeSource createdKnowledgeSource = searchIndexClient.createKnowledgeSource(knowledgeSource);
        assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, createdKnowledgeSource.getKind());

        searchIndexClient.deleteKnowledgeSource(createdKnowledgeSource.getName());

        assertThrows(HttpResponseException.class, () -> {
            searchIndexClient.getKnowledgeSource(createdKnowledgeSource.getName());
        });
    }

    @Test
    public void canDeleteOneLakeKnowledgeSourceAsync() {
        SearchIndexAsyncClient asyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("delete-async");

        Mono<Void> deleteMono = asyncClient.createKnowledgeSource(knowledgeSource).flatMap(created -> {
            assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, created.getKind());
            return asyncClient.deleteKnowledgeSource(created.getName());

        });
        StepVerifier.create(deleteMono).verifyComplete();

        StepVerifier.create(asyncClient.getKnowledgeSource(knowledgeSource.getName()))
            .expectError(HttpResponseException.class)
            .verify();
    }

    // ===== REQUIRED PARAMETER VALIDATION TESTS =====
    @Test
    public void createOneLakeKnowledgeSourceWithNullNameThrows() {
        IndexedOneLakeKnowledgeSourceParameters parameters
            = new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", "test-lakehouse-id");

        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSource(null, parameters);
        });
    }

    @Test
    public void createOneLakeKnowledgeSourceWithEmptyNameThrows() {
        IndexedOneLakeKnowledgeSourceParameters parameters
            = new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", "test-lakehouse-id");

        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSource("", parameters);
        });
    }

    @Test
    public void createOneLakeKnowledgeSourceWithNullParametersThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSource(null, null);
        });
    }

    @Test
    public void createOneLakeParametersWithNullFabricWorkspaceIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSourceParameters(null, "test-lakehouse-id");
        });
    }

    @Test
    public void createOneLakeParametersWithEmptyFabricWorkspaceIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSourceParameters("", "test-lakehouse-id");
        });
    }

    @Test
    public void createOneLakeParametersWithNullLakehouseIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", null);
        });
    }

    @Test
    public void createOneLakeParametersWithEmptyLakehouseIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", "");  // Empty lakehouse ID
        });
    }

    @Test
    public void oneLakeKnowledgeSourceWorksWithCurrentApiVersion() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("current-api-test");
        KnowledgeSource createdKnowledgeSource = searchIndexClient.createKnowledgeSource(knowledgeSource);

        assertEquals(KnowledgeSourceKind.INDEXED_ONE_LAKE, createdKnowledgeSource);
        assertNotNull(((IndexedOneLakeKnowledgeSource) createdKnowledgeSource).getIndexedOneLakeParameters());
    }

    @Test
    public void oneLakeKnowledgeSourceFailsWithOlderApiVersion() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("older-api-test");

        SearchIndexClient clientWithOlderApiVersion = getSearchIndexClientBuilder(false)
            .serviceVersion(com.azure.search.documents.SearchServiceVersion.V2025_09_01)
            .buildClient();

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            clientWithOlderApiVersion.createKnowledgeSource(knowledgeSource);
        });

        assertEquals(400, exception.getResponse().getStatusCode());
        assertTrue(exception.getMessage().contains("indexedOneLake")
            || exception.getMessage().contains("unsupported")
            || exception.getMessage().contains("invalid"));
    }

    @Test
    public void oneLakeKnowledgeSourceSerializationTest() {
        IndexedOneLakeKnowledgeSource knowledgeSource = createOneLakeKnowledgeSource("serialization-test");

        // Test serialization to JSON
        String json = BinaryData.fromObject(knowledgeSource).toString();

        assertTrue(json.contains("\"kind\":\"indexedOneLake\""));
        assertTrue(json.contains("\"indexedOneLakeParameters\""));
        assertTrue(json.contains("\"fabricWorkspaceId\":\"test-workspace-id\""));
        assertTrue(json.contains("\"lakehouseId\":\"test-lakehouse-id\""));
    }

    @Test
    public void OneLakeKnowledgeSourceDeserializationTest() {
        ///string json representation of an IndexedOneLakeKnowledgeSource
        String json = "{" + "\"fabricWorkspaceId\":\"deserialized-workspace-id\","
            + "\"lakehouseId\":\"deserialized-lakehouse-id\"," + "\"targetPath\":\"/deserialized/path\"" + "}";

        IndexedOneLakeKnowledgeSourceParameters params
            = BinaryData.fromString(json).toObject(IndexedOneLakeKnowledgeSourceParameters.class);

        assertEquals("deserialized-workspace-id", params.getFabricWorkspaceId());
        assertEquals("deserialized-lakehouse-id", params.getLakehouseId());
        assertEquals("/deserialized/path", params.getTargetPath());
    }

    private String randomKnowledgeSourceName() {
        // Generate a random name for the knowledge source.
        return testResourceNamer.randomName("knowledge-source-", 63);
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

    private IndexedOneLakeKnowledgeSource createOneLakeKnowledgeSource(String suffix) {
        IndexedOneLakeKnowledgeSourceParameters parameters
            = new IndexedOneLakeKnowledgeSourceParameters("test-workspace-id", "test-lakehouse-id");

        parameters.setTargetPath("/test/path");

        parameters.setIngestionParameters(null);

        return new IndexedOneLakeKnowledgeSource(randomKnowledgeSourceName() + "-" + suffix, parameters);
    }

}
