// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Indexer> createOrUpdateFunc =
            (Indexer indexer, AccessOptions ac) ->
                createIndexer(indexer, ac.getAccessCondition(), ac.getRequestOptions());

    private BiFunction<Indexer,
        AccessOptions,
        Indexer> createOrUpdateWithResponseFunc =
            (Indexer indexer, AccessOptions ac) ->
                createIndexerWithResponse(indexer, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Indexer> newIndexerFunc =
        () -> createTestIndexer("name")
            .setDataSourceName(SQL_DATASOURCE_NAME);

    private Function<Indexer, Indexer> changeIndexerFunc =
        (Indexer indexer) -> indexer.setDescription("ABrandNewDescription");

    private BiConsumer<String, AccessOptions> deleteIndexerFunc =
        (String name, AccessOptions ac) ->
            deleteIndexer(name, ac.getAccessCondition(), ac.getRequestOptions());


    protected void createDatasourceAndIndex() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);
    }

    List<Indexer> prepareIndexersForCreateAndListIndexers() {
        // Create the data source, note it is a valid DS with actual connection string
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // Create two indexers
        Indexer indexer1 = createTestIndexer("indexer1").setDataSourceName(datasource.getName());
        Indexer indexer2 = createTestIndexer("indexer2").setDataSourceName(datasource.getName());
        createIndexer(indexer1, null, null);
        createIndexer(indexer2, null, null);

        return Arrays.asList(indexer1, indexer2);
    }


    protected Indexer createTestDataSourceAndIndexer() {
        // Create Datasource
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // Create the indexer object
        Indexer indexer = createTestIndexer("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        createIndexer(indexer, null, null);

        return indexer;
    }

    static void expectSameStartTime(Indexer expected, Indexer actual) {
        // There ought to be a start time in the response; We just can't know what it is because it would
        // make the test timing-dependent.
        expected.getSchedule().setStartTime(actual.getSchedule().getStartTime());
    }

    /**
     * This index contains fields that are declared on the live datasource
     * we use to test the indexers
     *
     * @return the newly created Index object
     */
    protected Index createTestIndexForLiveDatasource() {
        return new Index()
            .setName("indexforindexers")
            .setFields(Arrays.asList(
                new Field()
                    .setName("county_name")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.FALSE)
                    .setFilterable(Boolean.TRUE),
                new Field()
                    .setName("state")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE),
                new Field()
                    .setName("feature_id")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)));
    }

    void assertAllIndexerFieldsNullExceptName(Indexer indexer) {
        Assert.assertNull(indexer.getParameters());
        Assert.assertNull(indexer.getDataSourceName());
        Assert.assertNull(indexer.getDescription());
        Assert.assertNull(indexer.getETag());
        Assert.assertNull(indexer.getFieldMappings());
        Assert.assertNull(indexer.getOutputFieldMappings());
        Assert.assertNull(indexer.getSchedule());
        Assert.assertNull(indexer.getSkillsetName());
        Assert.assertNull(indexer.getTargetIndexName());
    }

    /**
     * Creates the index and indexer in the search service and then update the indexer
     *
     * @param updatedIndexer the indexer to be updated
     * @param datasourceName the datasource name for this indexer
     */
    void createUpdateAndValidateIndexer(Indexer updatedIndexer, String datasourceName) {
        updatedIndexer.setDataSourceName(datasourceName);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        Indexer initial =
            createTestIndexer("indexer")
                .setDataSourceName(datasourceName)
                .setIsDisabled(true);

        // create this indexer in the service
        createIndexer(initial);

        // update the indexer in the service
        Indexer indexerResponse = createIndexer(updatedIndexer, null, null);

        // verify the returned updated indexer is as expected
        expectSameStartTime(updatedIndexer, indexerResponse);
        assertIndexersEqual(updatedIndexer, indexerResponse);
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    void createAndValidateIndexer(Indexer indexer) {
        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // create this indexer in the service
        Indexer indexerResponse = createIndexer(indexer, null, null);

        // verify the returned updated indexer is as expected
        expectSameStartTime(indexer, indexerResponse);
        assertIndexersEqual(indexer, indexerResponse);
    }

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

    protected Indexer getIndexer(String indexerName) {
        return client.getIndexer(indexerName);
    }

    protected Indexer createIndexer(Indexer indexer,
                                    AccessCondition accessCondition,
                                    RequestOptions requestOptions) {
        return client.createOrUpdateIndexer(
            indexer,
            accessCondition,
            requestOptions);
    }

    protected Indexer createIndexerWithResponse(Indexer indexer,
                                    AccessCondition accessCondition,
                                    RequestOptions requestOptions) {
        return client.createOrUpdateIndexerWithResponse(
            indexer,
            accessCondition,
            requestOptions,
            Context.NONE)
            .getValue();
    }

    protected Response<Void> deleteIndexer(String indexerName,
                                           AccessCondition accessCondition,
                                           RequestOptions requestOptions) {
        return client.deleteIndexerWithResponse(indexerName,
            accessCondition,
            requestOptions,
            Context.NONE);
    }

    protected Response<Void> deleteIndexer(String indexerName) {
        return deleteIndexer(indexerName, null, null);
    }

    @Override
    public void createIndexerReturnsCorrectDefinition() {
        Indexer expectedIndexer =
            createTestIndexer("indexer")
                .setIsDisabled(true)
                .setDataSourceName(SQL_DATASOURCE_NAME)
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

    @Override
    public void createOrUpdateIndexerIfNotExistsFailsOnExistingResource() {
        // Prepare datasource and index
        AccessConditionTests act = new AccessConditionTests();

        createDatasourceAndIndex();

        act.createOrUpdateIfNotExistsFailsOnExistingResource(
            createOrUpdateFunc,
            newIndexerFunc,
            changeIndexerFunc);
    }

    @Override
    public void canUpdateIndexer() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentDescription();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canUpdateIndexerFieldMapping() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentFieldMapping();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithFieldMapping() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createIndexerWithDifferentFieldMapping()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Override
    public void canUpdateIndexerDisabled() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createDisabledIndexer();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canUpdateIndexerSchedule() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentSchedule();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithSchedule() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createIndexerWithDifferentSchedule()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Override
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createTestIndexer("indexer");

        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Override
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createTestIndexer("indexer");
        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer)
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(updatedExpected);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Override
    public void canUpdateIndexerBlobParams() {
        // Create the needed Azure blob resources and datasource object
        DataSource blobDataSource = createBlobDataSource();

        // Create the datasource within the search service
        createDatasource(blobDataSource);

        // modify the indexer's blob params
        Indexer updatedExpected = createIndexerWithStorageConfig();

        createUpdateAndValidateIndexer(updatedExpected, BLOB_DATASOURCE_NAME);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Override
    public void canCreateIndexerWithBlobParams() {
        // Create the needed Azure blob resources and datasource object
        DataSource blobDataSource = createBlobDataSource();

        // Create the datasource within the search service
        DataSource dataSource = createDatasource(blobDataSource);

        // modify the indexer's blob params
        Indexer indexer = createIndexerWithStorageConfig()
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Override
    public void deleteIndexerIsIdempotent() {
        // Create Datasource
        createDatasourceAndIndex();

        // Create the indexer object
        Indexer indexer = createTestIndexer("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);

        // Try delete before the indexer even exists.
        Response<Void> result = deleteIndexer(indexer.getName());

        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        createIndexer(indexer, null, null);

        // Now delete twice.
        result = deleteIndexer(indexer.getName());
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, result.getStatusCode());

        result = deleteIndexer(indexer.getName());
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());
    }

    @Override
    public void canCreateAndGetIndexer() {
        String indexerName = "indexer";

        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        Indexer indexer = createTestIndexer(indexerName).setDataSourceName(datasource.getName());

        createIndexer(indexer, null, null);

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
        AccessConditionTests act = new AccessConditionTests();

        // Prepare datasource and index
        createDatasourceAndIndex();

        Indexer indexerResult = act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateFunc,
            newIndexerFunc);

        Assert.assertTrue(StringUtils.isNoneEmpty(indexerResult.getETag()));
    }

    @Override
    public void createOrUpdateIndexerWithResponseIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        // Prepare datasource and index
        createDatasourceAndIndex();

        Indexer indexerResult = act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateWithResponseFunc,
            newIndexerFunc);

        Assert.assertTrue(StringUtils.isNoneEmpty(indexerResult.getETag()));
    }

    @Override
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests act = new AccessConditionTests();

        // Prepare datasource and index
        createDatasourceAndIndex();

        String indexerName = "name";
        act.deleteIfExistsWorksOnlyWhenResourceExists(
            deleteIndexerFunc,
            createOrUpdateFunc,
            newIndexerFunc,
            indexerName);
    }

    @Override
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();

        // Prepare datasource and index
        createDatasourceAndIndex();

        String indexerName = "name";
        act.deleteIfNotChangedWorksOnlyOnCurrentResource(
            deleteIndexerFunc,
            newIndexerFunc,
            createOrUpdateFunc,
            indexerName);
    }

    @Override
    public void updateIndexerIfExistsFailsOnNoResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsFailsOnNoResource(
            newIndexerFunc,
            createOrUpdateFunc);
    }

    @Override
    public void updateIndexerIfExistsSucceedsOnExistingResource() throws NoSuchFieldException, IllegalAccessException {
        // Prepare datasource and index
        createDatasourceAndIndex();

        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsSucceedsOnExistingResource(
            newIndexerFunc,
            createOrUpdateFunc,
            changeIndexerFunc);
    }

    @Override
    public void updateIndexerIfNotChangedFailsWhenResourceChanged() throws NoSuchFieldException, IllegalAccessException {
        // Prepare datasource and index
        createDatasourceAndIndex();

        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedFailsWhenResourceChanged(
            newIndexerFunc,
            createOrUpdateFunc,
            changeIndexerFunc);
    }

    @Override
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged() throws NoSuchFieldException, IllegalAccessException {
        // Prepare datasource and index
        createDatasourceAndIndex();

        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchanged(
            newIndexerFunc,
            createOrUpdateFunc,
            changeIndexerFunc);
    }

    @Override
    public void canUpdateIndexerSkillset() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

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
        DataSource dataSource = createTestSqlDataSource();
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

        Assert.assertTrue(client.indexerExists(indexer.getName()));
    }

    @Override
    public void existsReturnsFalseForNonExistingIndexer() {
        Assert.assertFalse(client.indexerExists("invalidindex"));
    }
}
