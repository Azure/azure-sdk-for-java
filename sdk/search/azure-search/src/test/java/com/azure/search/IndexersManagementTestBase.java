// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.FieldMapping;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexingParameters;
import com.azure.search.models.IndexingSchedule;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class IndexersManagementTestBase extends SearchServiceTestBase {

    @Test
    public abstract void createIndexerReturnsCorrectDefinition();

    @Test
    public abstract void canCreateAndListIndexers();

    @Test
    public abstract void createIndexerFailsWithUsefulMessageOnUserError();

    @Test
    public abstract void canResetIndexerAndGetIndexerStatus();

    @Test
    public void canUpdateIndexer() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentDescription();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerFieldMapping() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentFieldMapping();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithFieldMapping() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createIndexerWithDifferentFieldMapping()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerDisabled() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createDisabledIndexer();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canUpdateIndexerSchedule() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentSchedule();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithSchedule() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createIndexerWithDifferentSchedule()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    @Test
    public void canUpdateIndexerBatchSizeMaxFailedItems() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer updatedExpected = createIndexerWithDifferentIndexingParameters();

        createUpdateAndValidateIndexer(updatedExpected, SQL_DATASOURCE_NAME);
    }

    @Test
    public void canCreateIndexerWithBatchSizeMaxFailedItems() {
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        Indexer indexer = createIndexerWithDifferentIndexingParameters()
            .setDataSourceName(SQL_DATASOURCE_NAME);

        createAndValidateIndexer(indexer);
    }

    // This test currently does not pass on our Dogfood account, as the
    // Storage resource provider is not returning an answer.
    @Test
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
    @Test
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

    @Test
    public void deleteIndexerIsIdempotent() {
        // Create Datasource
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // Create the indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);

        // Try delete before the indexer even exists.
        Response<Void> result = deleteIndexer(indexer);

        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());

        // Actually create the indexer
        createIndexer(indexer);

        // Now delete twice.
        result = deleteIndexer(indexer);
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, result.getStatusCode());

        result = deleteIndexer(indexer);
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void canCreateAndGetIndexer() {
        String indexerName = "indexer";

        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        Indexer indexer = createTestDataSourceAndIndexer(indexerName);

        createIndexer(indexer);

        Indexer indexerResult = getIndexer(indexerName);

        assertIndexersEqual(indexer, indexerResult);
    }

    @Test
    public void getIndexerThrowsOnNotFound() {
        assertException(
            () -> getIndexer("thisindexerdoesnotexist"),
            HttpResponseException.class,
            "Indexer 'thisindexerdoesnotexist' was not found");
    }

    protected void assertIndexersEqual(Indexer expected, Indexer actual) {
        expected.setETag("none");
        actual.setETag("none");

        // we ignore defaults as when properties are not set they are returned from the service with
        // default values
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    protected Indexer createTestDataSourceAndIndexer(String indexerName) {
        return new Indexer()
            .setName(indexerName)
            .setTargetIndexName("indexforindexers")
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }

    protected Indexer createTestDataSourceAndIndexer() {
        // Create Datasource
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // Create the indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");
        indexer.setDataSourceName(SQL_DATASOURCE_NAME);
        createIndexer(indexer);

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

    /**
     * Creates the index and indexer in the search service and then update the indexer
     * @param updatedIndexer the indexer to be updated
     * @param datasourceName the datasource name for this indexer
     */
    void createUpdateAndValidateIndexer(Indexer updatedIndexer, String datasourceName) {
        updatedIndexer.setDataSourceName(datasourceName);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        Indexer initial =
            createTestDataSourceAndIndexer("indexer")
                .setDataSourceName(datasourceName)
                .setIsDisabled(true);

        // create this indexer in the service
        createIndexer(initial);

        // create this indexer in the service
        Indexer indexerResponse = createIndexer(updatedIndexer);

        // verify the returned updated indexer is as expected
        expectSameStartTime(updatedIndexer, indexerResponse);
        assertIndexersEqual(updatedIndexer, indexerResponse);
    }

    /**
     * Creates the index and indexer in the search service and then retrieves the indexer and validates it
     * @param indexer the indexer to be created
     */
    void createAndValidateIndexer(Indexer indexer) {
        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // create this indexer in the service
        Indexer indexerResponse = createIndexer(indexer);

        // verify the returned updated indexer is as expected
        expectSameStartTime(indexer, indexerResponse);
        assertIndexersEqual(indexer, indexerResponse);
    }

    /**
     * Create a new indexer and change its description property
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentDescription() {
        // create a new indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");

        // modify it
        indexer.setDescription("somethingdifferent");

        return indexer;
    }

    /**
     * Create a new indexer and change its field mappings property
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentFieldMapping() {
        // create a new indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");

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
     * @return the created indexer
     */
    Indexer createDisabledIndexer() {
        // create a new indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");

        // modify it
        indexer.setIsDisabled(false);

        return indexer;
    }

    /**
     * Create a new indexer and change its schedule property
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentSchedule() {
        // create a new indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");

        IndexingSchedule is = new IndexingSchedule()
            .setInterval(Duration.ofMinutes(10));

        // modify the indexer
        indexer.setSchedule(is);

        return indexer;
    }

    /**
     * Create a new indexer and change its indexing parameters
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentIndexingParameters() {
        // create a new indexer object
        Indexer indexer = createTestDataSourceAndIndexer("indexer");

        IndexingParameters ip = new IndexingParameters()
            .setMaxFailedItems(121)
            .setMaxFailedItemsPerBatch(11)
            .setBatchSize(20);

        // modify the indexer
        indexer.setParameters(ip);

        return indexer;
    }

    protected Indexer createIndexerWithStorageConfig() {
        // create an indexer object
        Indexer updatedExpected =
            createTestDataSourceAndIndexer("indexer");

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

    protected abstract Index createIndex(Index index);

    protected abstract DataSource createDatasource(DataSource datasource);

    protected abstract Indexer createIndexer(Indexer indexer);

    protected abstract Indexer getIndexer(String indexerName);

    protected abstract Response<Void> deleteIndexer(Indexer indexer);
}
