// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexingSchedule;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class IndexersManagementTestBase extends SearchServiceTestBase {

    @Test
    public abstract void createIndexerReturnsCorrectDefinition();

    @Test
    public abstract void canCreateAndListIndexers();

    @Test
    public abstract void createIndexerFailsWithUsefulMessageOnUserError();

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
}
