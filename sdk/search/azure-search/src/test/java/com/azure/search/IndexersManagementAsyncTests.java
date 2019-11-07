// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexingParameters;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

public class IndexersManagementAsyncTests extends IndexersManagementTestBase {
    private SearchServiceAsyncClient client;

    @Override
    public void createIndexerReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildAsyncClient();
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
        client = getSearchServiceClientBuilder().buildAsyncClient();

        // Create the data source, note it a valid DS with actual
        // connection string
        DataSource datasource = createTestSqlDataSource();
        client.createOrUpdateDataSource(datasource).block();

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        client.createOrUpdateIndex(index).block();


        // Create two indexers
        Indexer indexer1 = createTestIndexer("i1");
        Indexer indexer2 = createTestIndexer("i2");
        client.createOrUpdateIndexer(indexer1).block();
        client.createOrUpdateIndexer(indexer2).block();

        PagedFlux<Indexer> returnedIndexersLst = client.listIndexers();
        StepVerifier
            .create(returnedIndexersLst.collectList())
            .assertNext(indexers -> {
                Assert.assertEquals(2, indexers.size());
                Assert.assertTrue(indexers.get(0).getName().equals(indexer1.getName()));
                Assert.assertTrue(indexers.get(1).getName().equals(indexer2.getName()));
            })
            .verifyComplete();
    }

    @Override
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        client = getSearchServiceClientBuilder().buildAsyncClient();
        Indexer indexer = createTestIndexer("indexer");
        indexer.setDataSourceName("thisdatasourcedoesnotexist");

        assertException(
            () -> client.createOrUpdateIndexer(indexer).block(),
            HttpResponseException.class,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }
}
