// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerExecutionStatus;
import com.azure.search.models.IndexerStatus;
import com.azure.search.models.IndexingParameters;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.Skillset;
import com.azure.search.test.AccessConditionTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IndexersManagementSyncTests extends IndexersManagementTestBase {
    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<Indexer,
        AccessOptions,
        Indexer> createOrUpdateIndexerFunc =
            (Indexer indexer, AccessOptions ac) ->
                createOrUpdateIndexer(indexer, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Indexer> newIndexerFunc =
        () -> createBaseTestIndexerObject("name", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);

    private Function<Indexer, Indexer> mutateIndexerFunc =
        (Indexer indexer) -> indexer.setDescription("ABrandNewDescription");

    private BiConsumer<String, AccessOptions> deleteIndexerFunc =
        (String name, AccessOptions ac) ->
            client.deleteIndexerWithResponse(name, ac.getAccessCondition(), ac.getRequestOptions(), Context.NONE);


    private void createDataSourceAndIndex(String dataSourceName, String indexName) {
        // Create DataSource
        DataSource dataSource = createTestSqlDataSourceObject(dataSourceName);
        client.createOrUpdateDataSource(dataSource);

        // Create an index
        Index index = createTestIndexForLiveDatasource(indexName);
        client.createIndex(index);
    }

    private List<Indexer> prepareIndexersForCreateAndListIndexers() {
        // Create DataSource and Index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        // Create two indexers
        Indexer indexer1 = createBaseTestIndexerObject("indexer1", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);
        Indexer indexer2 = createBaseTestIndexerObject("indexer2", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexer(indexer1);
        client.createIndexer(indexer2);

        return Arrays.asList(indexer1, indexer2);
    }

    private Indexer createTestDataSourceAndIndexer() {
        // Create DataSource and Index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        // Create the indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexer(indexer);

        return indexer;
    }

    /**
     * Creates the index and indexer in the search service and then update the indexer
     *
     * @param updatedIndexer the indexer to be updated
     * @param dataSourceName the data source name for this indexer
     */
    private void createUpdateAndValidateIndexer(Indexer updatedIndexer, String dataSourceName) {
        updatedIndexer.setDataSourceName(dataSourceName);

        // Create an index
        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        client.createIndex(index);

        Indexer initial =
            createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
                .setDataSourceName(dataSourceName)
                .setIsDisabled(true);

        // create this indexer in the service
        client.createIndexer(initial);

        // update the indexer in the service
        Indexer indexerResponse = client.createOrUpdateIndexer(updatedIndexer);

        // verify the returned updated indexer is as expected
        setSameStartTime(updatedIndexer, indexerResponse);
        assertIndexersEqual(updatedIndexer, indexerResponse);
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    private void createAndValidateIndexer(Indexer indexer) {
        // Create an index
        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        client.createIndex(index);

        // create this indexer in the service
        Indexer indexerResponse = client.createIndexer(indexer);

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertIndexersEqual(indexer, indexerResponse);
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    private Indexer createOrUpdateIndexer(Indexer indexer,
        AccessCondition accessCondition,
        RequestOptions requestOptions) {
        return client.createOrUpdateIndexerWithResponse(
            indexer, accessCondition, requestOptions, Context.NONE)
            .getValue();
    }

    @Test
    public void createIndexerReturnsCorrectDefinition() {
        Indexer expectedIndexer =
            createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
                .setIsDisabled(true)
                .setDataSourceName(SQL_DATASOURCE_NAME)
                .setParameters(
                    new IndexingParameters()
                        .setBatchSize(50)
                        .setMaxFailedItems(10)
                        .setMaxFailedItemsPerBatch(10));

        Indexer actualIndexer = client.createIndexer(expectedIndexer);

        IndexingParameters ip = new IndexingParameters();
        Map<String, Object> config = new HashMap<>();
        ip.setConfiguration(config);
        expectedIndexer.setParameters(ip); // Get returns empty dictionary.
        setSameStartTime(expectedIndexer, actualIndexer);

        assertIndexersEqual(expectedIndexer, actualIndexer);
    }

    @Test
    public void canCreateAndListIndexers() {

        // Create the data source, note it a valid DS with actual
        // connection string
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        // Create an index
        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        client.createIndex(index);

        // Create two indexers
        Indexer indexer1 = createBaseTestIndexerObject("indexer1", TARGET_INDEX_NAME)
            .setDataSourceName(dataSource.getName());
        Indexer indexer2 = createBaseTestIndexerObject("indexer2", TARGET_INDEX_NAME)
            .setDataSourceName(dataSource.getName());
        client.createIndexer(indexer1);
        client.createIndexer(indexer2);

        List<Indexer> indexers = client.listIndexers().stream().collect(Collectors.toList());
        Assert.assertEquals(2, indexers.size());
        assertIndexersEqual(indexer1, indexers.get(0));
        assertIndexersEqual(indexer2, indexers.get(1));
    }

    @Test
    public void canCreateAndListIndexerNames() {
        List<Indexer> indexers = prepareIndexersForCreateAndListIndexers();

        List<Indexer> indexersRes = client.listIndexers("name", generateRequestOptions(), Context.NONE)
            .stream().collect(Collectors.toList());

        Assert.assertEquals(2, indexersRes.size());
        Assert.assertEquals(indexers.get(0).getName(), indexersRes.get(0).getName());
        Assert.assertEquals(indexers.get(1).getName(), indexersRes.get(1).getName());

        // Assert all other fields than "name" are null:
        assertAllIndexerFieldsNullExceptName(indexersRes.get(0));
        assertAllIndexerFieldsNullExceptName(indexersRes.get(1));
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
            .setDataSourceName("thisdatasourcedoesnotexist");

        assertHttpResponseException(
            () -> client.createIndexer(indexer),
            HttpResponseStatus.BAD_REQUEST,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }

    @Test
    public void canResetIndexerAndGetIndexerStatus() {
        Indexer indexer = createTestDataSourceAndIndexer();

        client.resetIndexer(indexer.getName());
        IndexerExecutionInfo indexerStatus = client.getIndexerStatus(indexer.getName());
        Assert.assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
        Assert.assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusWithResponse() {
        Indexer indexer = createTestDataSourceAndIndexer();

        client.resetIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        IndexerExecutionInfo indexerStatusResponse = client.getIndexerStatusWithResponse(indexer.getName(),
            generateRequestOptions(), Context.NONE).getValue();
        Assert.assertEquals(IndexerStatus.RUNNING, indexerStatusResponse.getStatus());
        Assert.assertEquals(IndexerExecutionStatus.RESET, indexerStatusResponse.getLastResult().getStatus());
    }

    @Test
    public void canRunIndexer() {
        Indexer indexer = createTestDataSourceAndIndexer();
        client.runIndexer(indexer.getName());

        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerWithResponse() {
        Indexer indexer = createTestDataSourceAndIndexer();
        Response<Void> response = client.runIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());

        Assert.assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusCode());
        Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerAndGetIndexerStatus() {
        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        client = getSearchServiceClientBuilderWithHttpPipelinePolicies(
            Collections.singletonList(MOCK_STATUS_PIPELINE_POLICY))
            .buildClient();

        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);

        client.createIndexer(indexer);

        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());

        Response<Void> indexerRunResponse = client.runIndexerWithResponse(indexer.getName(), new RequestOptions(),
            Context.NONE);
        Assert.assertEquals(HttpResponseStatus.ACCEPTED.code(), indexerRunResponse.getStatusCode());

        indexerExecutionInfo = client.getIndexerStatus(indexer.getName());

        assertValidIndexerExecutionInfo(indexerExecutionInfo);
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsFailsOnExistingResource() {
        // Prepare data source and index
        AccessConditionTests act = new AccessConditionTests();

        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        act.createOrUpdateIfNotExistsFailsOnExistingResource(
            createOrUpdateIndexerFunc,
            newIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void canUpdateIndexer() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentDescription();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerFieldMapping() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentFieldMapping();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithFieldMapping() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createIndexerWithDifferentFieldMapping()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerDisabled() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createDisabledIndexer();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerSchedule() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentSchedule();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithSchedule() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createIndexerWithDifferentSchedule()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);

        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer)
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(updatedExpected);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canUpdateIndexerBlobParams() {
        // Create the needed Azure blob resources and data source object
        DataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        client.createOrUpdateDataSource(blobDataSource);

        // modify the indexer's blob params
        Indexer updatedExpected = createIndexerWithStorageConfig();

        createUpdateAndValidateIndexer(updatedExpected, BLOB_DATASOURCE_NAME);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canCreateIndexerWithBlobParams() {
        // Create the needed Azure blob resources and data source object
        DataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        DataSource dataSource = client.createOrUpdateDataSource(blobDataSource);

        // modify the indexer's blob params
        Indexer indexer = createIndexerWithStorageConfig()
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canCreateAndDeleteIndexer() {
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexer(indexer);

        client.deleteIndexer(indexer.getName());
        Assert.assertFalse(client.indexerExists(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponse() {
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexerWithResponse(indexer, new RequestOptions(), Context.NONE);

        client.deleteIndexerWithResponse(indexer.getName(), new AccessCondition(), new RequestOptions(), Context.NONE);
        Assert.assertFalse(client.indexerExistsWithResponse(indexer.getName(), new RequestOptions(), Context.NONE)
            .getValue());
    }

    @Test
    public void deleteIndexerIsIdempotent() {
        // Create Datasource
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        // Create the indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);

        // Try delete before the indexer even exists.
        Response<Void> result = client.deleteIndexerWithResponse(
            indexer.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        client.createIndexer(indexer);

        // Now delete twice.
        result = client.deleteIndexerWithResponse(
            indexer.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, result.getStatusCode());

        result = client.deleteIndexerWithResponse(
            indexer.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void canCreateAndGetIndexer() {
        String indexerName = "indexer";
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        client.createIndex(index);

        Indexer indexer = createBaseTestIndexerObject(indexerName, TARGET_INDEX_NAME)
            .setDataSourceName(dataSource.getName());

        client.createIndexer(indexer);
        Indexer indexerResult = client.getIndexer(indexerName);
        assertIndexersEqual(indexer, indexerResult);

        indexerResult = client.getIndexerWithResponse(indexerName, generateRequestOptions(), Context.NONE).getValue();
        assertIndexersEqual(indexer, indexerResult);
    }

    @Test
    public void getIndexerThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getIndexer("thisindexerdoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "Indexer 'thisindexerdoesnotexist' was not found");
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        // Prepare data source and index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        Indexer indexerResult = act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateIndexerFunc,
            newIndexerFunc);

        Assert.assertTrue(StringUtils.isNoneEmpty(indexerResult.getETag()));
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests act = new AccessConditionTests();

        // Prepare data source and index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        String indexerName = "name";
        act.deleteIfExistsWorksOnlyWhenResourceExists(
            deleteIndexerFunc,
            createOrUpdateIndexerFunc,
            newIndexerFunc,
            indexerName);
    }

    @Test
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests act = new AccessConditionTests();

        // Prepare data source and index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        String indexerName = "name";
        act.deleteIfNotChangedWorksOnlyOnCurrentResource(
            deleteIndexerFunc,
            newIndexerFunc,
            createOrUpdateIndexerFunc,
            indexerName);
    }

    @Test
    public void updateIndexerIfExistsFailsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsFailsOnNoResource(
            newIndexerFunc,
            createOrUpdateIndexerFunc);
    }

    @Test
    public void updateIndexerIfExistsSucceedsOnExistingResource() {
        // Prepare datasource and index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsSucceedsOnExistingResource(
            newIndexerFunc,
            createOrUpdateIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void updateIndexerIfNotChangedFailsWhenResourceChanged() {
        // Prepare datasource and index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedFailsWhenResourceChanged(
            newIndexerFunc,
            createOrUpdateIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged() {
        // Prepare datasource and index
        createDataSourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchanged(
            newIndexerFunc,
            createOrUpdateIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void canUpdateIndexerSkillset() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        // Create a new skillset object
        // todo: task 1544 - change all over the code that that the object creation and actual service creation will
        // have meaningful and differentiated names
        Skillset skillset = createSkillsetObject();

        // create the skillset in the search service
        client.createSkillset(skillset);
        Indexer updatedExpected = createIndexerWithDifferentSkillset(skillset.getName());
        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithSkillset() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource);

        // Create a new skillset object
        // todo: task 1544 - change all over the code that that the object creation and actual service creation will
        // have meaningful and differentiated names
        Skillset skillset = createSkillsetObject();

        // create the skillset in the search service
        client.createSkillset(skillset);

        Indexer indexer = createIndexerWithDifferentSkillset(skillset.getName())
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Test
    public void existsReturnsTrueForExistingIndexer() {
        Indexer indexer = createTestDataSourceAndIndexer();

        Assert.assertTrue(client.indexerExists(indexer.getName()));
    }

    @Test
    public void existsReturnsTrueForExistingIndexerWithResponse() {
        Indexer indexer = createTestDataSourceAndIndexer();

        Assert.assertTrue(
            client.indexerExistsWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE).getValue());
    }

    @Test
    public void existsReturnsFalseForNonExistingIndexer() {
        Assert.assertFalse(client.indexerExists("invalidindex"));
    }
}
