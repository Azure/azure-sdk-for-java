// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerExecutionStatus;
import com.azure.search.models.IndexerStatus;
import com.azure.search.models.IndexingParameters;
import org.apache.http.HttpStatus;
import com.azure.search.models.Skillset;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndexersManagementSyncTests extends IndexersManagementTestBase {
    private SearchServiceClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }


    protected DataSource createDatasource(DataSource ds) {
        return client.createOrUpdateDataSource(ds);
    }

    protected Index createIndex(Index index) {
        return client.createOrUpdateIndex(index);
    }

    protected Indexer createIndexer(Indexer indexer) {
        return client.createOrUpdateIndexer(indexer);
    }

    protected Skillset createSkillset(Skillset skillset) {
        return client.createOrUpdateSkillset(skillset);
    }

    @Override
    protected Indexer getIndexer(String indexerName) {
        return client.getIndexer(indexerName);
    }

    @Override
    protected Response<Void> deleteIndexer(Indexer indexer) {
        return client.deleteIndexerWithResponse(indexer.getName(), null, null, Context.NONE);
    }

    @Override
    public void createIndexerReturnsCorrectDefinition() {
        Indexer expectedIndexer =
            createTestIndexer("indexer")
                .setIsDisabled(true)
                .setParameters(
                    new IndexingParameters()
                        .setBatchSize(50)
                        .setMaxFailedItems(10)
                        .setMaxFailedItemsPerBatch(10));

        Indexer actualIndexer = client.createOrUpdateIndexer(expectedIndexer);

        IndexingParameters ip = new IndexingParameters();
        Map<String, Object> config = new HashMap<>();
        ip.setConfiguration(config);
        expectedIndexer.setParameters(ip); // Get returns empty dictionary.
        expectSameStartTime(expectedIndexer, actualIndexer);

        assertIndexersEqual(expectedIndexer, actualIndexer);
    }

    @Override
    public void canCreateAndListIndexers() {

        // Create the data source, note it a valid DS with actual
        // connection string
        DataSource datasource = createTestSqlDataSource();
        client.createOrUpdateDataSource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        client.createOrUpdateIndex(index);


        // Create two indexers
        Indexer indexer1 = createTestIndexer("indexer1").setDataSourceName(datasource.getName());
        Indexer indexer2 = createTestIndexer("indexer2").setDataSourceName(datasource.getName());
        client.createOrUpdateIndexer(indexer1);
        client.createOrUpdateIndexer(indexer2);

        List<Indexer> indexers = client.listIndexers().stream().collect(Collectors.toList());
        Assert.assertEquals(2, indexers.size());

        assertIndexersEqual(indexer1, indexers.get(0));
        assertIndexersEqual(indexer2, indexers.get(1));
    }

    @Override
    public void canCreateAndListIndexerNames() {
        List<Indexer> indexers = prepareIndexersForCreateAndListIndexers();

        List<Indexer> indexersRes = client.listIndexers("name", generateRequestOptions())
            .stream().collect(Collectors.toList());

        Assert.assertEquals(2, indexersRes.size());
        Assert.assertEquals(indexers.get(0).getName(), indexersRes.get(0).getName());
        Assert.assertEquals(indexers.get(1).getName(), indexersRes.get(1).getName());

        // Assert all other fields than "name" are null:
        assertAllIndexerFieldsNullExceptName(indexersRes.get(0));
        assertAllIndexerFieldsNullExceptName(indexersRes.get(1));
    }

    @Override
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        Indexer indexer = createTestIndexer("indexer");
        indexer.setDataSourceName("thisdatasourcedoesnotexist");

        assertException(
            () -> client.createOrUpdateIndexer(indexer),
            HttpResponseException.class,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }

    @Override
    public void canResetIndexerAndGetIndexerStatus() {
        Indexer indexer = createTestDataSourceAndIndexer();

        client.resetIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);

        IndexerExecutionInfo indexerStatus = client.getIndexerStatus(indexer.getName());

        Assert.assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
        Assert.assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
    }

    @Override
    public void canRunIndexer() {
        Indexer indexer = createTestDataSourceAndIndexer();

        Response<Void> response = client.runIndexerWithResponse(indexer.getName(), null, null);
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusCode());

        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }
}
