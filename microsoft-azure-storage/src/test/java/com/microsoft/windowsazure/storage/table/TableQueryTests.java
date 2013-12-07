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
package com.microsoft.windowsazure.storage.table;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ResponseReceivedEvent;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.StorageEvent;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.table.TableQuery.QueryComparisons;

/**
 * Table Query Tests
 */
@RunWith(Parameterized.class)
public class TableQueryTests extends TableTestBase {

    private final TableRequestOptions options;
    private final boolean usePropertyResolver;

    /**
     * These parameters are passed to the constructor at the start of each test run. This includes TablePayloadFormat,
     * and if that format is JsonNoMetadata, whether or not to use a PropertyResolver
     * 
     * @return the parameters pass to the constructor
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { TablePayloadFormat.AtomPub, false }, // AtomPub
                { TablePayloadFormat.JsonFullMetadata, false }, // Json Full Metadata
                { TablePayloadFormat.Json, false }, // Json Minimal Metadata
                { TablePayloadFormat.JsonNoMetadata, false }, // Json No Metadata without PropertyResolver 
                { TablePayloadFormat.JsonNoMetadata, true } // Json No Metadata with PropertyResolver
                });
    }

    /**
     * Takes a parameter from @Parameters to use for this run of the tests.
     * 
     * @param format
     *            The {@link TablePaylodFormat} to use for this test run
     * @param usePropertyResolver
     *            Whether or not to use a property resolver, applicable only for <code>JsonNoMetadata</code> format
     */
    public TableQueryTests(TablePayloadFormat format, boolean usePropertyResolver) {
        this.options = new TableRequestOptions();
        this.options.setTablePayloadFormat(format);
        this.usePropertyResolver = usePropertyResolver;
    }

    @BeforeClass
    public static void setup() throws URISyntaxException, StorageException, InvalidKeyException {
        TableTestBase.setup();

        // Insert 500 entities in Batches to query
        for (int i = 0; i < 5; i++) {
            TableBatchOperation batch = new TableBatchOperation();

            for (int j = 0; j < 100; j++) {
                Class1 ent = generateRandomEntity("javatables_batch_" + Integer.toString(i));
                ent.setRowKey(String.format("%06d", j));
                batch.insert(ent);
            }

            tClient.execute(testSuiteTableName, batch);
        }
    }

    //@Test
    public void tableQueryIterateTwice() throws StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        final Iterable<DynamicTableEntity> result = tClient.execute(
                TableQuery.from(testSuiteTableName, DynamicTableEntity.class).take(50), options, null);

        ArrayList<DynamicTableEntity> firstIteration = new ArrayList<DynamicTableEntity>();
        ArrayList<DynamicTableEntity> secondIteration = new ArrayList<DynamicTableEntity>();

        // Validate results
        for (DynamicTableEntity ent : result) {
            assertEquals(ent.getProperties().size(), 4);
            assertEquals(ent.getProperties().get("A").getValueAsString(), randEnt.getA());
            assertEquals(ent.getProperties().get("B").getValueAsString(), randEnt.getB());
            assertEquals(ent.getProperties().get("C").getValueAsString(), randEnt.getC());
            assertTrue(Arrays.equals(ent.getProperties().get("D").getValueAsByteArray(), randEnt.getD()));
            firstIteration.add(ent);
        }

        // Validate results
        for (DynamicTableEntity ent : result) {
            assertEquals(ent.getProperties().size(), 4);
            assertEquals(ent.getProperties().get("A").getValueAsString(), randEnt.getA());
            assertEquals(ent.getProperties().get("B").getValueAsString(), randEnt.getB());
            assertEquals(ent.getProperties().get("C").getValueAsString(), randEnt.getC());
            assertTrue(Arrays.equals(ent.getProperties().get("D").getValueAsByteArray(), randEnt.getD()));
            secondIteration.add(ent);
        }

