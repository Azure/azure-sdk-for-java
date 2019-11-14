// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.DataSource;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.FieldMapping;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexingParameters;
import com.azure.search.models.IndexingSchedule;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
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
    public abstract void canUpdateIndexer();

    @Test
    public abstract void canUpdateIndexerFieldMapping();

    @Test
    public abstract void canUpdateIndexerDisabled();

    @Test
    public abstract void canUpdateIndexerSchedule();

    @Test
    public abstract void canCreateIndexerWithSchedule();

    @Test
    public abstract void canUpdateIndexerBatchSizeMaxFailedItems();

    @Test
    public abstract void canCreateIndexerWithBatchSizeMaxFailedItems();

    protected void assertIndexersEqual(Indexer expected, Indexer actual) {
        expected.setETag("none");
        actual.setETag("none");

        // we ignore defaults as when properties are not set they are returned from the service with
        // default values
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    protected Indexer createTestIndexer(String indexerName) {
        return new Indexer()
            .setName(indexerName)
            .setDataSourceName("azs-java-test-sql")
            .setTargetIndexName("indexforindexers")
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }

    protected static void expectSameStartTime(Indexer expected, Indexer actual) {
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

    void assertException(Runnable exceptionThrower, Class<? extends Exception> expectedExceptionType, String expectedMessage) {
        try {
            exceptionThrower.run();
            Assert.fail();
        } catch (Throwable ex) {
            Assert.assertEquals(expectedExceptionType, ex.getClass());
            Assert.assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    /**
     * Creates the index and indexer in the search service and then update the indexer
     * @param updatedIndexer the indexer to be updated
     */
    void createUpdateAndValidateIndexer(Indexer updatedIndexer) {
        // Create the data source, note it's a valid DS with actual connection string
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // create an indexer object
        Indexer initial =
            createTestIndexer("indexer")
                .setIsDisabled(true);

        // create this indexer in the service
        createIndexer(initial);

        // update the indexer
        Indexer updatedResponse = createIndexer(updatedIndexer);

        // verify the returned updated indexer is as expected
        expectSameStartTime(updatedIndexer, updatedResponse);
        assertIndexersEqual(updatedIndexer, updatedResponse);
    }

    void createAndValidateIndexer(Indexer indexer) {
        // Create the data source, note it's a valid DS with actual connection string
        DataSource datasource = createTestSqlDataSource();
        createDatasource(datasource);

        // Create an index
        Index index = createTestIndexForLiveDatasource();
        createIndex(index);

        // create this indexer in the service
        Indexer indexerResponse = createIndexer(indexer);

        // verify the returned indexer is as expected
        assertIndexersEqual(indexer, indexerResponse);
    }

    /**
     * Create a new indexer and change its description property
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentDescription() {
        // create a new indexer object
        Indexer indexer = createTestIndexer("indexer");

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
        Indexer indexer = createTestIndexer("indexer");

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
        Indexer indexer = createTestIndexer("indexer");

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
        Indexer indexer = createTestIndexer("indexer");

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
        Indexer indexer = createTestIndexer("indexer");

        IndexingParameters ip = new IndexingParameters()
            .setMaxFailedItems(121)
            .setMaxFailedItemsPerBatch(11)
            .setBatchSize(20);

        // modify the indexer
        indexer.setParameters(ip);

        return indexer;
    }

    protected abstract Index createIndex(Index index);

    protected abstract DataSource createDatasource(DataSource datasource);

    protected abstract Indexer createIndexer(Indexer initial);
}
