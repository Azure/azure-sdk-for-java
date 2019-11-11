// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.DataSource;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.FieldMapping;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexingSchedule;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
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
    protected void createUpdateValidateIndexer(Indexer updatedIndexer) {
        // Create the data source, note it's a valid DS with actual
        // connection string
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

    /**
     * Change a basic property of an indexer and return it
     * @return the updated indexer
     */
    protected Indexer changeIndexerBasic() {
        // create another indexer object
        Indexer updatedExpected =
            createTestIndexer("indexer");

        // modify it
        updatedExpected.setDescription("somethingdifferent");

        return updatedExpected;
    }

    /**
     * Change the field mappings property of an indexer and return it
     * @return the updated indexer
     */
    protected Indexer changeIndexerFieldMapping() {
        // Check field mappings can be changed
        List<FieldMapping> lst = new LinkedList<>();
        FieldMapping fm = new FieldMapping();
        fm.setSourceFieldName("state_alpha");
        fm.setTargetFieldName("state");
        lst.add(fm);


        // create another indexer object
        Indexer updatedExpected =
            createTestIndexer("indexer");

        // modify it
        updatedExpected.setFieldMappings(lst);

        return updatedExpected;
    }

    /**
     * Change the Disabled property of an indexer and return it
     * @return the updated indexer
     */
    protected Indexer changeIndexerDisabled() {
        // create another indexer object
        Indexer updatedExpected =
            createTestIndexer("indexer");

        // modify it
        updatedExpected.setIsDisabled(false);

        return updatedExpected;
    }

    /**
     * Change the schedule property of an indexer and return it
     * @return the updated indexer
     */
    protected Indexer changeIndexerSchedule() {
        // create another indexer object
        Indexer updatedExpected =
            createTestIndexer("indexer");

        IndexingSchedule is2 = updatedExpected.getSchedule();

        IndexingSchedule is = new IndexingSchedule();
        //is.setStartTime(OffsetDateTime.parse("2020-12-30T11:22:33+02:00"));
        is.setInterval(Duration.ofMinutes(10));

        // modify it
        updatedExpected.setSchedule(is);

        return updatedExpected;
    }


    protected abstract Index createIndex(Index index);

    protected abstract DataSource createDatasource(DataSource datasource);

    protected abstract Indexer createIndexer(Indexer initial);
}