        assertEquals(firstIteration.size(), secondIteration.size());
        for (int m = 0; m < firstIteration.size(); m++) {
            assertEquals(firstIteration.get(m).getPartitionKey(), secondIteration.get(m).getPartitionKey());
            assertEquals(firstIteration.get(m).getRowKey(), secondIteration.get(m).getRowKey());
            assertEquals(firstIteration.get(m).getProperties().size(), secondIteration.get(m).getProperties().size());
            assertEquals(firstIteration.get(m).getProperties().get("A").getValueAsString(), secondIteration.get(m)
                    .getProperties().get("A").getValueAsString());
            assertEquals(firstIteration.get(m).getProperties().get("B").getValueAsString(), secondIteration.get(m)
                    .getProperties().get("B").getValueAsString());
            assertEquals(firstIteration.get(m).getProperties().get("C").getValueAsString(), secondIteration.get(m)
                    .getProperties().get("C").getValueAsString());
            assertTrue(Arrays.equals(firstIteration.get(m).getProperties().get("D").getValueAsByteArray(),
                    secondIteration.get(m).getProperties().get("D").getValueAsByteArray()));
        }
    }

    @Test
    public void tableQueryWithDynamicEntity() throws StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        final Iterable<DynamicTableEntity> result = tClient.execute(
                TableQuery.from(testSuiteTableName, DynamicTableEntity.class), options, null);

        // Validate results
        for (DynamicTableEntity ent : result) {
            assertEquals(ent.getProperties().size(), 4);
            assertEquals(ent.getProperties().get("A").getValueAsString(), randEnt.getA());
            assertEquals(ent.getProperties().get("B").getValueAsString(), randEnt.getB());
            assertEquals(ent.getProperties().get("C").getValueAsString(), randEnt.getC());
            assertTrue(Arrays.equals(ent.getProperties().get("D").getValueAsByteArray(), randEnt.getD()));
        }
    }

    @Test
    public void tableQueryWithProjection() throws StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final Iterable<Class1> result = tClient.execute(
                TableQuery.from(testSuiteTableName, Class1.class).select(new String[] { "A", "C" }), options, null);

        // Validate results
        for (Class1 ent : result) {
            // Validate core properties were sent.
            assertNotNull(ent.getPartitionKey());
            assertNotNull(ent.getRowKey());
            assertNotNull(ent.getTimestamp());

            // Validate correct columsn returned.
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), null);
            assertEquals(ent.getC(), randEnt.getC());
            assertEquals(ent.getD(), null);
        }
    }

    @Test
    public void ensureSelectOnlySendsReservedColumnsOnce() throws StorageException {
        // Create entity to use property resolver
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        OperationContext opContext = new OperationContext();
        opContext.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                HttpURLConnection conn = (HttpURLConnection) eventArg.getConnectionObject();

                String urlString = conn.getURL().toString();

                assertEquals(urlString.indexOf("PartitionKey"), urlString.lastIndexOf("PartitionKey"));
                assertEquals(urlString.indexOf("RowKey"), urlString.lastIndexOf("RowKey"));
                assertEquals(urlString.indexOf("Timestamp"), urlString.lastIndexOf("Timestamp"));
            }
        });

        final Iterable<Class1> result = tClient.execute(
                TableQuery.from(testSuiteTableName, Class1.class).select(
                        new String[] { "PartitionKey", "RowKey", "Timestamp" }), options, opContext);

        // Validate results
        for (Class1 ent : result) {
            assertEquals(ent.getA(), null);
            assertEquals(ent.getB(), null);
            assertEquals(ent.getC(), null);
            assertEquals(ent.getD(), null);
        }
    }

    @Test
    public void tableQueryWithReflection() throws StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final Iterable<Class1> result = tClient.execute(TableQuery.from(testSuiteTableName, Class1.class), options,
                null);

        // Validate results
        for (Class1 ent : result) {
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), randEnt.getB());
            assertEquals(ent.getC(), randEnt.getC());
            assertTrue(Arrays.equals(ent.getD(), randEnt.getD()));
        }
    }

    @Test
    public void tableQueryWithResolver() throws StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final Iterable<Class1> result = tClient.execute(TableQuery.from(testSuiteTableName, TableServiceEntity.class),
                new EntityResolver<Class1>() {
                    @Override
                    public Class1 resolve(String partitionKey, String rowKey, Date timeStamp,
                            HashMap<String, EntityProperty> properties, String etag) {
                        assertEquals(properties.size(), 4);
                        Class1 ref = new Class1();
                        ref.setA(properties.get("A").getValueAsString());
                        ref.setB(properties.get("B").getValueAsString());
                        ref.setC(properties.get("C").getValueAsString());
                        ref.setD(properties.get("D").getValueAsByteArray());
                        return ref;
                    }
                }, options, null);

        // Validate results
        for (Class1 ent : result) {
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), randEnt.getB());
            assertEquals(ent.getC(), randEnt.getC());
            assertTrue(Arrays.equals(ent.getD(), randEnt.getD()));
        }
    }

    @Test
    public void tableQueryWithTake() throws IOException, URISyntaxException, StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final ResultSegment<Class1> result = tClient.executeSegmented(TableQuery.from(testSuiteTableName, Class1.class)
                .select(new String[] { "A", "C" }).take(25), null, options, null);

        int count = 0;
        // Validate results
        for (Class1 ent : result.getResults()) {
            count++;
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), null);
            assertEquals(ent.getC(), randEnt.getC());
            assertEquals(ent.getD(), null);
        }

        assertEquals(count, 25);
    }

    @Test
    public void tableQueryWithFilter() throws StorageException {
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        TableQuery<Class1> query = TableQuery.from(testSuiteTableName, Class1.class).where(
                String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

        int count = 0;

        for (Class1 ent : tClient.execute(query, options, null)) {
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), randEnt.getB());
            assertEquals(ent.getC(), randEnt.getC());
            assertEquals(ent.getPartitionKey(), "javatables_batch_1");
            assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
            count++;
        }

        assertEquals(count, 50);
    }

    @Test
    public void tableQueryWithContinuation() throws StorageException {
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        TableQuery<Class1> query = TableQuery.from(testSuiteTableName, Class1.class)
                .where(String.format("(PartitionKey ge '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"))
                .take(25);

        // take will cause the query to return 25 at a time

        int count = 0;
        int pk = 1;
        for (Class1 ent : tClient.execute(query, options, null)) {
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), randEnt.getB());
            assertEquals(ent.getC(), randEnt.getC());
            assertEquals(ent.getPartitionKey(), "javatables_batch_" + Integer.toString(pk));
            assertEquals(ent.getRowKey(), String.format("%06d", count % 50 + 50));
            count++;

            if (count % 50 == 0) {
                pk++;
            }
        }

        assertEquals(count, 200);
    }

    @Test
    public void testQueryWithNullClassType() throws StorageException {
        try {
            TableQuery.from(testSuiteTableName, null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), String.format(SR.ARGUMENT_NULL_OR_EMPTY, "class type"));
        }
    }

    @Test
    public void testQueryWithInvalidTakeCount() throws StorageException {
        try {
            TableQuery.from(testSuiteTableName, TableServiceEntity.class).take(0);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Take count must be positive and greater than 0.");
        }

        try {
            TableQuery.from(testSuiteTableName, TableServiceEntity.class).take(-1);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Take count must be positive and greater than 0.");
        }
    }

    @Test
    public void tableInvalidQuery() throws StorageException, IOException, URISyntaxException {
        // Create entity to check against
        Class1 randEnt = TableTestBase.generateRandomEntity(null);

        if (this.usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        TableQuery<Class1> query = TableQuery.from(testSuiteTableName, Class1.class).where(
                String.format("(PartitionKey ) and (RowKey ge '%s')", "000050"));
        try {
            tClient.executeSegmented(query, null, options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("One of the request inputs is not valid."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
        }
    }

    @Test
    public void testQueryOnSupportedTypes() throws StorageException, InterruptedException {
        // Setup
        TableBatchOperation batch = new TableBatchOperation();
        String pk = UUID.randomUUID().toString();

        long maxInt = Integer.MAX_VALUE; // used to ensure the test sends longs that cannot be interpreted as ints

        ComplexEntity middleRef = null;

        for (int j = 0; j < 100; j++) {
            ComplexEntity ent = new ComplexEntity();
            ent.setPartitionKey(pk);
            ent.setRowKey(String.format("%04d", j));
            ent.setBinary(new Byte[] { 0x01, 0x02, (byte) j });
            ent.setBinaryPrimitive(new byte[] { 0x01, 0x02, (byte) j });
            ent.setBool(j % 2 == 0 ? true : false);
            ent.setBoolPrimitive(j % 2 == 0 ? true : false);
            ent.setDateTime(new Date());
            ent.setDouble(j + ((double) j) / 100);
            ent.setDoublePrimitive(j + ((double) j) / 100);
            ent.setInt32(j);
            ent.setInt64(j + maxInt);
            ent.setIntegerPrimitive(j);
            ent.setLongPrimitive(j + maxInt);
            ent.setGuid(UUID.randomUUID());
            ent.setString(String.format("%04d", j));

            // Add delay to make times unique
            Thread.sleep(50);
            batch.insert(ent);
            if (j == 50) {
                middleRef = ent;
            }
        }

        if (this.usePropertyResolver) {
            options.setPropertyResolver(middleRef);
        }

        tClient.execute(testSuiteTableName, batch, options, null);

        try {
            // 1. Filter on String
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("String", QueryComparisons.GREATER_THAN_OR_EQUAL, "0050"), 50);

            // 2. Filter on UUID
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Guid", QueryComparisons.EQUAL, middleRef.getGuid()), 1);

            // 3. Filter on Long
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Int64", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getInt64()), 50);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("LongPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getInt64()), 50);

            // 4. Filter on Double
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Double", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getDouble()), 50);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("DoublePrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getDouble()), 50);

            // 5. Filter on Integer
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Int32", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getInt32()), 50);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("IntegerPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getInt32()), 50);

            // 6. Filter on Date
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("DateTime", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getDateTime()), 50);

            // 7. Filter on Boolean
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Bool", QueryComparisons.EQUAL, middleRef.getBool()), 50);

            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("BoolPrimitive", QueryComparisons.EQUAL, middleRef.getBool()),
                    50);

            // 8. Filter on Binary 
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Binary", QueryComparisons.EQUAL, middleRef.getBinary()), 1);

            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("BinaryPrimitive", QueryComparisons.EQUAL,
                            middleRef.getBinaryPrimitive()), 1);

            // 9. Filter on Binary GTE
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Binary", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getBinary()), 50);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("BinaryPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getBinaryPrimitive()), 50);

            // 10. Complex Filter on Binary GTE
            executeQueryAndAssertResults(TableQuery.combineFilters(
                    TableQuery.generateFilterCondition(TableConstants.PARTITION_KEY, QueryComparisons.EQUAL,
                            middleRef.getPartitionKey()),
                    TableQuery.Operators.AND,
                    TableQuery.generateFilterCondition("Binary", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getBinary())), 50);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("BinaryPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getBinaryPrimitive()), 50);

        }
        finally {
            // cleanup
            TableBatchOperation delBatch = new TableBatchOperation();
            TableQuery<ComplexEntity> query = TableQuery.from(testSuiteTableName, ComplexEntity.class).where(
                    String.format("PartitionKey eq '%s'", pk));

            for (ComplexEntity e : tClient.execute(query, options, null)) {
                delBatch.delete(e);
            }

            tClient.execute(testSuiteTableName, delBatch, options, null);
        }
    }

    private void executeQueryAndAssertResults(String filter, int expectedResults) throws StorageException {
        // instantiate class to use property resolver
        ComplexEntity ent = new ComplexEntity();
        if (this.usePropertyResolver) {
            options.setPropertyResolver(ent);
        }

        int count = 0;
        TableQuery<ComplexEntity> query = TableQuery.from(testSuiteTableName, ComplexEntity.class).where(filter);
        for (@SuppressWarnings("unused")
        ComplexEntity e : tClient.execute(query, options, null)) {
            count++;
        }

        assertEquals(expectedResults, count);
    }
}
