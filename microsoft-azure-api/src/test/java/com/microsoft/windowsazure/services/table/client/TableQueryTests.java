/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.table.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ResponseReceivedEvent;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.StorageEvent;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.TableQuery.QueryComparisons;

/**
 * Table Query Tests
 */
public class TableQueryTests extends TableTestBase {
    @BeforeClass
    public static void setup() throws URISyntaxException, StorageException, InvalidKeyException {
        TableTestBase.setup();

        // Insert 500 entities in Batches to query
        for (int i = 0; i < 5; i++) {
            TableBatchOperation batch = new TableBatchOperation();

            for (int j = 0; j < 100; j++) {
                class1 ent = generateRandomEnitity("javatables_batch_" + Integer.toString(i));
                ent.setRowKey(String.format("%06d", j));
                batch.insert(ent);
            }

            tClient.execute(testSuiteTableName, batch);
        }
    }

    @Test
    public void tableQueryWithDynamicEntity() {
        // Create entity to check against
        class1 randEnt = TableTestBase.generateRandomEnitity(null);

        final Iterable<DynamicTableEntity> result = tClient.execute(TableQuery.from(testSuiteTableName,
                DynamicTableEntity.class));

        // Validate results
        for (DynamicTableEntity ent : result) {
            Assert.assertEquals(ent.getProperties().size(), 4);
            Assert.assertEquals(ent.getProperties().get("A").getValueAsString(), randEnt.getA());
            Assert.assertEquals(ent.getProperties().get("B").getValueAsString(), randEnt.getB());
            Assert.assertEquals(ent.getProperties().get("C").getValueAsString(), randEnt.getC());
            Assert.assertTrue(Arrays.equals(ent.getProperties().get("D").getValueAsByteArray(), randEnt.getD()));
        }
    }

    @Test
    public void tableQueryWithProjection() {
        // Create entity to check against
        class1 randEnt = TableTestBase.generateRandomEnitity(null);
        final Iterable<class1> result = tClient.execute(TableQuery.from(testSuiteTableName, class1.class).select(
                new String[] { "A", "C" }));

        // Validate results
        for (class1 ent : result) {
            // Validate core properties were sent.
            Assert.assertNotNull(ent.getPartitionKey());
            Assert.assertNotNull(ent.getRowKey());
            Assert.assertNotNull(ent.getTimestamp());

            // Validate correct columsn returned.
            Assert.assertEquals(ent.getA(), randEnt.getA());
            Assert.assertEquals(ent.getB(), null);
            Assert.assertEquals(ent.getC(), randEnt.getC());
            Assert.assertEquals(ent.getD(), null);
        }
    }

