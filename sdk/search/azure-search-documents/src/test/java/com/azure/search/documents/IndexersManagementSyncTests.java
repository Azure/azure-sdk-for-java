// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SearchIndexerDataSourceClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerSkillsetClient;
import com.azure.search.documents.models.FieldMapping;
import com.azure.search.documents.models.IndexerExecutionResult;
import com.azure.search.documents.models.IndexerExecutionStatus;
import com.azure.search.documents.models.IndexerStatus;
import com.azure.search.documents.models.IndexingParameters;
import com.azure.search.documents.models.IndexingSchedule;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OcrSkill;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchField;
import com.azure.search.documents.models.SearchFieldDataType;
import com.azure.search.documents.models.SearchIndex;
import com.azure.search.documents.models.SearchIndexer;
import com.azure.search.documents.models.SearchIndexerDataSource;
import com.azure.search.documents.models.SearchIndexerLimits;
import com.azure.search.documents.models.SearchIndexerSkill;
import com.azure.search.documents.models.SearchIndexerSkillset;
import com.azure.search.documents.models.SearchIndexerStatus;
import com.azure.search.documents.test.AccessConditionTests;
import com.azure.search.documents.test.AccessOptions;
import com.azure.search.documents.test.CustomQueryPipelinePolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IndexersManagementSyncTests extends SearchServiceTestBase {
    private static final String TARGET_INDEX_NAME = "indexforindexers";
    private static final HttpPipelinePolicy MOCK_STATUS_PIPELINE_POLICY =
        new CustomQueryPipelinePolicy("mock_status", "inProgress");

    private SearchServiceClient serviceClient;
    private SearchIndexerDataSourceClient dataSourceClient;
    private SearchIndexerClient searchIndexerClient;
    private SearchIndexClient searchIndexClient;
    private SearchIndexerSkillsetClient searchSkillsetClient;

    // commonly used lambda definitions
    private BiFunction<SearchIndexer, AccessOptions, SearchIndexer> createOrUpdateIndexerFunc =
        (SearchIndexer indexer, AccessOptions ac) ->
            createOrUpdateIndexer(indexer, ac.getOnlyIfUnchanged(), ac.getRequestOptions());

    private Supplier<SearchIndexer> newIndexerFunc =
        () -> createBaseTestIndexerObject("name").setDataSourceName(SQL_DATASOURCE_NAME);

    private Function<SearchIndexer, SearchIndexer> mutateIndexerFunc =
        (SearchIndexer indexer) -> indexer.setDescription("ABrandNewDescription");

    private BiConsumer<SearchIndexer, AccessOptions> deleteIndexerFunc =
        (SearchIndexer indexer, AccessOptions ac) ->
            searchIndexerClient.deleteIndexerWithResponse(indexer, ac.getOnlyIfUnchanged(),
                ac.getRequestOptions(), Context.NONE);


    private void createDataSourceAndIndex() {
        // Create DataSource
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        // Create an index
        SearchIndex index = createTestIndexForLiveDatasource();
        searchIndexClient.createIndex(index);
    }

    private List<SearchIndexer> prepareIndexersForCreateAndListIndexers() {
        // Create SearchIndexerDataSource and Index
        createDataSourceAndIndex();

        // Create two indexers
        SearchIndexer indexer1 = createBaseTestIndexerObject("indexer1").setDataSourceName(SQL_DATASOURCE_NAME);
        SearchIndexer indexer2 = createBaseTestIndexerObject("indexer2").setDataSourceName(SQL_DATASOURCE_NAME);
        searchIndexerClient.createIndexer(indexer1);
        searchIndexerClient.createIndexer(indexer2);

        return Arrays.asList(indexer1, indexer2);
    }

    private SearchIndexer createTestDataSourceAndIndexer() {
        // Create SearchIndexerDataSource and Index
        createDataSourceAndIndex();

        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject("indexer")
            .setDataSourceName(SQL_DATASOURCE_NAME);
        searchIndexerClient.createIndexer(indexer);

        return indexer;
    }

    /**
     * Creates the index and indexer in the search service and then update the indexer
     *
     * @param updatedIndexer the indexer to be updated
     * @param dataSourceName the data source name for this indexer
     */
    private void createUpdateAndValidateIndexer(SearchIndexer updatedIndexer, String dataSourceName) {
        updatedIndexer.setDataSourceName(dataSourceName);

        // Create an index
        SearchIndex index = createTestIndexForLiveDatasource();
        searchIndexClient.createIndex(index);

        SearchIndexer initial = createBaseTestIndexerObject("indexer")
            .setDataSourceName(dataSourceName)
            .setIsDisabled(true);

        // create this indexer in the service
        searchIndexerClient.createIndexer(initial);

        // update the indexer in the service
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updatedIndexer);

        // verify the returned updated indexer is as expected
        setSameStartTime(updatedIndexer, indexerResponse);
        assertObjectEquals(updatedIndexer, indexerResponse, true, "etag");
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    private void createAndValidateIndexer(SearchIndexer indexer) {
        // Create an index
        SearchIndex index = createTestIndexForLiveDatasource();
        searchIndexClient.createIndex(index);

        // create this indexer in the service
        SearchIndexer indexerResponse = searchIndexerClient.createIndexer(indexer);

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertObjectEquals(indexer, indexerResponse, true, "etag");
    }

    private SearchIndexer createOrUpdateIndexer(SearchIndexer indexer, Boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        return searchIndexerClient.createOrUpdateIndexerWithResponse(
            indexer, onlyIfUnchanged, requestOptions, Context.NONE)
            .getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        serviceClient = getSearchServiceClientBuilder().buildClient();
        dataSourceClient = serviceClient.getSearchIndexerDataSourceClient();
        searchIndexClient = serviceClient.getSearchIndexClient();
        searchIndexerClient = serviceClient.getSearchIndexerClient();
        searchSkillsetClient = serviceClient.getSearchIndexerSkillsetClient();
    }

    @Test
    public void createIndexerReturnsCorrectDefinition() {
        SearchIndexer expectedIndexer =
            createBaseTestIndexerObject("indexer")
                .setIsDisabled(true)
                .setDataSourceName(SQL_DATASOURCE_NAME)
                .setParameters(
                    new IndexingParameters()
                        .setBatchSize(50)
                        .setMaxFailedItems(10)
                        .setMaxFailedItemsPerBatch(10));

        SearchIndexer actualIndexer = searchIndexerClient.createIndexer(expectedIndexer);

        expectedIndexer.setParameters(new IndexingParameters()
            .setConfiguration(Collections.emptyMap()));
        setSameStartTime(expectedIndexer, actualIndexer);

        assertObjectEquals(expectedIndexer, actualIndexer, true, "etag");
    }

    @Test
    public void canCreateAndListIndexers() {

        // Create the data source, note it a valid DS with actual
        // connection string
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        // Create an index
        SearchIndex index = createTestIndexForLiveDatasource();
        searchIndexClient.createIndex(index);

        // Create two indexers
        SearchIndexer indexer1 = createBaseTestIndexerObject("indexer1")
            .setDataSourceName(dataSource.getName());
        SearchIndexer indexer2 = createBaseTestIndexerObject("indexer2")
            .setDataSourceName(dataSource.getName());
        searchIndexerClient.createIndexer(indexer1);
        searchIndexerClient.createIndexer(indexer2);

        Iterator<SearchIndexer> indexers = searchIndexerClient.listIndexers().iterator();

        assertObjectEquals(indexer1, indexers.next(), true, "etag");
        assertObjectEquals(indexer2, indexers.next(), true, "etag");
        assertFalse(indexers.hasNext());
    }

    @Test
    public void canCreateAndListIndexerNames() {
        List<SearchIndexer> indexers = prepareIndexersForCreateAndListIndexers();

        Iterator<SearchIndexer> indexersRes = searchIndexerClient.listIndexers("name", generateRequestOptions(), Context.NONE).iterator();

        SearchIndexer actualIndexer = indexersRes.next();
        assertEquals(indexers.get(0).getName(), actualIndexer.getName());
        assertAllIndexerFieldsNullExceptName(actualIndexer);

        actualIndexer = indexersRes.next();
        assertEquals(indexers.get(1).getName(), actualIndexer.getName());
        assertAllIndexerFieldsNullExceptName(actualIndexer);

        assertFalse(indexersRes.hasNext());
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        SearchIndexer indexer = createBaseTestIndexerObject("indexer")
            .setDataSourceName("thisdatasourcedoesnotexist");

        assertHttpResponseException(
            () -> searchIndexerClient.createIndexer(indexer),
            HttpResponseStatus.BAD_REQUEST,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }

    @Test
    public void canResetIndexerAndGetIndexerStatus() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        searchIndexerClient.resetIndexer(indexer.getName());
        SearchIndexerStatus indexerStatus = searchIndexerClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusWithResponse() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        searchIndexerClient.resetIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        SearchIndexerStatus indexerStatusResponse = searchIndexerClient.getIndexerStatusWithResponse(indexer.getName(),
            generateRequestOptions(), Context.NONE).getValue();
        assertEquals(IndexerStatus.RUNNING, indexerStatusResponse.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatusResponse.getLastResult().getStatus());
    }

    @Test
    public void canRunIndexer() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        searchIndexerClient.runIndexer(indexer.getName());

        SearchIndexerStatus indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerWithResponse() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        Response<Void> response = searchIndexerClient.runIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        SearchIndexerStatus indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());

        assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerAndGetIndexerStatus() {
        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        serviceClient = getSearchServiceClientBuilderWithHttpPipelinePolicies(
            Collections.singletonList(MOCK_STATUS_PIPELINE_POLICY))
            .buildClient();
        searchIndexClient = serviceClient.getSearchIndexClient();
        dataSourceClient = serviceClient.getSearchIndexerDataSourceClient();
        searchIndexerClient = serviceClient.getSearchIndexerClient();

        createDataSourceAndIndex();

        SearchIndexer indexer = createBaseTestIndexerObject("indexer")
            .setDataSourceName(SQL_DATASOURCE_NAME);

        searchIndexerClient.createIndexer(indexer);

        SearchIndexerStatus indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());

        Response<Void> indexerRunResponse = searchIndexerClient.runIndexerWithResponse(indexer.getName(), new RequestOptions(),
            Context.NONE);
        assertEquals(HttpResponseStatus.ACCEPTED.code(), indexerRunResponse.getStatusCode());

        indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());

        assertValidIndexerExecutionInfo(indexerExecutionInfo);
    }

    @Test
    public void canUpdateIndexer() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer updatedExpected = createIndexerWithDifferentDescription();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerFieldMapping() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer updatedExpected = createIndexerWithDifferentFieldMapping();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithFieldMapping() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer indexer = createIndexerWithDifferentFieldMapping()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerDisabled() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer updatedExpected = createDisabledIndexer();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerSchedule() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer updatedExpected = createIndexerWithDifferentSchedule();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithSchedule() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer indexer = createIndexerWithDifferentSchedule()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer indexer = createBaseTestIndexerObject("indexer");

        SearchIndexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndexer indexer = createBaseTestIndexerObject("indexer");
        SearchIndexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer)
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(updatedExpected);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canUpdateIndexerBlobParams() {
        // Create the needed Azure blob resources and data source object
        SearchIndexerDataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        dataSourceClient.createOrUpdateDataSource(blobDataSource);

        // modify the indexer's blob params
        SearchIndexer updatedExpected = createIndexerWithStorageConfig();

        createUpdateAndValidateIndexer(updatedExpected, BLOB_DATASOURCE_NAME);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canCreateIndexerWithBlobParams() {
        // Create the needed Azure blob resources and data source object
        SearchIndexerDataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        SearchIndexerDataSource dataSource = dataSourceClient.createOrUpdateDataSource(blobDataSource);

        // modify the indexer's blob params
        SearchIndexer indexer = createIndexerWithStorageConfig()
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canCreateAndDeleteIndexer() {
        createDataSourceAndIndex();
        SearchIndexer indexer = createBaseTestIndexerObject("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        searchIndexerClient.createIndexer(indexer);

        searchIndexerClient.deleteIndexer(indexer.getName());
        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponse() {
        createDataSourceAndIndex();
        SearchIndexer indexer = createBaseTestIndexerObject("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        searchIndexerClient.createIndexerWithResponse(indexer, new RequestOptions(), Context.NONE);

        searchIndexerClient.deleteIndexerWithResponse(indexer, false, new RequestOptions(), Context.NONE);
        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer(indexer.getName()));
    }

    @Test
    public void deleteIndexerIsIdempotent() {
        // Create Datasource
        createDataSourceAndIndex();

        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);

        // Try delete before the indexer even exists.
        Response<Void> result = searchIndexerClient.deleteIndexerWithResponse(
            indexer, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        searchIndexerClient.createIndexer(indexer);

        // Now delete twice.
        result = searchIndexerClient.deleteIndexerWithResponse(indexer, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());

        result = searchIndexerClient.deleteIndexerWithResponse(indexer, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void canCreateAndGetIndexer() {
        String indexerName = "indexer";
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        SearchIndex index = createTestIndexForLiveDatasource();
        searchIndexClient.createIndex(index);

        SearchIndexer indexer = createBaseTestIndexerObject(indexerName)
            .setDataSourceName(dataSource.getName());

        searchIndexerClient.createIndexer(indexer);
        SearchIndexer indexerResult = searchIndexerClient.getIndexer(indexerName);
        assertObjectEquals(indexer, indexerResult, true, "etag");

        indexerResult = searchIndexerClient.getIndexerWithResponse(indexerName, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(indexer, indexerResult, true, "etag");
    }

    @Test
    public void getIndexerThrowsOnNotFound() {
        assertHttpResponseException(
            () -> searchIndexerClient.getIndexer("thisindexerdoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "Indexer 'thisindexerdoesnotexist' was not found");
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResource() {
        // Prepare data source and index
        createDataSourceAndIndex();

        SearchIndexer indexerResult = AccessConditionTests
            .createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateIndexerFunc, newIndexerFunc);

        assertFalse(CoreUtils.isNullOrEmpty(indexerResult.getETag()));
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        // Prepare data source and index
        createDataSourceAndIndex();

        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteIndexerFunc, createOrUpdateIndexerFunc,
            newIndexerFunc);
    }

    @Test
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() {
        // Prepare data source and index
        createDataSourceAndIndex();

        AccessConditionTests.deleteIfNotChangedWorksOnlyOnCurrentResource(deleteIndexerFunc, newIndexerFunc,
            createOrUpdateIndexerFunc, "name");
    }

    @Test
    public void updateIndexerIfExistsSucceedsOnExistingResource() {
        // Prepare datasource and index
        createDataSourceAndIndex();

        AccessConditionTests.updateIfExistsSucceedsOnExistingResource(newIndexerFunc, createOrUpdateIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void updateIndexerIfNotChangedFailsWhenResourceChanged() {
        // Prepare datasource and index
        createDataSourceAndIndex();

        AccessConditionTests.updateIfNotChangedFailsWhenResourceChanged(newIndexerFunc, createOrUpdateIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged() {
        // Prepare datasource and index
        createDataSourceAndIndex();

        AccessConditionTests.updateIfNotChangedSucceedsWhenResourceUnchanged(newIndexerFunc, createOrUpdateIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void canUpdateIndexerSkillset() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        // Create a new skillset object
        // todo: task 1544 - change all over the code that that the object creation and actual service creation will
        // have meaningful and differentiated names
        SearchIndexerSkillset skillset = createSkillsetObject();

        // create the skillset in the search service
        searchSkillsetClient.createSkillset(skillset);
        SearchIndexer updatedExpected = createIndexerWithDifferentSkillset(skillset.getName());
        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithSkillset() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSourceClient.createOrUpdateDataSource(dataSource);

        // Create a new skillset object
        // todo: task 1544 - change all over the code that that the object creation and actual service creation will
        // have meaningful and differentiated names
        SearchIndexerSkillset skillset = createSkillsetObject();

        // create the skillset in the search service
        searchSkillsetClient.createSkillset(skillset);

        SearchIndexer indexer = createIndexerWithDifferentSkillset(skillset.getName())
            .setDataSourceName(dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    /**
     * Create a new valid skillset object
     * @return the newly created skillset object
     */
    SearchIndexerSkillset createSkillsetObject() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry()
                .setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill()
                .setShouldDetectOrientation(true)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext("/document")
                .setInputs(inputs)
                .setOutputs(outputs)
        );
        return new SearchIndexerSkillset()
            .setName("ocr-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexer createBaseTestIndexerObject(String indexerName) {
        return new SearchIndexer()
            .setName(indexerName)
            .setTargetIndexName(IndexersManagementSyncTests.TARGET_INDEX_NAME)
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }

    /**
     * This index contains fields that are declared on the live data source
     * we use to test the indexers
     *
     * @return the newly created SearchIndex object
     */
    SearchIndex createTestIndexForLiveDatasource() {
        return new SearchIndex()
            .setName(IndexersManagementSyncTests.TARGET_INDEX_NAME)
            .setFields(Arrays.asList(
                new SearchField()
                    .setName("county_name")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.FALSE)
                    .setFilterable(Boolean.TRUE),
                new SearchField()
                    .setName("state")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE),
                new SearchField()
                    .setName("feature_id")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)));
    }


    /**
     * Create a new indexer and change its description property
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentDescription() {
        // create a new indexer object with a modified description
        return createBaseTestIndexerObject("indexer")
            .setDescription("somethingdifferent");
    }

    /**
     * Create a new indexer and change its field mappings property
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentFieldMapping() {
        // create a new indexer object
        SearchIndexer indexer = createBaseTestIndexerObject("indexer");

        // Create field mappings
        List<FieldMapping> fieldMappings = Collections.singletonList(new FieldMapping()
            .setSourceFieldName("state_alpha")
            .setTargetFieldName("state"));

        // modify the indexer
        indexer.setFieldMappings(fieldMappings);

        return indexer;
    }

    /**
     * Create a new indexer and set the Disabled property to true
     *
     * @return the created indexer
     */
    SearchIndexer createDisabledIndexer() {
        // create a new indexer object
        SearchIndexer indexer = createBaseTestIndexerObject("indexer");

        // modify it
        indexer.setIsDisabled(false);

        return indexer;
    }

    /**
     * Create a new indexer and change its schedule property
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentSchedule() {
        // create a new indexer object
        SearchIndexer indexer = createBaseTestIndexerObject("indexer");

        IndexingSchedule is = new IndexingSchedule()
            .setInterval(Duration.ofMinutes(10));

        // modify the indexer
        indexer.setSchedule(is);

        return indexer;
    }

    /**
     * Create a new indexer and change its skillset
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentSkillset(String skillsetName) {
        // create a new indexer object
        return createBaseTestIndexerObject("indexer")
            .setSkillsetName(skillsetName);
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

    SearchIndexer createIndexerWithStorageConfig() {
        // create an indexer object
        SearchIndexer updatedExpected =
            createBaseTestIndexerObject("indexer");

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

    void assertAllIndexerFieldsNullExceptName(SearchIndexer indexer) {
        assertNull(indexer.getParameters());
        assertNull(indexer.getDataSourceName());
        assertNull(indexer.getDescription());
        assertNull(indexer.getETag());
        assertNull(indexer.getFieldMappings());
        assertNull(indexer.getOutputFieldMappings());
        assertNull(indexer.getSchedule());
        assertNull(indexer.getSkillsetName());
        assertNull(indexer.getTargetIndexName());
    }

    void assertStartAndEndTimeValid(IndexerExecutionResult result) {
        assertNotNull(result.getStartTime());
        assertNotEquals(OffsetDateTime.now(), result.getStartTime());
        assertNotNull(result.getEndTime());
        assertNotEquals(OffsetDateTime.now(), result.getEndTime());
    }

    void assertValidIndexerExecutionInfo(SearchIndexerStatus indexerExecutionInfo) {
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
