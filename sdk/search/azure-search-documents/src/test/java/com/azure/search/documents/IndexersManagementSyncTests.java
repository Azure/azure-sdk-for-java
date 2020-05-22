// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
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
import com.azure.search.documents.models.SearchErrorException;
import com.azure.search.documents.models.SearchField;
import com.azure.search.documents.models.SearchFieldDataType;
import com.azure.search.documents.models.SearchIndex;
import com.azure.search.documents.models.SearchIndexer;
import com.azure.search.documents.models.SearchIndexerDataSource;
import com.azure.search.documents.models.SearchIndexerLimits;
import com.azure.search.documents.models.SearchIndexerSkill;
import com.azure.search.documents.models.SearchIndexerSkillset;
import com.azure.search.documents.models.SearchIndexerStatus;
import com.azure.search.documents.test.CustomQueryPipelinePolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.generateRequestOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexersManagementSyncTests extends SearchTestBase {
    private static final String TARGET_INDEX_NAME = "indexforindexers";
    private static final HttpPipelinePolicy MOCK_STATUS_PIPELINE_POLICY =
        new CustomQueryPipelinePolicy("mock_status", "inProgress");

    private final List<String> dataSourcesToDelete = new ArrayList<>();
    private final List<String> indexersToDelete = new ArrayList<>();
    private final List<String> indexesToDelete = new ArrayList<>();
    private final List<String> skillsetsToDelete = new ArrayList<>();

    private SearchServiceClient client;

    private String createDataSource() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        client.createOrUpdateDataSource(dataSource);
        dataSourcesToDelete.add(dataSource.getName());

        return dataSource.getName();
    }

    private String createIndex() {
        SearchIndex index = createTestIndexForLiveDatasource();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        return index.getName();
    }

    private SearchIndexer createTestDataSourceAndIndexer() {
        // Create the indexer object
        SearchIndexer  indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        client.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        return indexer;
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     *
     * @param indexer the indexer to be created
     */
    private void createAndValidateIndexer(SearchIndexer indexer) {
        // create this indexer in the service
        SearchIndexer indexerResponse = client.createIndexer(indexer);
        indexersToDelete.add(indexerResponse.getName());

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertObjectEquals(indexer, indexerResponse, true, "etag");
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String skillset : skillsetsToDelete) {
            client.deleteSkillset(skillset);
        }

        for (String dataSource : dataSourcesToDelete) {
            client.deleteDataSource(dataSource);
        }

        for (String indexer : indexersToDelete) {
            client.deleteIndexer(indexer);
        }

        for (String index : indexesToDelete) {
            client.deleteIndex(index);
        }
    }

    @Test
    public void createIndexerReturnsCorrectDefinition() {
        SearchIndexer expectedIndexer = createBaseTestIndexerObject(createIndex(), createDataSource())
            .setIsDisabled(true)
            .setParameters(new IndexingParameters()
                .setBatchSize(50)
                .setMaxFailedItems(10)
                .setMaxFailedItemsPerBatch(10));

        SearchIndexer actualIndexer = client.createIndexer(expectedIndexer);
        indexersToDelete.add(actualIndexer.getName());

        expectedIndexer.setParameters(new IndexingParameters()
            .setConfiguration(Collections.emptyMap()));
        setSameStartTime(expectedIndexer, actualIndexer);

        assertObjectEquals(expectedIndexer, actualIndexer, true, "etag");
    }

    @Test
    public void canCreateAndListIndexers() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        // Create two indexers
        SearchIndexer indexer1 = createBaseTestIndexerObject(indexName, dataSourceName);
        indexer1.setName("a" + indexer1.getName());
        SearchIndexer indexer2 = createBaseTestIndexerObject(indexName, dataSourceName);
        indexer2.setName("b" + indexer2.getName());
        client.createIndexer(indexer1);
        indexersToDelete.add(indexer1.getName());
        client.createIndexer(indexer2);
        indexersToDelete.add(indexer2.getName());

        Iterator<SearchIndexer> indexers = client.listIndexers().iterator();

        SearchIndexer returnedIndexer = indexers.next();
        assertObjectEquals(indexer1, returnedIndexer, true, "etag");
        returnedIndexer = indexers.next();
        assertObjectEquals(indexer2, returnedIndexer, true, "etag");
        assertFalse(indexers.hasNext());
    }

    @Test
    public void canCreateAndListIndexerNames() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer indexer1 = createBaseTestIndexerObject(indexName, dataSourceName);
        indexer1.setName("a" + indexer1.getName());
        SearchIndexer indexer2 = createBaseTestIndexerObject(indexName, dataSourceName);
        indexer2.setName("b" + indexer2.getName());
        client.createIndexer(indexer1);
        indexersToDelete.add(indexer1.getName());
        client.createIndexer(indexer2);
        indexersToDelete.add(indexer2.getName());

        Iterator<SearchIndexer> indexersRes = client.listIndexers("name", generateRequestOptions(), Context.NONE).iterator();

        SearchIndexer actualIndexer = indexersRes.next();
        assertEquals(indexer1.getName(), actualIndexer.getName());
        assertAllIndexerFieldsNullExceptName(actualIndexer);

        actualIndexer = indexersRes.next();
        assertEquals(indexer2.getName(), actualIndexer.getName());
        assertAllIndexerFieldsNullExceptName(actualIndexer);

        assertFalse(indexersRes.hasNext());
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), "thisdatasourcedoesnotexist");

        assertHttpResponseException(
            () -> client.createIndexer(indexer),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "This indexer refers to a data source 'thisdatasourcedoesnotexist' that doesn't exist");
    }

    @Test
    public void canResetIndexerAndGetIndexerStatus() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        client.resetIndexer(indexer.getName());
        SearchIndexerStatus indexerStatus = client.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerStatus.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatus.getLastResult().getStatus());
    }

    @Test
    public void canResetIndexerAndGetIndexerStatusWithResponse() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();

        client.resetIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        SearchIndexerStatus indexerStatusResponse = client.getIndexerStatusWithResponse(indexer.getName(),
            generateRequestOptions(), Context.NONE).getValue();
        assertEquals(IndexerStatus.RUNNING, indexerStatusResponse.getStatus());
        assertEquals(IndexerExecutionStatus.RESET, indexerStatusResponse.getLastResult().getStatus());
    }

    @Test
    public void canRunIndexer() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        client.runIndexer(indexer.getName());

        SearchIndexerStatus indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerWithResponse() {
        SearchIndexer indexer = createTestDataSourceAndIndexer();
        Response<Void> response = client.runIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE);
        SearchIndexerStatus indexerExecutionInfo = client.getIndexerStatus(indexer.getName());

        assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerAndGetIndexerStatus() {
        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        client = getSearchServiceClientBuilder(MOCK_STATUS_PIPELINE_POLICY).buildClient();

        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());

        client.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        SearchIndexerStatus indexerExecutionInfo = client.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());

        Response<Void> indexerRunResponse = client.runIndexerWithResponse(indexer.getName(), new RequestOptions(),
            Context.NONE);
        assertEquals(HttpResponseStatus.ACCEPTED.code(), indexerRunResponse.getStatusCode());

        indexerExecutionInfo = client.getIndexerStatus(indexer.getName());

        assertValidSearchIndexerStatus(indexerExecutionInfo);
    }

    @Test
    public void canUpdateIndexer() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentDescription(indexName, dataSourceName)
            .setName(initial.getName());
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        // verify the returned updated indexer is as expected
        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerFieldMapping() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentFieldMapping(indexName, dataSourceName)
            .setName(initial.getName());
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        // verify the returned updated indexer is as expected
        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canCreateIndexerWithFieldMapping() {
        SearchIndexer indexer = createIndexerWithDifferentFieldMapping(createIndex(), createDataSource());
        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerDisabled() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createDisabledIndexer(indexName, dataSourceName)
            .setName(initial.getName());
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerSchedule() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentSchedule(indexName, dataSourceName)
            .setName(initial.getName());
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canCreateIndexerWithSchedule() {
        SearchIndexer indexer = createIndexerWithDifferentSchedule(createIndex(), createDataSource());
        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentIndexingParameters(initial);
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer updatedExpected = createIndexerWithDifferentIndexingParameters(indexer);

        createAndValidateIndexer(updatedExpected);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canUpdateIndexerBlobParams() {
        String indexName = createIndex();
        String dataSourceName = client.createDataSource(createBlobDataSource()).getName();
        dataSourcesToDelete.add(dataSourceName);

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithStorageConfig(indexName, dataSourceName)
            .setName(initial.getName());
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canCreateIndexerWithBlobParams() {
        // Create the needed Azure blob resources and data source object
        SearchIndexerDataSource blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        SearchIndexerDataSource dataSource = client.createOrUpdateDataSource(blobDataSource);
        dataSourcesToDelete.add(dataSource.getName());

        // modify the indexer's blob params
        SearchIndexer indexer = createIndexerWithStorageConfig(createIndex(), dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canCreateAndDeleteIndexer() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        client.createIndexer(indexer);

        client.deleteIndexer(indexer.getName());
        assertThrows(HttpResponseException.class, () -> client.getIndexer(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponse() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        client.createIndexerWithResponse(indexer, new RequestOptions(), Context.NONE);

        client.deleteIndexerWithResponse(indexer, false, new RequestOptions(), Context.NONE);
        assertThrows(HttpResponseException.class, () -> client.getIndexer(indexer.getName()));
    }

    @Test
    public void deleteIndexerIsIdempotent() {
        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());

        // Try delete before the indexer even exists.
        Response<Void> result = client.deleteIndexerWithResponse(indexer, false, generateRequestOptions(),
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        client.createIndexer(indexer);

        // Now delete twice.
        result = client.deleteIndexerWithResponse(indexer, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());

        result = client.deleteIndexerWithResponse(indexer, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void canCreateAndGetIndexer() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        client.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        SearchIndexer  indexerResult = client.getIndexer(indexer.getName());
        assertObjectEquals(indexer, indexerResult, true, "etag");

        indexerResult = client.getIndexerWithResponse(indexer.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        assertObjectEquals(indexer, indexerResult, true, "etag");
    }

    @Test
    public void getIndexerThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getIndexer("thisindexerdoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "Indexer 'thisindexerdoesnotexist' was not found");
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResource() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer created = client.createOrUpdateIndexerWithResponse(indexer, true, null, Context.NONE)
            .getValue();
        indexersToDelete.add(created.getName());

        assertFalse(CoreUtils.isNullOrEmpty(created.getETag()));
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer created = client.createOrUpdateIndexerWithResponse(indexer, false, null, Context.NONE)
            .getValue();

        client.deleteIndexerWithResponse(created, true, null, Context.NONE);

        // Try to delete again and expect to fail
        try {
            client.deleteIndexerWithResponse(created, true, null, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer stale = client.createOrUpdateIndexerWithResponse(indexer, true, null, Context.NONE)
            .getValue();

        SearchIndexer updated = client.createOrUpdateIndexerWithResponse(stale, false, null, Context.NONE)
            .getValue();

        try {
            client.deleteIndexerWithResponse(stale, true, null, Context.NONE);
            fail("deleteFunc should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteIndexerWithResponse(updated, true, null, Context.NONE);
    }

    @Test
    public void updateIndexerIfExistsSucceedsOnExistingResource() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer original = client.createOrUpdateIndexerWithResponse(indexer, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = client.createOrUpdateIndexerWithResponse(original.setDescription("ABrandNewDescription"),
            false, null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Verify the eTag is not empty and was changed
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfNotChangedFailsWhenResourceChanged() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer original = client.createOrUpdateIndexerWithResponse(indexer, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = client.createOrUpdateIndexerWithResponse(original.setDescription("ABrandNewDescription"),
            true, null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Update and check the eTags were changed
        try {
            client.createOrUpdateIndexerWithResponse(original, true, null, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        // Check eTags
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer original = client.createOrUpdateIndexerWithResponse(indexer, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = client.createOrUpdateIndexerWithResponse(original.setDescription("ABrandNewDescription"),
            true, null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Check eTags as expected
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void canUpdateIndexerSkillset() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        client.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexerSkillset skillset = createSkillsetObject();
        client.createSkillset(skillset);
        skillsetsToDelete.add(skillset.getName());
        SearchIndexer updated = createIndexerWithDifferentSkillset(indexName, dataSourceName, skillset.getName())
            .setName(initial.getName());
        SearchIndexer indexerResponse = client.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canCreateIndexerWithSkillset() {
        SearchIndexerSkillset skillset = client.createSkillset(createSkillsetObject());
        skillsetsToDelete.add(skillset.getName());

        SearchIndexer indexer = createIndexerWithDifferentSkillset(createIndex(), createDataSource(), skillset.getName());

        createAndValidateIndexer(indexer);
    }

    /**
     * Create a new valid skillset object
     *
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
            .setName(testResourceNamer.randomName("ocr-skillset", 32))
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexer createBaseTestIndexerObject(String targetIndexName, String dataSourceName) {
        return new SearchIndexer()
            .setName(testResourceNamer.randomName("indexer", 32))
            .setTargetIndexName(targetIndexName)
            .setDataSourceName(dataSourceName)
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }

    /**
     * This index contains fields that are declared on the live data source we use to test the indexers
     *
     * @return the newly created Index object
     */
    SearchIndex createTestIndexForLiveDatasource() {
        return new SearchIndex()
            .setName(testResourceNamer.randomName(IndexersManagementSyncTests.TARGET_INDEX_NAME, 32))
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
    SearchIndexer createIndexerWithDifferentDescription(String targetIndexName, String dataSourceName) {
        // create a new indexer object with a modified description
        return createBaseTestIndexerObject(targetIndexName, dataSourceName)
            .setDescription("somethingdifferent");
    }

    /**
     * Create a new indexer and change its field mappings property
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentFieldMapping(String targetIndexName, String dataSourceName) {
        // create a new indexer object
        SearchIndexer  indexer = createBaseTestIndexerObject(targetIndexName, dataSourceName);

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
    SearchIndexer createDisabledIndexer(String targetIndexName, String dataSourceName) {
        // create a new indexer object
        SearchIndexer  indexer = createBaseTestIndexerObject(targetIndexName, dataSourceName);

        // modify it
        indexer.setIsDisabled(false);

        return indexer;
    }

    /**
     * Create a new indexer and change its schedule property
     *
     * @return the created indexer
     */
    SearchIndexer createIndexerWithDifferentSchedule(String targetIndexName, String dataSourceName) {
        // create a new indexer object
        SearchIndexer  indexer = createBaseTestIndexerObject(targetIndexName, dataSourceName);

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
    SearchIndexer createIndexerWithDifferentSkillset(String targetIndexName, String dataSourceName, String skillsetName) {
        // create a new indexer object
        return createBaseTestIndexerObject(targetIndexName, dataSourceName)
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

    SearchIndexer createIndexerWithStorageConfig(String targetIndexName, String dataSourceName) {
        // create an indexer object
        SearchIndexer updatedExpected = createBaseTestIndexerObject(targetIndexName, dataSourceName);

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
