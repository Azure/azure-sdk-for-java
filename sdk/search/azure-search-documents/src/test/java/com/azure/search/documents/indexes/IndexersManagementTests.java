// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.IndexerExecutionResult;
import com.azure.search.documents.indexes.models.IndexerExecutionStatus;
import com.azure.search.documents.indexes.models.IndexerStatus;
import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.IndexingSchedule;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerLimits;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.BLOB_DATASOURCE_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexersManagementTests extends SearchTestBase {
    private static final String TARGET_INDEX_NAME = "indexforindexers";
    private static final HttpPipelinePolicy MOCK_STATUS_PIPELINE_POLICY = (context, next) -> {
        String url = context.getHttpRequest().getUrl().toString();
        String separator = (url.indexOf('?') >= 0) ? "&" : "?";
        context.getHttpRequest().setUrl(url + separator + "mock_status=inProgress");
        return next.process();
    };

    private static SearchIndexerClient sharedIndexerClient;
    private static SearchIndexClient sharedIndexClient;
    private static SearchIndexerSkillset sharedSkillset;
    private static SearchIndexerDataSourceConnection sharedDatasource;
    private static SearchIndex sharedIndex;

    private final List<String> indexersToDelete = new ArrayList<>();

    private SearchIndexerClient searchIndexerClient;
    private SearchIndexerAsyncClient searchIndexerAsyncClient;

    private SearchIndexer createTestDataSourceAndIndexer() {
        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerClient.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        return indexer;
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    private void createAndValidateIndexerSync(SearchIndexer indexer) {
        // create this indexer in the service
        SearchIndexer indexerResponse = searchIndexerClient.createIndexer(indexer);
        indexersToDelete.add(indexerResponse.getName());

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertObjectEquals(indexer, indexerResponse, true, "etag");
    }

    private void createAndValidateIndexerAsync(SearchIndexer indexer) {
        Mono<SearchIndexer> createIndexerMono = searchIndexerAsyncClient.createIndexer(indexer)
            .map(actual -> {
                indexersToDelete.add(actual.getName());
                setSameStartTime(indexer, actual);

                return actual;
            });

        StepVerifier.create(createIndexerMono)
            .assertNext(actual -> assertObjectEquals(indexer, actual, true, "etag"))
            .verifyComplete();
    }

    @BeforeAll
    public static void setupSharedResources() {
        sharedIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(TestHelpers.getTestTokenCredential())
            .buildClient();
        sharedIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(TestHelpers.getTestTokenCredential())
            .buildClient();

        sharedSkillset = createSkillsetObject();
        sharedDatasource = createSharedDataSource();
        sharedIndex = createTestIndexForLiveDatasource();

        if (TEST_MODE != TestMode.PLAYBACK) {
            sharedSkillset = sharedIndexerClient.createSkillset(sharedSkillset);
            sharedDatasource = sharedIndexerClient.createOrUpdateDataSourceConnection(sharedDatasource);
            sharedIndex = sharedIndexClient.createIndex(sharedIndex);
        }
    }

    @AfterAll
    public static void cleanupSharedResources() {
        if (TEST_MODE == TestMode.PLAYBACK) {
            return; // Running in PLAYBACK, no need to run.
        }

        sharedIndexerClient.deleteSkillset(sharedSkillset.getName());
        sharedIndexerClient.deleteDataSourceConnection(sharedDatasource.getName());
        sharedIndexClient.deleteIndex(sharedIndex.getName());
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();

        searchIndexerClient = getSearchIndexerClientBuilder(true).buildClient();
        searchIndexerAsyncClient = getSearchIndexerClientBuilder(false).buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String indexer : indexersToDelete) {
            searchIndexerClient.deleteIndexer(indexer);
        }
    }

    @Test
    public void createIndexerReturnsCorrectDefinitionSync() {
        SearchIndexer expectedIndexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setIsDisabled(true)
            .setParameters(new IndexingParameters()
                .setBatchSize(50)
                .setMaxFailedItems(10)
                .setMaxFailedItemsPerBatch(10));

        SearchIndexer actualIndexer = searchIndexerClient.createIndexer(expectedIndexer);
        indexersToDelete.add(actualIndexer.getName());

        setSameStartTime(expectedIndexer, actualIndexer);
        assertObjectEquals(expectedIndexer, actualIndexer, true, "etag");
    }

    @Test
    public void createIndexerReturnsCorrectDefinitionAsync() {
        SearchIndexer expectedIndexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setIsDisabled(true)
            .setParameters(new IndexingParameters()
                .setBatchSize(50)
                .setMaxFailedItems(10)
                .setMaxFailedItemsPerBatch(10));

        StepVerifier.create(searchIndexerAsyncClient.createIndexer(expectedIndexer))
            .assertNext(actualIndexer -> {
                indexersToDelete.add(actualIndexer.getName());
                setSameStartTime(expectedIndexer, actualIndexer);
                assertObjectEquals(expectedIndexer, actualIndexer, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canCreateAndListIndexersSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        // Create two indexers
        SearchIndexer indexer1 = createBaseTestIndexerObject("a" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);
        SearchIndexer indexer2 = createBaseTestIndexerObject("b" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);

        searchIndexerClient.createIndexer(indexer1);
        indexersToDelete.add(indexer1.getName());
        searchIndexerClient.createIndexer(indexer2);
        indexersToDelete.add(indexer2.getName());

        Map<String, SearchIndexer> expectedIndexers = new HashMap<>();
        expectedIndexers.put(indexer1.getName(), indexer1);
        expectedIndexers.put(indexer2.getName(), indexer2);

        Map<String, SearchIndexer> actualIndexers = searchIndexerClient.listIndexers().stream()
            .collect(Collectors.toMap(SearchIndexer::getName, si -> si));

        compareMaps(expectedIndexers, actualIndexers,
            (expected, actual) -> assertObjectEquals(expected, actual, true, "etag"));
    }

    @Test
    public void canCreateAndListIndexersAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        // Create two indexers
        SearchIndexer indexer1 = createBaseTestIndexerObject("a" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);
        SearchIndexer indexer2 = createBaseTestIndexerObject("b" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);

        searchIndexerAsyncClient.createIndexer(indexer1).block();
        indexersToDelete.add(indexer1.getName());
        searchIndexerAsyncClient.createIndexer(indexer2).block();
        indexersToDelete.add(indexer2.getName());

        Map<String, SearchIndexer> expectedIndexers = new HashMap<>();
        expectedIndexers.put(indexer1.getName(), indexer1);
        expectedIndexers.put(indexer2.getName(), indexer2);

        Mono<Map<String, SearchIndexer>> listMono = searchIndexerAsyncClient.listIndexers()
            .collect(Collectors.toMap(SearchIndexer::getName, si -> si));

        StepVerifier.create(listMono)
            .assertNext(actualIndexers -> compareMaps(expectedIndexers, actualIndexers,
                (expected, actual) -> assertObjectEquals(expected, actual, true, "etag")))
            .verifyComplete();
    }

    @Test
    public void canCreateAndListIndexerNamesSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer indexer1 = createBaseTestIndexerObject("a" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);
        SearchIndexer indexer2 = createBaseTestIndexerObject("b" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);
        searchIndexerClient.createIndexer(indexer1);
        indexersToDelete.add(indexer1.getName());
        searchIndexerClient.createIndexer(indexer2);
        indexersToDelete.add(indexer2.getName());

        Set<String> expectedIndexers = new HashSet<>(Arrays.asList(indexer1.getName(), indexer2.getName()));
        Set<String> actualIndexers = searchIndexerClient.listIndexerNames().stream().collect(Collectors.toSet());

        assertEquals(expectedIndexers.size(), actualIndexers.size());
        assertTrue(actualIndexers.containsAll(expectedIndexers));
    }

    @Test
    public void canCreateAndListIndexerNamesAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer indexer1 = createBaseTestIndexerObject("a" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);
        SearchIndexer indexer2 = createBaseTestIndexerObject("b" + testResourceNamer.randomName("indexer", 32),
            indexName, dataSourceName);
        searchIndexerAsyncClient.createIndexer(indexer1).block();
        indexersToDelete.add(indexer1.getName());
        searchIndexerAsyncClient.createIndexer(indexer2).block();
        indexersToDelete.add(indexer2.getName());

        Set<String> expectedIndexers = new HashSet<>(Arrays.asList(indexer1.getName(), indexer2.getName()));

        StepVerifier.create(searchIndexerAsyncClient.listIndexerNames().collect(Collectors.toSet()))
            .assertNext(actualIndexers -> {
                assertEquals(expectedIndexers.size(), actualIndexers.size());
                assertTrue(actualIndexers.containsAll(expectedIndexers));
            })
            .verifyComplete();
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserErrorSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), "thisdatasourcedoesnotexist");

        assertHttpResponseException(() -> searchIndexerClient.createIndexer(indexer),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserErrorAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), "thisdatasourcedoesnotexist");

        StepVerifier.create(searchIndexerAsyncClient.createIndexer(indexer))
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_BAD_REQUEST,
                "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist"));
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusSync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        searchIndexerClient.resetIndexer(indexer.getName());
        SearchIndexerStatus indexerStatus = searchIndexerClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusAsync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        searchIndexerAsyncClient.resetIndexer(indexer.getName()).block();

        StepVerifier.create(searchIndexerAsyncClient.getIndexerStatus(indexer.getName()))
            .assertNext(indexerStatus -> {
                assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
                assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
            })
            .verifyComplete();
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusWithResponseSync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        searchIndexerClient.resetIndexerWithResponse(indexer.getName(), Context.NONE);
        SearchIndexerStatus indexerStatusResponse = searchIndexerClient.getIndexerStatusWithResponse(indexer.getName(),
            Context.NONE).getValue();
        assertEquals(IndexerStatus.RUNNING, indexerStatusResponse.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatusResponse.getLastResult().getStatus());
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusWithResponseAsync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        searchIndexerAsyncClient.resetIndexerWithResponse(indexer.getName()).block();

        StepVerifier.create(searchIndexerAsyncClient.getIndexerStatusWithResponse(indexer.getName()))
            .assertNext(response -> {
                assertEquals(IndexerStatus.RUNNING, response.getValue().getStatus());
                assertEquals(IndexerExecutionStatus.RESET, response.getValue().getLastResult().getStatus());
            })
            .verifyComplete();

    }

    @Test
    public void canRunIndexerSync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        searchIndexerClient.runIndexer(indexer.getName());

        SearchIndexerStatus indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerAsync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        searchIndexerAsyncClient.runIndexer(indexer.getName()).block();

        StepVerifier.create(searchIndexerAsyncClient.getIndexerStatus(indexer.getName()))
            .assertNext(info -> assertEquals(IndexerStatus.RUNNING, info.getStatus()))
            .verifyComplete();
    }

    @Test
    public void canRunIndexerWithResponseSync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        Response<Void> response = searchIndexerClient.runIndexerWithResponse(indexer.getName(), Context.NONE);
        SearchIndexerStatus indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());

        assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerWithResponseAsync() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();


        StepVerifier.create(searchIndexerAsyncClient.runIndexerWithResponse(indexer.getName()))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(searchIndexerAsyncClient.getIndexerStatus(indexer.getName()))
            .assertNext(info -> assertEquals(IndexerStatus.RUNNING, info.getStatus()))
            .verifyComplete();
    }

    @Test
    public void canRunIndexerAndGetIndexerStatusSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        SearchIndexerClient mockStatusClient = getSearchIndexerClientBuilder(true, MOCK_STATUS_PIPELINE_POLICY)
            .buildClient();

        mockStatusClient.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        SearchIndexerStatus indexerExecutionInfo = mockStatusClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());

        Response<Void> indexerRunResponse = mockStatusClient.runIndexerWithResponse(indexer.getName(),
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_ACCEPTED, indexerRunResponse.getStatusCode());

        assertValidSearchIndexerStatus(mockStatusClient.getIndexerStatus(indexer.getName()));
    }

    @Test
    public void canRunIndexerAndGetIndexerStatusAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        SearchIndexerAsyncClient mockStatusClient = getSearchIndexerClientBuilder(false, MOCK_STATUS_PIPELINE_POLICY)
            .buildAsyncClient();

        mockStatusClient.createIndexer(indexer).block();
        indexersToDelete.add(indexer.getName());

        StepVerifier.create(mockStatusClient.getIndexerStatus(indexer.getName()))
            .assertNext(info -> assertEquals(IndexerStatus.RUNNING, info.getStatus()))
            .verifyComplete();

        StepVerifier.create(mockStatusClient.runIndexerWithResponse(indexer.getName()))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(mockStatusClient.getIndexerStatus(indexer.getName()))
            .assertNext(this::assertValidSearchIndexerStatus)
            .verifyComplete();
    }

    @Test
    public void canUpdateIndexerSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setDescription("somethingdifferent");
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        // verify the returned updated indexer is as expected
        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setDescription("somethingdifferent");

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexer(updated))
            .assertNext(indexerResponse -> {
                // verify the returned updated indexer is as expected
                setSameStartTime(updated, indexerResponse);
                assertObjectEquals(updated, indexerResponse, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canUpdateIndexerFieldMappingSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setFieldMappings(Collections.singletonList(new FieldMapping("state_alpha").setTargetFieldName("state")));
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        // verify the returned updated indexer is as expected
        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerFieldMappingAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setFieldMappings(Collections.singletonList(new FieldMapping("state_alpha").setTargetFieldName("state")));

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexer(updated))
            .assertNext(indexerResponse -> {
                // verify the returned updated indexer is as expected
                setSameStartTime(updated, indexerResponse);
                assertObjectEquals(updated, indexerResponse, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canCreateIndexerWithFieldMappingSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setFieldMappings(Collections.singletonList(new FieldMapping("state_alpha").setTargetFieldName("state")));

        createAndValidateIndexerSync(indexer);
    }

    @Test
    public void canCreateIndexerWithFieldMappingAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setFieldMappings(Collections.singletonList(new FieldMapping("state_alpha").setTargetFieldName("state")));

        createAndValidateIndexerAsync(indexer);
    }

    @Test
    public void canUpdateIndexerDisabledSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setIsDisabled(false);
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerDisabledAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setIsDisabled(false);

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexer(updated))
            .assertNext(indexerResponse -> {
                setSameStartTime(updated, indexerResponse);
                assertObjectEquals(updated, indexerResponse, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canUpdateIndexerScheduleSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setSchedule(new IndexingSchedule(Duration.ofMinutes(10)));
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerScheduleAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setSchedule(new IndexingSchedule(Duration.ofMinutes(10)));

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexer(updated))
            .assertNext(indexerResponse -> {
                setSameStartTime(updated, indexerResponse);
                assertObjectEquals(updated, indexerResponse, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canCreateIndexerWithScheduleSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setSchedule(new IndexingSchedule(Duration.ofMinutes(10)));

        createAndValidateIndexerSync(indexer);
    }

    @Test
    public void canCreateIndexerWithScheduleAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setSchedule(new IndexingSchedule(Duration.ofMinutes(10)));

        createAndValidateIndexerAsync(indexer);
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItemsSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentIndexingParameters(initial);
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItemsAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentIndexingParameters(initial);

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexer(updated))
            .assertNext(indexerResponse -> {
                setSameStartTime(updated, indexerResponse);
                assertObjectEquals(updated, indexerResponse, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItemsSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createAndValidateIndexerSync(updatedExpected);
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItemsAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createAndValidateIndexerAsync(updatedExpected);
    }

    @Test
    public void canUpdateIndexerBlobParamsSync() {
        String indexName = sharedIndex.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, sharedDatasource.getName()).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithStorageConfig(initial.getName(), indexName,
            sharedDatasource.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerBlobParamsAsync() {
        String indexName = sharedIndex.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, sharedDatasource.getName()).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithStorageConfig(initial.getName(), indexName,
            sharedDatasource.getName());

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexer(updated))
            .assertNext(indexerResponse -> {
                setSameStartTime(updated, indexerResponse);
                assertObjectEquals(updated, indexerResponse, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void canCreateIndexerWithBlobParamsSync() {
        // modify the indexer's blob params
        SearchIndexer indexer = createIndexerWithStorageConfig(sharedIndex.getName(), sharedDatasource.getName());

        createAndValidateIndexerSync(indexer);
    }

    @Test
    public void canCreateIndexerWithBlobParamsAsync() {
        // Create the data source within the search service
        String targetIndexName = sharedIndex.getName();
        SearchIndexer indexer = createIndexerWithStorageConfig(targetIndexName, sharedDatasource.getName());

        createAndValidateIndexerAsync(indexer);
    }

    @Test
    public void canCreateAndDeleteIndexerSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerClient.createIndexer(indexer);

        searchIndexerClient.deleteIndexer(indexer.getName());
        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerAsyncClient.createIndexer(indexer).block();

        searchIndexerAsyncClient.deleteIndexer(indexer.getName()).block();

        StepVerifier.create(searchIndexerAsyncClient.getIndexer(indexer.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponseSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerClient.createIndexerWithResponse(indexer, Context.NONE);

        searchIndexerClient.deleteIndexerWithResponse(indexer, false, Context.NONE);
        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponseAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerAsyncClient.createIndexerWithResponse(indexer).block();

        searchIndexerAsyncClient.deleteIndexerWithResponse(indexer, false).block();

        StepVerifier.create(searchIndexerAsyncClient.getIndexer(indexer.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void deleteIndexerIsIdempotentSync() {
        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        // Try deleting before the indexer even exists.
        Response<Void> result = searchIndexerClient.deleteIndexerWithResponse(indexer, false,
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        searchIndexerClient.createIndexer(indexer);

        // Now delete twice.
        result = searchIndexerClient.deleteIndexerWithResponse(indexer, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());

        result = searchIndexerClient.deleteIndexerWithResponse(indexer, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void deleteIndexerIsIdempotentAsync() {
        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        // Try deleting before the indexer even exists.
        StepVerifier.create(searchIndexerAsyncClient.deleteIndexerWithResponse(indexer, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();

        // Actually create the indexer
        searchIndexerAsyncClient.createIndexer(indexer).block();

        // Now delete twice.
        StepVerifier.create(searchIndexerAsyncClient.deleteIndexerWithResponse(indexer, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(searchIndexerAsyncClient.deleteIndexerWithResponse(indexer, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void canCreateAndGetIndexerSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerClient.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        SearchIndexer indexerResult = searchIndexerClient.getIndexer(indexer.getName());
        assertObjectEquals(indexer, indexerResult, true, "etag");

        indexerResult = searchIndexerClient.getIndexerWithResponse(indexer.getName(), Context.NONE).getValue();
        assertObjectEquals(indexer, indexerResult, true, "etag");
    }

    @Test
    public void canCreateAndGetIndexerAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        searchIndexerAsyncClient.createIndexer(indexer).block();
        indexersToDelete.add(indexer.getName());

        StepVerifier.create(searchIndexerAsyncClient.getIndexer(indexer.getName()))
            .assertNext(indexerResult -> assertObjectEquals(indexer, indexerResult, true, "etag"))
            .verifyComplete();

        StepVerifier.create(searchIndexerAsyncClient.getIndexerWithResponse(indexer.getName()))
            .assertNext(response -> assertObjectEquals(indexer, response.getValue(), true, "etag"))
            .verifyComplete();
    }

    @Test
    public void getIndexerThrowsOnNotFoundSync() {
        assertHttpResponseException(() -> searchIndexerClient.getIndexer("thisindexerdoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND, "Indexer 'thisindexerdoesnotexist' was not found");
    }

    @Test
    public void getIndexerThrowsOnNotFoundAsync() {
        StepVerifier.create(searchIndexerAsyncClient.getIndexer("thisindexerdoesnotexist"))
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_NOT_FOUND,
                "Indexer 'thisindexerdoesnotexist' was not found"));
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResourceSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer created = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, true, Context.NONE)
            .getValue();
        indexersToDelete.add(created.getName());

        assertNotNull(created.getETag());
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResourceAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        StepVerifier.create(searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, true))
            .assertNext(response -> {
                indexersToDelete.add(response.getValue().getName());
                assertNotNull(response.getValue().getETag());
            })
            .verifyComplete();
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExistsSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer created = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();

        searchIndexerClient.deleteIndexerWithResponse(created, true, Context.NONE);

        // Try to delete again and expect to fail
        try {
            searchIndexerClient.deleteIndexerWithResponse(created, true, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExistsAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        Mono<Response<Void>> createDeleteThenFailToDeleteMono =
            searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, false)
                .flatMap(response -> searchIndexerAsyncClient.deleteIndexerWithResponse(response.getValue(), true)
                    .then(searchIndexerAsyncClient.deleteIndexerWithResponse(response.getValue(), true)));

        StepVerifier.create(createDeleteThenFailToDeleteMono)
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResourceSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer stale = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, true, Context.NONE)
            .getValue();

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(stale, false, Context.NONE)
            .getValue();

        try {
            searchIndexerClient.deleteIndexerWithResponse(stale, true, Context.NONE);
            fail("deleteFunc should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        searchIndexerClient.deleteIndexerWithResponse(updated, true, Context.NONE);
    }

    @Test
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResourceAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer stale = searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, true)
            .map(Response::getValue)
            .block();

        SearchIndexer updated = searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(stale, false)
            .map(Response::getValue)
            .block();

        StepVerifier.create(searchIndexerAsyncClient.deleteIndexerWithResponse(stale, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });

        StepVerifier.create(searchIndexerAsyncClient.deleteIndexerWithResponse(updated, true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void updateIndexerIfExistsSucceedsOnExistingResourceSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer original = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(
                original.setDescription("ABrandNewDescription"), false, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertNotNull(updatedETag);
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfExistsSucceedsOnExistingResourceAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        Mono<Tuple2<String, String>> createAndUpdateIndexerMono =
            searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, false)
                .flatMap(response -> {
                    SearchIndexer original = response.getValue();
                    String originalETag = original.getETag();
                    indexersToDelete.add(original.getName());

                    return searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(
                            original.setDescription("an update"), false)
                        .map(updated -> Tuples.of(originalETag, updated.getValue().getETag()));
                });

        StepVerifier.create(createAndUpdateIndexerMono)
            .assertNext(etags -> {
                assertNotNull(etags.getT2());
                assertNotEquals(etags.getT1(), etags.getT2());
            })
            .verifyComplete();
    }

    @Test
    public void updateIndexerIfNotChangedFailsWhenResourceChangedSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer original = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(
                original.setDescription("ABrandNewDescription"), true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Update and check the eTags were changed
        try {
            searchIndexerClient.createOrUpdateIndexerWithResponse(original, true, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        // Check eTags
        assertNotNull(originalETag);
        assertNotNull(updatedETag);
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfNotChangedFailsWhenResourceChangedAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        Mono<Response<SearchIndexer>> createUpdateAndFailToUpdateMono =
            searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, false)
                .flatMap(response -> {
                    SearchIndexer original = response.getValue();
                    String originalETag = original.getETag();
                    indexersToDelete.add(original.getName());

                    return searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(
                            original.setDescription("an update"), true)
                        .map(update -> Tuples.of(originalETag, update.getValue().getETag(), original));
                })
                .doOnNext(etags -> {
                    assertNotNull(etags.getT1());
                    assertNotNull(etags.getT2());
                    assertNotEquals(etags.getT1(), etags.getT2());
                })
                .flatMap(original ->
                    searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(original.getT3(), true));

        StepVerifier.create(createUpdateAndFailToUpdateMono)
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchangedSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());
        SearchIndexer original = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(
                original.setDescription("ABrandNewDescription"), true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Check eTags as expected
        assertNotNull(originalETag);
        assertNotNull(updatedETag);
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchangedAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName());

        Mono<Tuple2<String, String>> createAndUpdateIndexerMono =
            searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, false)
                .flatMap(response -> {
                    SearchIndexer original = response.getValue();
                    String originalETag = original.getETag();
                    indexersToDelete.add(original.getName());

                    return searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(
                            original.setDescription("an update"), true)
                        .map(update -> Tuples.of(originalETag, update.getValue().getETag()));
                });

        StepVerifier.create(createAndUpdateIndexerMono)
            .assertNext(etags -> {
                assertNotNull(etags.getT1());
                assertNotNull(etags.getT2());
                assertNotEquals(etags.getT1(), etags.getT2());
            })
            .verifyComplete();
    }

    @Test
    public void canUpdateIndexerSkillsetSync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setSkillsetName(sharedSkillset.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerSkillsetAsync() {
        String indexName = sharedIndex.getName();
        String dataSourceName = sharedDatasource.getName();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerAsyncClient.createIndexer(initial).block();
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createBaseTestIndexerObject(initial.getName(), indexName, dataSourceName)
            .setSkillsetName(sharedSkillset.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canCreateIndexerWithSkillsetSync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setSkillsetName(sharedSkillset.getName());

        createAndValidateIndexerSync(indexer);
    }

    @Test
    public void canCreateIndexerWithSkillsetAsync() {
        SearchIndexer indexer = createBaseTestIndexerObject(sharedIndex.getName(), sharedDatasource.getName())
            .setSkillsetName(sharedSkillset.getName());

        createAndValidateIndexerAsync(indexer);
    }

    /**
     * Create a new valid skillset object
     *
     * @return the newly created skillset object
     */
    private static SearchIndexerSkillset createSkillsetObject() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry("url").setSource("/document/url"),
            new InputFieldMappingEntry("queryString").setSource("/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(new OutputFieldMappingEntry("text")
            .setTargetName("mytext"));

        List<SearchIndexerSkill> skills = Collections.singletonList(new OcrSkill(inputs, outputs)
            .setShouldDetectOrientation(true)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext("/document"));

        return new SearchIndexerSkillset("shared-ocr-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    private static SearchIndexerDataSourceConnection createSharedDataSource() {
        // create the new data source object for this storage account and container
        return SearchIndexerDataSources.createFromAzureBlobStorage("shared-" + BLOB_DATASOURCE_NAME,
            STORAGE_CONNECTION_STRING, BLOB_CONTAINER_NAME, "/", "real live blob",
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("fieldName")
                .setSoftDeleteMarkerValue("someValue"));
    }

    SearchIndexer createBaseTestIndexerObject(String targetIndexName, String dataSourceName) {
        return createBaseTestIndexerObject(testResourceNamer.randomName("indexer", 32), targetIndexName,
            dataSourceName);
    }

    SearchIndexer createBaseTestIndexerObject(String name, String targetIndexName, String dataSourceName) {
        if (name == null) {
            name = testResourceNamer.randomName("indexer", 32);
        }

        return new SearchIndexer(name, dataSourceName, targetIndexName)
            .setSchedule(new IndexingSchedule(Duration.ofDays(1)));
    }

    /**
     * This index contains fields that are declared on the live data source we use to test the indexers
     *
     * @return the newly created Index object
     */
    private static SearchIndex createTestIndexForLiveDatasource() {
        return new SearchIndex("shared" + IndexersManagementTests.TARGET_INDEX_NAME)
            .setFields(Arrays.asList(
                new SearchField("county_name", SearchFieldDataType.STRING)
                    .setSearchable(Boolean.FALSE)
                    .setFilterable(Boolean.TRUE),
                new SearchField("state", SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE),
                new SearchField("feature_id", SearchFieldDataType.STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)));
    }

    /**
     * Create a new indexer and change its indexing parameters
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentIndexingParameters(SearchIndexer indexer) {
        // create a new indexer object
        IndexingParameters ip = new IndexingParameters()
            .setMaxFailedItems(121)
            .setMaxFailedItemsPerBatch(11)
            .setBatchSize(20);

        // modify the indexer
        indexer.setParameters(ip);

        return indexer;
    }

    SearchIndexer createIndexerWithStorageConfig(String targetIndexName, String dataSourceName) {
        return createIndexerWithStorageConfig(null, targetIndexName, dataSourceName);
    }

    SearchIndexer createIndexerWithStorageConfig(String name, String targetIndexName, String dataSourceName) {
        // create an indexer object
        SearchIndexer updatedExpected = createBaseTestIndexerObject(name, targetIndexName, dataSourceName);

        // just adding some(valid) config values for blobs
        HashMap<String, Object> config = new HashMap<>();
        config.put("indexedFileNameExtensions", ".pdf,.docx");
        config.put("excludedFileNameExtensions", ".xlsx");
        config.put("dataToExtract", "storageMetadata");
        config.put("failOnUnsupportedContentType", false);

        IndexingParameters ip = new IndexingParameters()
            .setConfiguration(config);

        // modify it
        updatedExpected.setParameters(ip);

        return updatedExpected;
    }

    void setSameStartTime(SearchIndexer expected, SearchIndexer actual) {
        // There ought to be a start time in the response; We just can't know what it is because it would
        // make the test timing-dependent.
        expected.getSchedule().setStartTime(actual.getSchedule().getStartTime());
    }

    void assertStartAndEndTimeValid(IndexerExecutionResult result) {
        assertNotNull(result.getStartTime());
        assertNotEquals(OffsetDateTime.now(), result.getStartTime());
        assertNotNull(result.getEndTime());
        assertNotEquals(OffsetDateTime.now(), result.getEndTime());
    }

    void assertValidSearchIndexerStatus(SearchIndexerStatus indexerExecutionInfo) {
        assertEquals(IndexerExecutionStatus.IN_PROGRESS, indexerExecutionInfo.getLastResult().getStatus());
        assertEquals(3, indexerExecutionInfo.getExecutionHistory().size());

        SearchIndexerLimits limits = indexerExecutionInfo.getLimits();
        assertNotNull(limits);
        assertEquals(100000, limits.getMaxDocumentContentCharactersToExtract(), 0);
        assertEquals(1000, limits.getMaxDocumentExtractionSize(), 0);

        IndexerExecutionResult newestResult = indexerExecutionInfo.getExecutionHistory().get(0);
        IndexerExecutionResult middleResult = indexerExecutionInfo.getExecutionHistory().get(1);
        IndexerExecutionResult oldestResult = indexerExecutionInfo.getExecutionHistory().get(2);

        assertEquals(IndexerExecutionStatus.TRANSIENT_FAILURE, newestResult.getStatus());
        assertEquals("The indexer could not connect to the data source",
            newestResult.getErrorMessage());
        assertStartAndEndTimeValid(newestResult);

        assertEquals(IndexerExecutionStatus.RESET, middleResult.getStatus());
        assertStartAndEndTimeValid(middleResult);

        assertEquals(IndexerExecutionStatus.SUCCESS, oldestResult.getStatus());
        assertEquals(124876, oldestResult.getItemCount());
        assertEquals(2, oldestResult.getFailedItemCount());
        assertEquals("100", oldestResult.getInitialTrackingState());
        assertEquals("200", oldestResult.getFinalTrackingState());
        assertStartAndEndTimeValid(oldestResult);

        assertEquals(2, oldestResult.getErrors().size());
        assertEquals("1", oldestResult.getErrors().get(0).getKey());
        assertEquals("Key field contains unsafe characters",
            oldestResult.getErrors().get(0).getErrorMessage());
        assertEquals("DocumentExtraction.AzureBlob.MyDataSource",
            oldestResult.getErrors().get(0).getName());
        assertEquals("The file could not be parsed.", oldestResult.getErrors().get(0).getDetails());
        assertEquals("https://go.microsoft.com/fwlink/?linkid=2049388",
            oldestResult.getErrors().get(0).getDocumentationLink());

        assertEquals("121713", oldestResult.getErrors().get(1).getKey());
        assertEquals("Item is too large", oldestResult.getErrors().get(1).getErrorMessage());
        assertEquals("DocumentExtraction.AzureBlob.DataReader",
            oldestResult.getErrors().get(1).getName());
        assertEquals("Blob size cannot exceed 256 MB.", oldestResult.getErrors().get(1).getDetails());
        assertEquals("https://go.microsoft.com/fwlink/?linkid=2049388",
            oldestResult.getErrors().get(1).getDocumentationLink());


        assertEquals(1, oldestResult.getWarnings().size());
        assertEquals("2", oldestResult.getWarnings().get(0).getKey());
        assertEquals("Document was truncated to 50000 characters.",
            oldestResult.getWarnings().get(0).getMessage());
        assertEquals("Enrichment.LanguageDetectionSkill.#4",
            oldestResult.getWarnings().get(0).getName());
        assertEquals("Try to split the input into smaller chunks using Split skill.",
            oldestResult.getWarnings().get(0).getDetails());
        assertEquals("https://go.microsoft.com/fwlink/?linkid=2099692",
            oldestResult.getWarnings().get(0).getDocumentationLink());
    }
}
