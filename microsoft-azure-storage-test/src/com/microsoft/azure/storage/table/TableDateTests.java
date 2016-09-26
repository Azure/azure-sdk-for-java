/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.table;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

/**
 * Table Operation Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class TableDateTests {

    private CloudTable table;

    @Before
    public void tableTestMethodSetUp() throws URISyntaxException, StorageException {
        this.table = TableTestHelper.getRandomTableReference();
        this.table.createIfNotExists();
    }

    @After
    public void tableTestMethodTearDown() throws StorageException {
        this.table.deleteIfExists();
    }

    @Test
    public void testTableQueryRoundTripDate() throws URISyntaxException, StorageException {
        // 2014-12-07T09:15:12.123Z  from Java
        testRoundTripDate(new Date(1417943712123L));

        // 2015-01-14T14:53:32.800Z  from Java
        testRoundTripDate(new Date(1421247212800L));
    }

    @Test
    public void testRoundTripDateJsonAtom() throws URISyntaxException, StorageException {
        // JSON
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.123Z", 1417943712123L, 0, false, false, TablePayloadFormat.Json);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.800Z", 1421247212800L, 0, false, false, TablePayloadFormat.Json);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, false, false, TablePayloadFormat.Json);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, false, false, TablePayloadFormat.Json);


        // JSON NO METADATA
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.123Z", 1417943712123L, 0, false, false, TablePayloadFormat.JsonNoMetadata);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.800Z", 1421247212800L, 0, false, false, TablePayloadFormat.JsonNoMetadata);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, false, false, TablePayloadFormat.JsonNoMetadata);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, false, false, TablePayloadFormat.JsonNoMetadata);
    }

    @Test
    public void testRoundTripDateJsonAtomCrossVersion()
            throws URISyntaxException, StorageException {
        // JSON
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.0000123Z", 1417943712123L, 0, true, false, TablePayloadFormat.Json);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.0000800Z", 1421247212800L, 0, true, false, TablePayloadFormat.Json);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, true, false, TablePayloadFormat.Json);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, true, false, TablePayloadFormat.Json);

        // JSON NO METADATA
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.0000123Z", 1417943712123L, 0, true, false, TablePayloadFormat.JsonNoMetadata);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.0000800Z", 1421247212800L, 0, true, false, TablePayloadFormat.JsonNoMetadata);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, true, false, TablePayloadFormat.JsonNoMetadata);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, true, false, TablePayloadFormat.JsonNoMetadata);
    }

    @Test
    public void testRoundTripDateJsonAtomWithBackwardCompatibility()
            throws URISyntaxException, StorageException {
        // JSON
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.123Z", 1417943712123L, 0, false, true, TablePayloadFormat.Json);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.800Z", 1421247212800L, 0, false, true, TablePayloadFormat.Json);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, false, true, TablePayloadFormat.Json);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, false, true, TablePayloadFormat.Json);

        // JSON NO METADATA
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.123Z", 1417943712123L, 0, false, true, TablePayloadFormat.JsonNoMetadata);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.800Z", 1421247212800L, 0, false, true, TablePayloadFormat.JsonNoMetadata);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, false, true, TablePayloadFormat.JsonNoMetadata);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, false, true, TablePayloadFormat.JsonNoMetadata);
    }

    @Test
    public void testRoundTripDateJsonAtomCrossVersionWithBackwardCompatibility()
            throws URISyntaxException, StorageException {
        // JSON
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.0000123Z", 1417943712123L, 0, true, true, TablePayloadFormat.Json);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.0000800Z", 1421247212800L, 0, true, true, TablePayloadFormat.Json);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, true, true, TablePayloadFormat.Json);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, true, true, TablePayloadFormat.Json);

        // JSON NO METADATA
        // 2014-12-07T09:15:12.123Z  from Java
        testTableQueryRoundTripDate(
                "2014-12-07T09:15:12.0000123Z", 1417943712123L, 0, true, true, TablePayloadFormat.JsonNoMetadata);

        // 2015-01-14T14:53:32.800Z  from Java
        testTableQueryRoundTripDate(
                "2015-01-14T14:53:32.0000800Z", 1421247212800L, 0, true, true, TablePayloadFormat.JsonNoMetadata);

        // 2014-11-29T22:55:21.9876543Z  from .Net
        testTableQueryRoundTripDate(
                "2014-11-29T22:55:21.9876543Z", 1417301721987L, 6543, true, true, TablePayloadFormat.JsonNoMetadata);

        // 2015-02-14T03:11:13.0000229Z  from .Net
        testTableQueryRoundTripDate(
                "2015-02-14T03:11:13.0000229Z", 1423883473000L, 229, true, true, TablePayloadFormat.JsonNoMetadata);
    }

    private void testRoundTripDate(final Date date) throws URISyntaxException, StorageException {
        final String partitionKey = "partitionTest";

        // DateBackwardCompatibility off
        String rowKey = TableTestHelper.generateRandomKeyName();
        DateTestEntity entity = new DateTestEntity(partitionKey, rowKey);
        entity.setDate(date);

        TableOperation put = TableOperation.insertOrReplace(entity);
        this.table.execute(put);

        TableOperation get = TableOperation.retrieve(partitionKey, rowKey, DateTestEntity.class);
        entity = this.table.execute(get).getResultAsType();
        assertEquals(date.getTime(), entity.getDate().getTime());

        // DateBackwardCompatibility on
        rowKey = TableTestHelper.generateRandomKeyName();
        entity = new DateTestEntity(partitionKey, rowKey);
        entity.setDate(date);

        put = TableOperation.insertOrReplace(entity);
        this.table.execute(put);

        get = TableOperation.retrieve(partitionKey, rowKey, DateTestEntity.class);
        final TableRequestOptions options = new TableRequestOptions();
        options.setDateBackwardCompatibility(true);
        entity = this.table.execute(get, options, null).getResultAsType();
        assertEquals(date.getTime(), entity.getDate().getTime());

        // DateBackwardCompatibility off
        final String dateKey = "date";
        final EntityProperty property = new EntityProperty(date);
        rowKey = TableTestHelper.generateRandomKeyName();
        DynamicTableEntity dynamicEntity = new DynamicTableEntity(partitionKey, rowKey);
        dynamicEntity.getProperties().put(dateKey, property);

        put = TableOperation.insertOrReplace(dynamicEntity);
        this.table.execute(put);

        get = TableOperation.retrieve(partitionKey, rowKey, DynamicTableEntity.class);
        dynamicEntity = this.table.execute(get).getResultAsType();
        assertEquals(date.getTime(), dynamicEntity.getProperties().get(dateKey).getValueAsDate().getTime());

        // DateBackwardCompatibility on
        rowKey = TableTestHelper.generateRandomKeyName();
        dynamicEntity = new DynamicTableEntity(partitionKey, rowKey);
        dynamicEntity.getProperties().put(dateKey, property);

        put = TableOperation.insertOrReplace(dynamicEntity);
        this.table.execute(put);

        get = TableOperation.retrieve(partitionKey, rowKey, DynamicTableEntity.class);
        options.setDateBackwardCompatibility(true);
        dynamicEntity = this.table.execute(get, options, null).getResultAsType();
        assertEquals(date.getTime(), dynamicEntity.getProperties().get(dateKey).getValueAsDate().getTime());
    }

    private void testTableQueryRoundTripDate(final String dateString, final long milliseconds, final int ticks,
            final boolean writtenPre2, final boolean dateBackwardCompatibility, TablePayloadFormat format)
            throws URISyntaxException, StorageException {
        assertTrue(ticks >= 0);     // ticks is non-negative
        assertTrue(ticks <= 9999);  // ticks do not overflow into milliseconds
        final String partitionKey = "partitionTest";
        final String dateKey = "date";
        long expectedMilliseconds = milliseconds;

        if (dateBackwardCompatibility && (milliseconds % 1000 == 0) && (ticks < 1000)) {
            // when no milliseconds are present dateBackwardCompatibility causes up to 3 digits of ticks
            // to be read as milliseconds
            expectedMilliseconds += ticks;
        } else if (writtenPre2 && !dateBackwardCompatibility && (ticks == 0)) {
            // without DateBackwardCompatibility, milliseconds stored by Java prior to 2.0.0 are lost
            expectedMilliseconds -= expectedMilliseconds % 1000;
        }

        // Create a property for how the service would store the dateString
        EntityProperty property = new EntityProperty(dateString, EdmType.DATE_TIME);
        String rowKey = TableTestHelper.generateRandomKeyName();
        DynamicTableEntity dynamicEntity = new DynamicTableEntity(partitionKey, rowKey);
        dynamicEntity.getProperties().put(dateKey, property);

        // Add the entity to the table
        TableOperation put = TableOperation.insertOrReplace(dynamicEntity);
        this.table.execute(put);

        // Specify the options
        TableRequestOptions options = new TableRequestOptions();
        options.setDateBackwardCompatibility(dateBackwardCompatibility);
        options.setTablePayloadFormat(format);

        // Fetch the entity from the table
        TableOperation get = TableOperation.retrieve(partitionKey, rowKey, DynamicTableEntity.class);
        dynamicEntity = this.table.execute(get, options, null).getResultAsType();

        // Ensure the date matches our expectations
        assertEquals(expectedMilliseconds, dynamicEntity.getProperties().get(dateKey).getValueAsDate().getTime());
    }

    private static class DateTestEntity extends TableServiceEntity {
        private Date value;

        @SuppressWarnings("unused")
        public DateTestEntity() {
        }

        public DateTestEntity(String partition, String key) {
            super(partition, key);
        }

        public Date getDate() {
            return this.value;
        }

        public void setDate(Date value) {
            this.value = value;
        }
     }
}