    @Test
    public void ensureSelectOnlySendsReservedColumnsOnce() {
        OperationContext opContext = new OperationContext();
        opContext.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                HttpURLConnection conn = (HttpURLConnection) eventArg.getConnectionObject();

                String urlString = conn.getURL().toString();

                Assert.assertEquals(urlString.indexOf("PartitionKey"), urlString.lastIndexOf("PartitionKey"));
                Assert.assertEquals(urlString.indexOf("RowKey"), urlString.lastIndexOf("RowKey"));
                Assert.assertEquals(urlString.indexOf("Timestamp"), urlString.lastIndexOf("Timestamp"));
            }
        });

        final Iterable<class1> result = tClient.execute(
                TableQuery.from(testSuiteTableName, class1.class).select(
                        new String[] { "PartitionKey", "RowKey", "Timestamp" }), null, opContext);

        // Validate results
        for (class1 ent : result) {
            Assert.assertEquals(ent.getA(), null);
            Assert.assertEquals(ent.getB(), null);
            Assert.assertEquals(ent.getC(), null);
            Assert.assertEquals(ent.getD(), null);
        }
    }

    @Test
    public void tableQueryWithReflection() {
        // Create entity to check against
        class1 randEnt = TableTestBase.generateRandomEnitity(null);

        final Iterable<class1> result = tClient.execute(TableQuery.from(testSuiteTableName, class1.class));

        // Validate results
        for (class1 ent : result) {
            Assert.assertEquals(ent.getA(), randEnt.getA());
            Assert.assertEquals(ent.getB(), randEnt.getB());
            Assert.assertEquals(ent.getC(), randEnt.getC());
            Assert.assertTrue(Arrays.equals(ent.getD(), randEnt.getD()));
        }
    }

    @Test
    public void tableQueryWithResolver() {
        // Create entity to check against
        class1 randEnt = TableTestBase.generateRandomEnitity(null);

        final Iterable<class1> result = tClient.execute(TableQuery.from(testSuiteTableName, TableServiceEntity.class),
                new EntityResolver<class1>() {
                    @Override
                    public class1 resolve(String partitionKey, String rowKey, Date timeStamp,
                            HashMap<String, EntityProperty> properties, String etag) {
                        Assert.assertEquals(properties.size(), 4);
                        class1 ref = new class1();
                        ref.setA(properties.get("A").getValueAsString());
                        ref.setB(properties.get("B").getValueAsString());
                        ref.setC(properties.get("C").getValueAsString());
                        ref.setD(properties.get("D").getValueAsByteArray());
                        return ref;
                    }
                });

        // Validate results
        for (class1 ent : result) {
            Assert.assertEquals(ent.getA(), randEnt.getA());
            Assert.assertEquals(ent.getB(), randEnt.getB());
            Assert.assertEquals(ent.getC(), randEnt.getC());
            Assert.assertTrue(Arrays.equals(ent.getD(), randEnt.getD()));
        }
    }

    @Test
    public void tableQueryWithTake() throws IOException, URISyntaxException, StorageException {
        // Create entity to check against
        class1 randEnt = TableTestBase.generateRandomEnitity(null);
        final ResultSegment<class1> result = tClient.executeSegmented(TableQuery.from(testSuiteTableName, class1.class)
                .select(new String[] { "A", "C" }).take(25), null);

        int count = 0;
        // Validate results
        for (class1 ent : result.getResults()) {
            count++;
            Assert.assertEquals(ent.getA(), randEnt.getA());
            Assert.assertEquals(ent.getB(), null);
            Assert.assertEquals(ent.getC(), randEnt.getC());
            Assert.assertEquals(ent.getD(), null);
        }

        Assert.assertEquals(count, 25);
    }

    @Test
    public void tableQueryWithFilter() throws StorageException {
        class1 randEnt = TableTestBase.generateRandomEnitity(null);
        TableQuery<class1> query = TableQuery.from(testSuiteTableName, class1.class).where(
                String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

        int count = 0;

        for (class1 ent : tClient.execute(query)) {
            Assert.assertEquals(ent.getA(), randEnt.getA());
            Assert.assertEquals(ent.getB(), randEnt.getB());
            Assert.assertEquals(ent.getC(), randEnt.getC());
            Assert.assertEquals(ent.getPartitionKey(), "javatables_batch_1");
            Assert.assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
            count++;
        }

        Assert.assertEquals(count, 50);
    }

    @Test
    public void tableQueryWithContinuation() throws StorageException {
        class1 randEnt = TableTestBase.generateRandomEnitity(null);
        TableQuery<class1> query = TableQuery.from(testSuiteTableName, class1.class)
                .where(String.format("(PartitionKey ge '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"))
                .take(25);

        // take will cause the query to return 25 at a time

        int count = 0;
        int pk = 1;
        for (class1 ent : tClient.execute(query)) {
            Assert.assertEquals(ent.getA(), randEnt.getA());
            Assert.assertEquals(ent.getB(), randEnt.getB());
            Assert.assertEquals(ent.getC(), randEnt.getC());
            Assert.assertEquals(ent.getPartitionKey(), "javatables_batch_" + Integer.toString(pk));
            Assert.assertEquals(ent.getRowKey(), String.format("%06d", count % 50 + 50));
            count++;

            if (count % 50 == 0) {
                pk++;
            }
        }

        Assert.assertEquals(count, 200);
    }

    @Test
    public void testQueryWithNullClassType() throws StorageException {
        try {
            TableQuery.from(testSuiteTableName, null);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Query requires a valid class type.");
        }
    }

    @Test
    public void testQueryWithInvalidTakeCount() throws StorageException {
        try {
            TableQuery.from(testSuiteTableName, TableServiceEntity.class).take(0);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Take count must be positive and greater than 0.");
        }

        try {
            TableQuery.from(testSuiteTableName, TableServiceEntity.class).take(-1);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Take count must be positive and greater than 0.");
        }
    }

    @Test
    public void tableInvalidQuery() throws StorageException, IOException, URISyntaxException {
        TableQuery<class1> query = TableQuery.from(testSuiteTableName, class1.class).where(
                String.format("(PartitionKey ) and (RowKey ge '%s')", "javatables_batch_1"));
        try {
            tClient.executeSegmented(query, null);
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("One of the request inputs is not valid."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
        }
    }

    @Test
    public void testQueryOnSupportedTypes() throws StorageException {
        // Setup
        TableBatchOperation batch = new TableBatchOperation();
        String pk = UUID.randomUUID().toString();

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
            ent.setInt64((long) j);
            ent.setIntegerPrimitive(j);
            ent.setLongPrimitive(j);
            ent.setGuid(UUID.randomUUID());
            ent.setString(String.format("%04d", j));

            try {
                // Add delay to make times unique
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            batch.insert(ent);
            if (j == 50) {
                middleRef = ent;
            }
        }

        tClient.execute(testSuiteTableName, batch);

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

            for (ComplexEntity e : tClient.execute(query)) {
                delBatch.delete(e);
            }

            tClient.execute(testSuiteTableName, delBatch);
        }
    }

    private void executeQueryAndAssertResults(String filter, int expectedResults) {
        int count = 0;
        TableQuery<ComplexEntity> query = TableQuery.from(testSuiteTableName, ComplexEntity.class).where(filter);
        for (@SuppressWarnings("unused")
        ComplexEntity e : tClient.execute(query)) {
            count++;
        }

        Assert.assertEquals(expectedResults, count);
    }
}
