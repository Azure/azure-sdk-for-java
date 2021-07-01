// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.SearchTestBase;
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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;

import java.lang.reflect.Field;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexersManagementSyncTests extends SearchTestBase {
    private static final String TARGET_INDEX_NAME = "indexforindexers";
    private static final HttpPipelinePolicy MOCK_STATUS_PIPELINE_POLICY = (context, next) -> {
        String url = context.getHttpRequest().getUrl().toString();
        String separator = url.contains("?") ? "&" : "?";
        context.getHttpRequest()
            .setUrl(url + separator + "mock_status=inProgress");
        return next.process();
    };

    private final List<String> dataSourcesToDelete = new ArrayList<>();
    private final List<String> indexersToDelete = new ArrayList<>();
    private final List<String> indexesToDelete = new ArrayList<>();
    private final List<String> skillsetsToDelete = new ArrayList<>();

    private SearchIndexerClient searchIndexerClient;
    private SearchIndexClient searchIndexClient;

    private String createDataSource() {
        SearchIndexerDataSourceConnection dataSource = createBlobDataSource();
        searchIndexerClient.createOrUpdateDataSourceConnection(dataSource);

        dataSourcesToDelete.add(dataSource.getName());

        return dataSource.getName();
    }

    private String createIndex() {
        SearchIndex index = createTestIndexForLiveDatasource();
        searchIndexClient.createIndex(index);
        indexesToDelete.add(index.getName());

        return index.getName();
    }

    private SearchIndexer createTestDataSourceAndIndexer() {
        // Create the indexer object
        SearchIndexer  indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        searchIndexerClient.createIndexer(indexer);
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
        SearchIndexer indexerResponse = searchIndexerClient.createIndexer(indexer);
        indexersToDelete.add(indexerResponse.getName());

        // verify the returned updated indexer is as expected
        setSameStartTime(indexer, indexerResponse);
        assertObjectEquals(indexer, indexerResponse, true, "etag");
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchIndexerClient = getSearchIndexerClientBuilder().buildClient();
        searchIndexClient = getSearchIndexClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String skillset : skillsetsToDelete) {
            searchIndexerClient.deleteSkillset(skillset);
        }

        for (String dataSource : dataSourcesToDelete) {
            searchIndexerClient.deleteDataSourceConnection(dataSource);
        }

        for (String indexer : indexersToDelete) {
            searchIndexerClient.deleteIndexer(indexer);
        }

        for (String index : indexesToDelete) {
            searchIndexClient.deleteIndex(index);
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

        SearchIndexer actualIndexer = searchIndexerClient.createIndexer(expectedIndexer);
        indexersToDelete.add(actualIndexer.getName());

        setSameStartTime(expectedIndexer, actualIndexer);
        assertObjectEquals(expectedIndexer, actualIndexer, true, "etag");
    }

    @Test
    public void canCreateAndListIndexers() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        // Create two indexers
        SearchIndexer indexer1 = createBaseTestIndexerObject(indexName, dataSourceName);
        mutateName(indexer1, "a" + indexer1.getName());
        SearchIndexer indexer2 = createBaseTestIndexerObject(indexName, dataSourceName);
        mutateName(indexer2, "b" + indexer2.getName());

        searchIndexerClient.createIndexer(indexer1);
        indexersToDelete.add(indexer1.getName());
        searchIndexerClient.createIndexer(indexer2);
        indexersToDelete.add(indexer2.getName());

        Iterator<SearchIndexer> indexers = searchIndexerClient.listIndexers().iterator();

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
        mutateName(indexer1, "a" + indexer1.getName());

        SearchIndexer indexer2 = createBaseTestIndexerObject(indexName, dataSourceName);
        mutateName(indexer2, "b" + indexer2.getName());
        searchIndexerClient.createIndexer(indexer1);
        indexersToDelete.add(indexer1.getName());
        searchIndexerClient.createIndexer(indexer2);
        indexersToDelete.add(indexer2.getName());

        Iterator<String> indexersRes = searchIndexerClient.listIndexerNames(Context.NONE)
            .iterator();

        String actualIndexer = indexersRes.next();
        assertNotNull(actualIndexer);
        assertEquals(indexer1.getName(), actualIndexer);

        actualIndexer = indexersRes.next();
        assertNotNull(actualIndexer);
        assertEquals(indexer2.getName(), actualIndexer);

        assertFalse(indexersRes.hasNext());
    }

    @Test
    public void createIndexerFailsWithUsefulMessageOnUserError() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), "thisdatasourcedoesnotexist");

        assertHttpResponseException(
            () -> searchIndexerClient.createIndexer(indexer),
            HttpURLConnection.HTTP_BAD_REQUEST,
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

        searchIndexerClient.resetIndexerWithResponse(indexer.getName(), Context.NONE);
        SearchIndexerStatus indexerStatusResponse = searchIndexerClient.getIndexerStatusWithResponse(indexer.getName(),
            Context.NONE).getValue();
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
        Response<Void> response = searchIndexerClient.runIndexerWithResponse(indexer.getName(), Context.NONE);
        SearchIndexerStatus indexerExecutionInfo = searchIndexerClient.getIndexerStatus(indexer.getName());

        assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.getStatusCode());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
    }

    @Test
    public void canRunIndexerAndGetIndexerStatus() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());

        // When an indexer is created, the execution info may not be available immediately. Hence, a
        // pipeline policy that injects a "mock_status" query string is added to the client, which results in service
        // returning a well-known mock response
        SearchIndexerClient mockStatusClient = getSearchIndexerClientBuilder(MOCK_STATUS_PIPELINE_POLICY).buildClient();

        mockStatusClient.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        SearchIndexerStatus indexerExecutionInfo = mockStatusClient.getIndexerStatus(indexer.getName());
        assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());

        Response<Void> indexerRunResponse = mockStatusClient.runIndexerWithResponse(indexer.getName(),
            Context.NONE);
        assertEquals(HttpResponseStatus.ACCEPTED.code(), indexerRunResponse.getStatusCode());

        indexerExecutionInfo = mockStatusClient.getIndexerStatus(indexer.getName());

        assertValidSearchIndexerStatus(indexerExecutionInfo);
    }

    @Test
    public void canUpdateIndexer() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentDescription(indexName, dataSourceName);

        mutateName(updated, initial.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        // verify the returned updated indexer is as expected
        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerFieldMapping() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentFieldMapping(indexName, dataSourceName);

        mutateName(updated, initial.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

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
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createDisabledIndexer(indexName, dataSourceName);

        mutateName(updated, initial.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canUpdateIndexerSchedule() {
        String indexName = createIndex();
        String dataSourceName = createDataSource();

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentSchedule(indexName, dataSourceName);

        mutateName(updated, initial.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

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
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithDifferentIndexingParameters(initial);
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

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
        String dataSourceName = searchIndexerClient.createDataSourceConnection(createBlobDataSource()).getName();
        dataSourcesToDelete.add(dataSourceName);

        SearchIndexer initial = createBaseTestIndexerObject(indexName, dataSourceName).setIsDisabled(true);
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexer updated = createIndexerWithStorageConfig(indexName, dataSourceName);

        mutateName(updated, initial.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
    public void canCreateIndexerWithBlobParams() {
        // Create the needed Azure blob resources and data source object
        SearchIndexerDataSourceConnection blobDataSource = createBlobDataSource();

        // Create the data source within the search service
        SearchIndexerDataSourceConnection dataSource = searchIndexerClient.createOrUpdateDataSourceConnection(blobDataSource);

        dataSourcesToDelete.add(dataSource.getName());

        // modify the indexer's blob params
        SearchIndexer indexer = createIndexerWithStorageConfig(createIndex(), dataSource.getName());

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canCreateAndDeleteIndexer() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        searchIndexerClient.createIndexer(indexer);

        searchIndexerClient.deleteIndexer(indexer.getName());
        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer(indexer.getName()));
    }

    @Test
    public void canCreateAndDeleteIndexerWithResponse() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        searchIndexerClient.createIndexerWithResponse(indexer, Context.NONE);

        searchIndexerClient.deleteIndexerWithResponse(indexer, false, Context.NONE);
        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer(indexer.getName()));
    }

    @Test
    public void deleteIndexerIsIdempotent() {
        // Create the indexer object
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());

        // Try delete before the indexer even exists.
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
    public void canCreateAndGetIndexer() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        searchIndexerClient.createIndexer(indexer);
        indexersToDelete.add(indexer.getName());

        SearchIndexer  indexerResult = searchIndexerClient.getIndexer(indexer.getName());
        assertObjectEquals(indexer, indexerResult, true, "etag");

        indexerResult = searchIndexerClient.getIndexerWithResponse(indexer.getName(), Context.NONE)
            .getValue();
        assertObjectEquals(indexer, indexerResult, true, "etag");
    }

    @Test
    public void getIndexerThrowsOnNotFound() {
        assertHttpResponseException(
            () -> searchIndexerClient.getIndexer("thisindexerdoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "Indexer 'thisindexerdoesnotexist' was not found");
    }

    @Test
    public void createOrUpdateIndexerIfNotExistsSucceedsOnNoResource() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer created = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, true, Context.NONE)
            .getValue();
        indexersToDelete.add(created.getName());

        assertFalse(CoreUtils.isNullOrEmpty(created.getETag()));
    }

    @Test
    public void deleteIndexerIfExistsWorksOnlyWhenResourceExists() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
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
    public void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
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
    public void updateIndexerIfExistsSucceedsOnExistingResource() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer original = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(original.setDescription("ABrandNewDescription"),
            false, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Verify the eTag is not empty and was changed
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfNotChangedFailsWhenResourceChanged() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer original = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(original.setDescription("ABrandNewDescription"),
            true, Context.NONE)
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
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndexer indexer = createBaseTestIndexerObject(createIndex(), createDataSource());
        SearchIndexer original = searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexersToDelete.add(original.getName());

        SearchIndexer updated = searchIndexerClient.createOrUpdateIndexerWithResponse(original.setDescription("ABrandNewDescription"),
            true, Context.NONE)
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
        searchIndexerClient.createIndexer(initial);
        indexersToDelete.add(initial.getName());

        SearchIndexerSkillset skillset = createSkillsetObject();
        searchIndexerClient.createSkillset(skillset);
        skillsetsToDelete.add(skillset.getName());
        SearchIndexer updated = createIndexerWithDifferentSkillset(indexName, dataSourceName, skillset.getName());
        mutateName(updated, initial.getName());
        SearchIndexer indexerResponse = searchIndexerClient.createOrUpdateIndexer(updated);

        setSameStartTime(updated, indexerResponse);
        assertObjectEquals(updated, indexerResponse, true, "etag");
    }

    @Test
    public void canCreateIndexerWithSkillset() {
        SearchIndexerSkillset skillset = searchIndexerClient.createSkillset(createSkillsetObject());
        skillsetsToDelete.add(skillset.getName());

        SearchIndexer indexer = createIndexerWithDifferentSkillset(createIndex(), createDataSource(), skillset.getName());

        createAndValidateIndexer(indexer);
    }

    void mutateName(SearchIndexer updateIndexer, String indexerName) {
        try {
            Field updateField = updateIndexer.getClass().getDeclaredField("name");
            updateField.setAccessible(true);
            updateField.set(updateIndexer, indexerName);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Create a new valid skillset object
     *
     * @return the newly created skillset object
     */
    SearchIndexerSkillset createSkillsetObject() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry("text")
                .setTargetName("mytext")
        );

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext("/document")
        );
        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-skillset", 32))
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexer createBaseTestIndexerObject(String targetIndexName, String dataSourceName) {
        return new SearchIndexer(testResourceNamer.randomName("indexer", 32), dataSourceName, targetIndexName)
            .setSchedule(new IndexingSchedule(Duration.ofDays(1)));
    }

    /**
     * This index contains fields that are declared on the live data source we use to test the indexers
     *
     * @return the newly created Index object
     */
    SearchIndex createTestIndexForLiveDatasource() {
        return new SearchIndex(testResourceNamer.randomName(IndexersManagementSyncTests.TARGET_INDEX_NAME, 32))
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
        List<FieldMapping> fieldMappings = Collections.singletonList(new FieldMapping("state_alpha")
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

        IndexingSchedule is = new IndexingSchedule(Duration.ofMinutes(10));

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
