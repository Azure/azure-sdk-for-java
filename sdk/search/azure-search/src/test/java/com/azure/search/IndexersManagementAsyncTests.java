// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerExecutionStatus;
import com.azure.search.models.IndexerStatus;
import com.azure.search.models.IndexingParameters;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexersManagementAsyncTests extends IndexersManagementTestBase {
    private SearchServiceAsyncClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    protected DataSource createDatasource(DataSource ds) {
        return client.createOrUpdateDataSource(ds).block();
    }

    protected Index createIndex(Index index) {
        return client.createOrUpdateIndex(index).block();
    }

    protected Indexer createIndexer(Indexer indexer) {
        return client.createOrUpdateIndexer(indexer).block();
    }

    @Override
    protected Indexer getIndexer(String indexerName) {
        return client.getIndexer(indexerName).block();
    }

    @Override
    protected Response<Void> deleteIndexer(Indexer indexer) {
        return client.deleteIndexerWithResponse(indexer.getName(), null, null).block();
    }


    @Override
    public void canResetIndexerAndGetIndexerStatus() {
        Indexer indexer = createTestDataSourceAndIndexer();

        Mono<IndexerExecutionInfo> indexerStatusAfterReset = client.resetIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE)
            .flatMap(res -> client.getIndexerStatus(indexer.getName()));


        StepVerifier.create(indexerStatusAfterReset)
            .assertNext(indexerStatus -> {
                Assert.assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
                Assert.assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
            }).verifyComplete();

    }

    @Override
    public void createIndexerReturnsCorrectDefinition() {
        Indexer expectedIndexer = createTestIndexer("indexer");
        expectedIndexer.setIsDisabled(true);
        expectedIndexer.setParameters(
            new IndexingParameters()
                .setBatchSize(50)
                .setMaxFailedItems(10)
                .setMaxFailedItemsPerBatch(10));

        Indexer actualIndexer = client.createOrUpdateIndexer(expectedIndexer).block();

        IndexingParameters ip = new IndexingParameters();
        Map<String, Object> config = new HashMap<>();
        ip.setConfiguration(config);
        expectedIndexer.setParameters(ip); // Get returns empty dictionary.
        expectSameStartTime(expectedIndexer, actualIndexer);

        assertIndexersEqual(expectedIndexer, actualIndexer);
    }

    @Override
    public void canCreateAndListIndexers() {
        List<Indexer> indexers = prepareIndexersForCreateAndListIndexers();

        PagedFlux<Indexer> returnedIndexersList = client.listIndexers();
        StepVerifier
            .create(returnedIndexersList.collectList())
            .assertNext(indexersRes -> {
                Assert.assertEquals(2, indexersRes.size());
                assertIndexersEqual(indexers.get(0), indexersRes.get(0));
                assertIndexersEqual(indexers.get(1), indexersRes.get(1));
            })
            .verifyComplete();
    }

    @Override
    public void canCreateAndListIndexerNames() {
        List<Indexer> indexers = prepareIndexersForCreateAndListIndexers();

        PagedFlux<Indexer> returnedIndexersList = client.listIndexers("name", generateRequestOptions());
        StepVerifier
            .create(returnedIndexersList.collectList())
            .assertNext(indexersRes -> {
                Assert.assertEquals(2, indexersRes.size());
                Assert.assertEquals(indexers.get(0).getName(), indexersRes.get(0).getName());
                Assert.assertEquals(indexers.get(1).getName(), indexersRes.get(1).getName());

                // Assert all other fields than "name" are null:
                assertAllIndexerFieldsNullExceptName(indexersRes.get(0));
                assertAllIndexerFieldsNullExceptName(indexersRes.get(1));
            })
            .verifyComplete();
    }

    @Override
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        Indexer indexer = createTestIndexer("indexer");
        indexer.setDataSourceName("thisdatasourcedoesnotexist");

        assertException(
            () -> client.createOrUpdateIndexer(indexer).block(),
            HttpResponseException.class,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }
}
