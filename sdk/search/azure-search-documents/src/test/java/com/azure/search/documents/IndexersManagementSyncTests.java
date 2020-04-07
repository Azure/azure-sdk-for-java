// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.FieldMapping;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.Indexer;
import com.azure.search.documents.models.IndexerExecutionInfo;
import com.azure.search.documents.models.IndexerExecutionResult;
import com.azure.search.documents.models.IndexerExecutionStatus;
import com.azure.search.documents.models.IndexerLimits;
import com.azure.search.documents.models.IndexerStatus;
import com.azure.search.documents.models.IndexingParameters;
import com.azure.search.documents.models.IndexingSchedule;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OcrSkill;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.Skill;
import com.azure.search.documents.models.Skillset;
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

    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<Indexer, AccessOptions, Indexer> createOrUpdateIndexerFunc =
        (Indexer indexer, AccessOptions ac) ->
            createOrUpdateIndexer(indexer, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Indexer> newIndexerFunc =
        () -> createBaseTestIndexerObject("name").setDataSourceName(SQL_DATASOURCE_NAME);

    private Function<Indexer, Indexer> mutateIndexerFunc =
        (Indexer indexer) -> indexer.setDescription("ABrandNewDescription");

    private BiConsumer<String, AccessOptions> deleteIndexerFunc =
        (String name, AccessOptions ac) ->
            client.deleteIndexerWithResponse(name, ac.getAccessCondition(), ac.getRequestOptions(), Context.NONE);


    private void createDataSourceAndIndex() {
        // Create DataSource
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        client.createIndex(index);
    }

    private List<Indexer> prepareIndexersForCreateAndListIndexers() {
        // Create DataSource and Index
        createDataSourceAndIndex();

        // Create two indexers
        Indexer indexer1 = createBaseTestIndexerObject("indexer1").setDataSourceName(SQL_DATASOURCE_NAME);
        Indexer indexer2 = createBaseTestIndexerObject("indexer2").setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexer(indexer1);
        client.createIndexer(indexer2);

        return Arrays.asList(indexer1, indexer2);
    }

    private Indexer createTestDataSourceAndIndexer() {
        // Create DataSource and Index
        createDataSourceAndIndex();

        // Create the indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer")
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
        Index index = createTestIndexForLiveDatasource();
        client.createIndex(index);

        Indexer initial = createBaseTestIndexerObject("indexer")
            .setDataSourceName(dataSourceName)
            .setIsDisabled(true);

        // create this indexer in the service
        client.createIndexer(initial);

        // update the indexer in the service
        Indexer indexerResponse = client.createOrUpdateIndexer(updatedIndexer);

        // verify the returned updated indexer is as expected
        setSameStartTime(updatedIndexer, indexerResponse);
        assertObjectEquals(updatedIndexer, indexerResponse, true, "etag");
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    private void createAndValidateIndexer(Indexer indexer) {
        // Create an index
        Index index = createTestIndexForLiveDatasource();
        client.createIndex(index);

        // create this indexer in the service
        Indexer indexerResponse = client.createIndexer(indexer);

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertObjectEquals(indexer, indexerResponse, true, "etag");
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    private Indexer createOrUpdateIndexer(Indexer indexer, MatchConditions accessCondition,
        RequestOptions requestOptions) {
        return client.createOrUpdateIndexerWithResponse(
            indexer, accessCondition, requestOptions, Context.NONE)
            .getValue();
    }

    @Test
    public void createIndexerReturnsCorrectDefinition() {
        Indexer expectedIndexer =
            createBaseTestIndexerObject("indexer")
                .setIsDisabled(true)
                .setDataSourceName(SQL_DATASOURCE_NAME)
                .setParameters(
                    new IndexingParameters()
                        .setBatchSize(50)
                        .setMaxFailedItems(10)
                        .setMaxFailedItemsPerBatch(10));

        Indexer actualIndexer = client.createIndexer(expectedIndexer);

        expectedIndexer.setParameters(new IndexingParameters()
            .setConfiguration(Collections.emptyMap()));
        setSameStartTime(expectedIndexer, actualIndexer);

        assertObjectEquals(expectedIndexer, actualIndexer, true, "etag");
    }

    @Test
    public void canCreateAndListIndexers() {

        // Create the data source, note it a valid DS with actual
        // connection string
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        client.createIndex(index);

        // Create two indexers
        Indexer indexer1 = createBaseTestIndexerObject("indexer1")
            .setDataSourceName(dataSource.getName());
        Indexer indexer2 = createBaseTestIndexerObject("indexer2")
            .setDataSourceName(dataSource.getName());
        client.createIndexer(indexer1);
        client.createIndexer(indexer2);

        Iterator<Indexer> indexers = client.listIndexers().iterator();

        assertObjectEquals(indexer1, indexers.next(), true, "etag");
        assertObjectEquals(indexer2, indexers.next(), true, "etag");
        assertFalse(indexers.hasNext());
    }

    @Test
    public void canCreateAndListIndexerNames() {
        List<Indexer> indexers = prepareIndexersForCreateAndListIndexers();

        Iterator<Indexer> indexersRes = client.listIndexers("name", generateRequestOptions(), Context.NONE).iterator();

        Indexer actualIndexer = indexersRes.next();
        assertEquals(indexers.get(0).getName(), actualIndexer.getName());
        assertAllIndexerFieldsNullExceptName(actualIndexer);

        actualIndexer = indexersRes.next();
        assertEquals(indexers.get(1).getName(), actualIndexer.getName());
        assertAllIndexerFieldsNullExceptName(actualIndexer);

        assertFalse(indexersRes.hasNext());
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        Indexer indexer = createBaseTestIndexerObject("indexer")
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
        assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusWithResponse() {
        Indexer indexer = createTestDataSourceAndIndexer();

        client.resetIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        IndexerExecutionInfo indexerStatusResponse = client.getIndexerStatusWithResponse(indexer.getName(),
            generateRequestOptions(), Context.NONE).getValue();
        assertEquals(IndexerStatus.RUNNING, indexerStatusResponse.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatusResponse.getLastResult().getStatus());
    }

    @Test
    public void canRunIndexer() {
        Indexer indexer = createTestDataSourceAndIndexer();
        client.runIndexer(indexer.getName());

        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerWithResponse() {
        Indexer indexer = createTestDataSourceAndIndexer();
        Response<Void> response = client.runIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());

        assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerAndGetIndexerStatus() {
        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        client = getSearchServiceClientBuilderWithHttpPipelinePolicies(
            Collections.singletonList(MOCK_STATUS_PIPELINE_POLICY))
            .buildClient();

        createDataSourceAndIndex();

        Indexer indexer = createBaseTestIndexerObject("indexer")
            .setDataSourceName(SQL_DATASOURCE_NAME);

        client.createIndexer(indexer);

        IndexerExecutionInfo indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());

        Response<Void> indexerRunResponse = client.runIndexerWithResponse(indexer.getName(), new RequestOptions(),
            Context.NONE);
        assertEquals(HttpResponseStatus.ACCEPTED.code(), indexerRunResponse.getStatusCode());

        indexerExecutionInfo = client.getIndexerStatus(indexer.getName());

        assertValidIndexerExecutionInfo(indexerExecutionInfo);
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsFailsOnExistingResource() {
        // Prepare data source and index
        createDataSourceAndIndex();

        AccessConditionTests.createOrUpdateIfNotExistsFailsOnExistingResource(
            createOrUpdateIndexerFunc,
            newIndexerFunc,
            mutateIndexerFunc);
    }

    @Test
    public void canUpdateIndexer() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentDescription();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerFieldMapping() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentFieldMapping();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithFieldMapping() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createIndexerWithDifferentFieldMapping()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerDisabled() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createDisabledIndexer();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerSchedule() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer updatedExpected = createIndexerWithDifferentSchedule();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithSchedule() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createIndexerWithDifferentSchedule()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createBaseTestIndexerObject("indexer");

        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Indexer indexer = createBaseTestIndexerObject("indexer");
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
        createDataSourceAndIndex();
        Indexer indexer = createBaseTestIndexerObject("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexer(indexer);

        client.deleteIndexer(indexer.getName());
        assertThrows(HttpResponseException.class, () -> client.getIndexer(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponse() {
        createDataSourceAndIndex();
        Indexer indexer = createBaseTestIndexerObject("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        client.createIndexerWithResponse(indexer, new RequestOptions(), Context.NONE);

        client.deleteIndexerWithResponse(indexer.getName(), new MatchConditions(), new RequestOptions(), Context.NONE);
        assertThrows(HttpResponseException.class, () -> client.getIndexer(indexer.getName()));
    }

    @Test
    public void deleteIndexerIsIdempotent() {
        // Create Datasource
        createDataSourceAndIndex();

        // Create the indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);

        // Try delete before the indexer even exists.
        Response<Void> result = client.deleteIndexerWithResponse(
            indexer.getName(), new MatchConditions(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        client.createIndexer(indexer);

        // Now delete twice.
        result = client.deleteIndexerWithResponse(
            indexer.getName(), new MatchConditions(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());

        result = client.deleteIndexerWithResponse(
            indexer.getName(), new MatchConditions(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void canCreateAndGetIndexer() {
        String indexerName = "indexer";
        DataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);

        Index index = createTestIndexForLiveDatasource();
        client.createIndex(index);

        Indexer indexer = createBaseTestIndexerObject(indexerName)
            .setDataSourceName(dataSource.getName());

        client.createIndexer(indexer);
        Indexer indexerResult = client.getIndexer(indexerName);
        assertObjectEquals(indexer, indexerResult, true, "etag");

        indexerResult = client.getIndexerWithResponse(indexerName, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(indexer, indexerResult, true, "etag");
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
        // Prepare data source and index
        createDataSourceAndIndex();

        Indexer indexerResult = AccessConditionTests
            .createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateIndexerFunc, newIndexerFunc);

        assertFalse(CoreUtils.isNullOrEmpty(indexerResult.getETag()));
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        // Prepare data source and index
        createDataSourceAndIndex();

        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteIndexerFunc, createOrUpdateIndexerFunc,
            newIndexerFunc, "name");
    }

    @Test
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() {
        // Prepare data source and index
        createDataSourceAndIndex();

        AccessConditionTests.deleteIfNotChangedWorksOnlyOnCurrentResource(deleteIndexerFunc, newIndexerFunc,
            createOrUpdateIndexerFunc, "name");
    }

    @Test
    public void updateIndexerIfExistsFailsOnNoResource() {
        AccessConditionTests.updateIfExistsFailsOnNoResource(newIndexerFunc, createOrUpdateIndexerFunc);
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
        DataSource dataSource = createTestSqlDataSourceObject();
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
        DataSource dataSource = createTestSqlDataSourceObject();
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

    /**
     * Create a new valid skillset object
     * @return the newly created skillset object
     */
    Skillset createSkillsetObject() {
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

        List<Skill> skills = Collections.singletonList(
            new OcrSkill()
                .setShouldDetectOrientation(true)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext("/document")
                .setInputs(inputs)
                .setOutputs(outputs)
        );
        return new Skillset()
            .setName("ocr-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Indexer createBaseTestIndexerObject(String indexerName) {
        return new Indexer()
            .setName(indexerName)
            .setTargetIndexName(IndexersManagementSyncTests.TARGET_INDEX_NAME)
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }

    /**
     * This index contains fields that are declared on the live data source
     * we use to test the indexers
     *
     * @return the newly created Index object
     */
    Index createTestIndexForLiveDatasource() {
        return new Index()
            .setName(IndexersManagementSyncTests.TARGET_INDEX_NAME)
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


    /**
     * Create a new indexer and change its description property
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentDescription() {
        // create a new indexer object with a modified description
        return createBaseTestIndexerObject("indexer")
            .setDescription("somethingdifferent");
    }

    /**
     * Create a new indexer and change its field mappings property
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentFieldMapping() {
        // create a new indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer");

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
    Indexer createDisabledIndexer() {
        // create a new indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer");

        // modify it
        indexer.setIsDisabled(false);

        return indexer;
    }

    /**
     * Create a new indexer and change its schedule property
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentSchedule() {
        // create a new indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer");

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
    Indexer createIndexerWithDifferentSkillset(String skillsetName) {
        // create a new indexer object
        return createBaseTestIndexerObject("indexer")
            .setSkillsetName(skillsetName);
    }

    /**
     * Create a new indexer and change its indexing parameters
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentIndexingParameters(Indexer indexer) {
        // create a new indexer object
        IndexingParameters ip = new IndexingParameters()
            .setMaxFailedItems(121)
            .setMaxFailedItemsPerBatch(11)
            .setBatchSize(20);

        // modify the indexer
        indexer.setParameters(ip);

        return indexer;
    }

    Indexer createIndexerWithStorageConfig() {
        // create an indexer object
        Indexer updatedExpected =
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

    void setSameStartTime(Indexer expected, Indexer actual) {
        // There ought to be a start time in the response; We just can't know what it is because it would
        // make the test timing-dependent.
        expected.getSchedule().setStartTime(actual.getSchedule().getStartTime());
    }

    void assertAllIndexerFieldsNullExceptName(Indexer indexer) {
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

    void assertValidIndexerExecutionInfo(IndexerExecutionInfo indexerExecutionInfo) {
        assertEquals(IndexerExecutionStatus.IN_PROGRESS, indexerExecutionInfo.getLastResult().getStatus());
        assertEquals(3, indexerExecutionInfo.getExecutionHistory().size());

        IndexerLimits limits = indexerExecutionInfo.getLimits();
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
