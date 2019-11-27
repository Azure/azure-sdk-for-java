// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
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
import com.azure.search.test.AccessConditionAsyncTests;
import com.azure.search.test.AccessOptions;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndexersManagementAsyncTests extends IndexersManagementTestBase {
    private SearchServiceAsyncClient client;

    // commonly used lambda definitions
    private BiFunction<Indexer,
        AccessOptions,
        Mono<Indexer>> createOrUpdateAsyncFunc =
            (Indexer indexer, AccessOptions ac) ->
                createIndexer(indexer, ac.getAccessCondition(), ac.getRequestOptions());

    private BiFunction<Indexer,
        AccessOptions,
        Mono<Indexer>> createOrUpdateWithResponseAsyncFunc =
            (Indexer indexer, AccessOptions ac) ->
                createIndexerWithResponse(indexer, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Indexer> newIndexerFunc =
        () -> createBaseTestIndexerObject("name", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);

    private Function<Indexer, Indexer> changeIndexerFunc =
        (Indexer indexer) -> indexer.setDescription("ABrandNewDescription");

    private BiFunction<String, AccessOptions, Mono<Void>> deleteIndexerAsyncFunc =
        (String name, AccessOptions ac) ->
            deleteIndexer(name, ac.getAccessCondition(), ac.getRequestOptions());

    private DataSource createDatasource(DataSource ds) {
        return client.createOrUpdateDataSource(ds).block();
    }

    private Index createIndex(Index index) {
        return client.createOrUpdateIndex(index).block();
    }

    private Skillset createSkillset(Skillset skillset) {
        return client.createOrUpdateSkillset(skillset).block();
    }

    private Mono<Indexer> createIndexer(Indexer indexer,
                                         AccessCondition accessCondition,
                                         RequestOptions requestOptions) {
        return client.createOrUpdateIndexer(
            indexer,
            accessCondition,
            requestOptions);
    }

    private Mono<Indexer> createIndexerWithResponse(Indexer indexer,
                                         AccessCondition accessCondition,
                                         RequestOptions requestOptions) {
        return client.createOrUpdateIndexerWithResponse(
            indexer,
            accessCondition,
            requestOptions,
            Context.NONE)
            .map(Response::getValue);
    }

    private Mono<Indexer> createIndexer(Indexer indexer) {
        return client.createOrUpdateIndexer(indexer);
    }

    private Indexer getIndexer(String indexerName) {
        return client.getIndexer(indexerName).block();
    }

    private Mono<Void> deleteIndexer(String indexerName,
                                     AccessCondition accessCondition,
                                     RequestOptions requestOptions) {
        return client.deleteIndexer(indexerName,
            accessCondition,
            requestOptions);
    }

    private Mono<Response<Void>> deleteIndexerWithResponse(String indexerName,
                                                           AccessCondition accessCondition,
                                                           RequestOptions requestOptions) {
        return client.deleteIndexerWithResponse(indexerName,
            accessCondition,
            requestOptions);
    }

    protected void createDatasourceAndIndex(String dataSourceName, String indexName) {
        // Create DataSource
        DataSource dataSource = createTestSqlDataSourceObject(dataSourceName);
        createDatasource(dataSource);

        // Create an index
        Index index = createTestIndexForLiveDatasource(indexName);
        createIndex(index);
    }

    List<Indexer> prepareIndexersForCreateAndListIndexers() {
        // Create DataSource and Index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        // Create two indexers
        Indexer indexer1 = createBaseTestIndexerObject("indexer1", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);
        Indexer indexer2 = createBaseTestIndexerObject("indexer2", TARGET_INDEX_NAME)
            .setDataSourceName(SQL_DATASOURCE_NAME);
        createIndexer(indexer1).block();
        createIndexer(indexer2).block();

        return Arrays.asList(indexer1, indexer2);
    }


    protected Indexer createTestDataSourceAndIndexer() {
        // Create DataSource and Index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        // Create the indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        createIndexer(indexer).block();

        return indexer;
    }

    /**
     * Creates the index and indexer in the search service and then update the indexer
     *
     * @param updatedIndexer the indexer to be updated
     * @param dataSourceName the data source name for this indexer
     */
    void createUpdateAndValidateIndexer(Indexer updatedIndexer, String dataSourceName) {
        updatedIndexer.setDataSourceName(dataSourceName);

        // Create an index
        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        createIndex(index);

        Indexer initial =
            createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
                .setDataSourceName(dataSourceName)
                .setIsDisabled(true);

        // create this indexer in the service
        createIndexer(initial).block();

        // update the indexer in the service
        Indexer indexerResponse = createIndexer(updatedIndexer, null, null).block();

        // verify the returned updated indexer is as expected
        setSameStartTime(updatedIndexer, indexerResponse);
        assertIndexersEqual(updatedIndexer, indexerResponse);
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    void createAndValidateIndexer(Indexer indexer) {
        // Create an index
        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        createIndex(index);

        // create this indexer in the service
        Indexer indexerResponse = createIndexer(indexer).block();

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertIndexersEqual(indexer, indexerResponse);
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
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
        Indexer expectedIndexer =
            createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
                .setIsDisabled(true)
                .setDataSourceName(SQL_DATASOURCE_NAME)
                .setParameters(
                    new IndexingParameters()
                        .setBatchSize(50)
                        .setMaxFailedItems(10)
                        .setMaxFailedItemsPerBatch(10));

        Indexer actualIndexer = client.createOrUpdateIndexer(expectedIndexer).block();

        IndexingParameters ip = new IndexingParameters();
        Map<String, Object> config = new HashMap<>();
        ip.setConfiguration(config);
        expectedIndexer.setParameters(ip); // Get returns empty dictionary.
        setSameStartTime(expectedIndexer, actualIndexer);

        assertIndexersEqual(expectedIndexer, actualIndexer);
    }

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
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        indexer.setDataSourceName("thisdatasourcedoesnotexist");

        assertException(
            () -> client.createOrUpdateIndexer(indexer).block(),
            HttpResponseException.class,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }

    @Override
    public void canRunIndexer() {
        Indexer indexer = createTestDataSourceAndIndexer();

        Response<Void> response =
            client.runIndexerWithResponse(indexer.getName(), null, null).block();
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusCode());

        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName()).block();
        Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Override
    public void createOrUpdateIndexerIfNotExistsFailsOnExistingResource() {
        // Prepare data source and index
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        act.createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
            createOrUpdateAsyncFunc,
            newIndexerFunc,
            changeIndexerFunc);
    }

    @Override
    public void canUpdateIndexer() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentDescription();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canUpdateIndexerFieldMapping() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentFieldMapping();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithFieldMapping() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer indexer = createIndexerWithDifferentFieldMapping()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Override
    public void canUpdateIndexerDisabled() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer updatedExpected = createDisabledIndexer();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canUpdateIndexerSchedule() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentSchedule();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithSchedule() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer indexer = createIndexerWithDifferentSchedule()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Override
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);

        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer)
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(updatedExpected);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Override
    public void canUpdateIndexerBlobParams() {
        // Create the needed Azure blob resources and data source object
        DataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        createDatasource(blobDataSource);

        // modify the indexer's blob params
        Indexer updatedExpected = createIndexerWithStorageConfig();

        createUpdateAndValidateIndexer(updatedExpected, BLOB_DATASOURCE_NAME);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Override
    public void canCreateIndexerWithBlobParams() {
        // Create the needed Azure blob resources and data source object
        DataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        DataSource dataSource = createDatasource(blobDataSource);

        // modify the indexer's blob params
        Indexer indexer = createIndexerWithStorageConfig()
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Override
    public void deleteIndexerIsIdempotent() {
        // Create Datasource
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        // Create the indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);

        // Try delete before the indexer even exists.
        Response<Void> result = deleteIndexerWithResponse(indexer.getName(), null, null)
            .block();

        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        createIndexer(indexer).block();

        // Now delete twice.
        result = deleteIndexerWithResponse(indexer.getName(), null, null)
            .block();
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, result.getStatusCode());

        result = deleteIndexerWithResponse(indexer.getName(), null, null)
            .block();
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());
    }

    @Override
    public void canCreateAndGetIndexer() {
        String indexerName = "indexer";

        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        Index index = createTestIndexForLiveDatasource(TARGET_INDEX_NAME);
        createIndex(index);

        Indexer indexer =
            createBaseTestIndexerObject(indexerName, TARGET_INDEX_NAME).setDataSourceName(dataSource.getName());

        createIndexer(indexer).block();

        Indexer indexerResult = getIndexer(indexerName);

        assertIndexersEqual(indexer, indexerResult);
    }

    @Override
    public void getIndexerThrowsOnNotFound() {
        assertException(
            () -> getIndexer("thisindexerdoesnotexist"),
            HttpResponseException.class,
            "Indexer 'thisindexerdoesnotexist' was not found");
    }

    @Override
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateAsyncFunc,
            newIndexerFunc);
    }

    @Override
    public void createOrUpdateIndexerWithResponseIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateWithResponseAsyncFunc,
            newIndexerFunc);
    }

    @Override
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        String indexerName = "name";
        act.deleteIfExistsWorksOnlyWhenResourceExistsAsync(
            deleteIndexerAsyncFunc,
            createOrUpdateAsyncFunc,
            newIndexerFunc,
            indexerName);
    }

    @Override
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        String indexerName = "name";
        act.deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
            deleteIndexerAsyncFunc,
            newIndexerFunc,
            createOrUpdateAsyncFunc,
            changeIndexerFunc,
            indexerName);
    }

    @Override
    public void updateIndexerIfExistsFailsOnNoResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsFailsOnNoResourceAsync(
            newIndexerFunc,
            createOrUpdateAsyncFunc);
    }

    @Override
    public void updateIndexerIfExistsSucceedsOnExistingResource() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsSucceedsOnExistingResourceAsync(
            newIndexerFunc,
            createOrUpdateAsyncFunc,
            changeIndexerFunc);
    }

    @Override
    public void updateIndexerIfNotChangedFailsWhenResourceChanged() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedFailsWhenResourceChangedAsync(
            newIndexerFunc,
            createOrUpdateAsyncFunc,
            changeIndexerFunc);
    }

    @Override
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged() throws NoSuchFieldException, IllegalAccessException {
        // Prepare data source and index
        createDatasourceAndIndex(SQL_DATASOURCE_NAME, TARGET_INDEX_NAME);

        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
            newIndexerFunc,
            createOrUpdateAsyncFunc,
            changeIndexerFunc);
    }

    @Override
    public void canUpdateIndexerSkillset() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        // Create a new skillset object
        // todo: task 1544 - change all over the code that that the object creation and actual service creation will
        // have meaningful and differentiated names
        Skillset skillset = createSkillsetObject();

        // create the skillset in the search service
        createSkillset(skillset);
        Indexer updatedExpected = createIndexerWithDifferentSkillset(skillset.getName());
        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithSkillset() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        createDatasource(dataSource);

        // Create a new skillset object
        // todo: task 1544 - change all over the code that that the object creation and actual service creation will
        // have meaningful and differentiated names
        Skillset skillset = createSkillsetObject();

        // create the skillset in the search service
        createSkillset(skillset);
        Indexer indexer = createIndexerWithDifferentSkillset(skillset.getName())
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Override
    public void existsReturnsTrueForExistingIndexer() {
        Indexer indexer = createTestDataSourceAndIndexer();

        StepVerifier.create(client.indexerExists(indexer.getName()))
            .expectNext(true)
            .verifyComplete();
    }

    @Override
    public void existsReturnsFalseForNonExistingIndexer() {

        StepVerifier.create(client.indexerExists("invalidindex"))
            .expectNext(false)
            .verifyComplete();
    }
}
