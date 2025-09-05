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
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeSource;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
