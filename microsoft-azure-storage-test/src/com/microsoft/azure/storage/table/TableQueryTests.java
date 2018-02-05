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

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResponseReceivedEvent;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;
import com.microsoft.azure.storage.table.TableTestHelper.Class1;
import com.microsoft.azure.storage.table.TableTestHelper.ComplexEntity;
import com.microsoft.azure.storage.table.TableTestHelper.EmptyClass;

import static org.junit.Assert.*;

/**
 * Table Query Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class TableQueryTests {

    private static CloudTable table;

    @BeforeClass
    public static void setup() throws URISyntaxException, StorageException {
        table = TableTestHelper.getRandomTableReference();
        table.createIfNotExists();

        // Insert 500 entities in Batches to query
        for (int i = 0; i < 5; i++) {
            TableBatchOperation batch = new TableBatchOperation();

            for (int j = 0; j < 100; j++) {
                Class1 ent = TableTestHelper.generateRandomEntity("javatables_batch_" + Integer.toString(i));
                ent.setRowKey(String.format("%06d", j));
                batch.insert(ent);
            }

            table.execute(batch);
        }
    }

    @AfterClass
    public static void teardown() throws StorageException {
        table.deleteIfExists();
    }

    @Test
    public void testQueryWithNullClassType() {
        try {
            TableQuery.from(null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(String.format(SR.ARGUMENT_NULL, "class type"), ex.getMessage());
        }
    }

    @Test
    public void testQueryWithInvalidTakeCount() {
        try {
            TableQuery.from(TableServiceEntity.class).take(0);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Take count must be positive and greater than 0.");
        }

        try {
            TableQuery.from(TableServiceEntity.class).take(-1);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Take count must be positive and greater than 0.");
        }
    }

    @Test
    public void testTableWithSelectOnMissingFields() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableWithSelectOnMissingFields(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableWithSelectOnMissingFields(options);
    }

    private void testTableWithSelectOnMissingFields(TableRequestOptions options) throws StorageException {
        TableQuery<DynamicTableEntity> projectionQuery = TableQuery.from(DynamicTableEntity.class).where(
                "(PartitionKey eq 'javatables_batch_0') and (RowKey eq '000000')");

        // A exists, F does not
        projectionQuery.select(new String[]{"A", "F"});

        ResultSegment<DynamicTableEntity> seg = table.executeSegmented(projectionQuery, null, options, null);
        assertEquals(1, seg.getResults().size());

        DynamicTableEntity ent = seg.getResults().get(0);
        assertEquals("foo_A", ent.getProperties().get("A").getValueAsString());
        assertEquals(null, ent.getProperties().get("F").getValueAsString());
        assertEquals(EdmType.STRING, ent.getProperties().get("F").getEdmType());
    }

    @Test
    public void testTableQueryProjectionWithNull() throws URISyntaxException, StorageException {
        CloudTable table = TableTestHelper.getRandomTableReference();
        try {
            // Create a new table so we don't pollute the main query table
            table.createIfNotExists();

            // Insert an entity which is missing String and IntegerPrimitive
            DynamicTableEntity entity = new DynamicTableEntity(UUID.randomUUID().toString(), UUID.randomUUID()
                    .toString());
            table.execute(TableOperation.insert(entity));

            testTableQueryProjectionWithSpecialCases(table);
        }
        finally {
            table.deleteIfExists();
        }
    }

    @Test
    public void testTableQueryProjectionWithIncorrectTypes() throws URISyntaxException, StorageException {
        CloudTable table = TableTestHelper.getRandomTableReference();
        try {
            // Create a new table so we don't pollute the main query table
            table.createIfNotExists();

            // Insert an entity with String as an int, and IntegerPrimitive as a bool
            DynamicTableEntity entity = new DynamicTableEntity(UUID.randomUUID().toString(), UUID.randomUUID()
                    .toString());
            entity.getProperties().put("String", new EntityProperty(1234));
            entity.getProperties().put("IntegerPrimitive", new EntityProperty(true));
            table.execute(TableOperation.insert(entity));

            testTableQueryProjectionWithSpecialCases(table);
        }
        finally {
            table.deleteIfExists();
        }
    }

    private void testTableQueryProjectionWithSpecialCases(CloudTable table) {
        table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(TablePayloadFormat.Json);

        // Query on String and IntegerPrimitive
        TableQuery<ComplexEntity> query = TableQuery.from(ComplexEntity.class).select(
                new String[] { "String", "IntegerPrimitive"});
        Iterable<ComplexEntity> iterable = table.execute(query);

        List<ComplexEntity> entities = new ArrayList<ComplexEntity>();
        for (ComplexEntity entity : iterable) {
            entities.add(entity);
        }

        // Verify A has a set value and B and E have class defaults
        assertEquals(1, entities.size());
        ComplexEntity entity = entities.get(0);
        assertNull(entity.getString());
        assertEquals(-1, entity.getIntegerPrimitive());
    }

    @Test
    public void testTableQueryWithSpecialChars() throws StorageException, URISyntaxException {
        CloudTable table = TableTestHelper.getRandomTableReference();

        try {
            table.createIfNotExists();

            testTableQueryWithSpecialChars('\'', table);
            testTableQueryWithSpecialChars('=', table);
            testTableQueryWithSpecialChars('_', table);
            testTableQueryWithSpecialChars(' ', table);
            testTableQueryWithSpecialChars('界', table);
        }
        finally {
            table.deleteIfExists();
        }
    }

    private void testTableQueryWithSpecialChars(char charToTest, CloudTable table)
            throws StorageException, URISyntaxException {
        String partitionKey = "partition" + charToTest + "key";
        String rowKey = "row" + charToTest + "key";

        EmptyClass ref = new EmptyClass();
        ref.setPartitionKey(partitionKey);
        ref.setRowKey(rowKey);

        table.execute(TableOperation.insert(ref));
        String condition = TableQuery.generateFilterCondition(TableConstants.PARTITION_KEY, QueryComparisons.EQUAL, partitionKey);
        ResultSegment<EmptyClass> seg = table.executeSegmented(TableQuery.from(EmptyClass.class).where(condition), null);

        assertEquals(1, seg.getLength());
        assertEquals(partitionKey, seg.getResults().get(0).getPartitionKey());
    }

    @Test
    public void testTableInvalidQuery() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        TableQuery<Class1> query = TableQuery.from(Class1.class).where(
                String.format("(PartitionKey ) and (RowKey ge '%s')", "000050"));
        try {
            table.executeSegmented(query, null, options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("A binary operator with incompatible types was detected. Found operand types 'Edm.String' and 'Edm.Boolean' for operator kind 'And'."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
        }
    }

    @Test
    public void testQueryOnSupportedTypes() throws StorageException, InterruptedException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testQueryOnSupportedTypes(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testQueryOnSupportedTypes(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testQueryOnSupportedTypes(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testQueryOnSupportedTypes(options, true);
    }

    private void testQueryOnSupportedTypes(TableRequestOptions options, boolean usePropertyResolver)
            throws StorageException, InterruptedException {
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

        if (usePropertyResolver) {
            options.setPropertyResolver(middleRef);
        }

        table.execute(batch, options, null);

        try {
            // 1. Filter on String
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("String", QueryComparisons.GREATER_THAN_OR_EQUAL, "0050"), 50,
                    options, usePropertyResolver);

            // 2. Filter on UUID
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Guid", QueryComparisons.EQUAL, middleRef.getGuid()), 1,
                    options, usePropertyResolver);

            // 3. Filter on Long
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Int64", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getInt64()), 50, options, usePropertyResolver);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("LongPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getInt64()), 50, options, usePropertyResolver);

            // 4. Filter on Double
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Double", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getDouble()), 50, options, usePropertyResolver);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("DoublePrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getDouble()), 50, options, usePropertyResolver);

            // 5. Filter on Integer
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Int32", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getInt32()), 50, options, usePropertyResolver);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("IntegerPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getInt32()), 50, options, usePropertyResolver);

            // 6. Filter on Date
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("DateTime", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getDateTime()), 50, options, usePropertyResolver);

            // 7. Filter on Boolean
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Bool", QueryComparisons.EQUAL, middleRef.getBool()), 50,
                    options, usePropertyResolver);

            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("BoolPrimitive", QueryComparisons.EQUAL, middleRef.getBool()),
                    50, options, usePropertyResolver);

            // 8. Filter on Binary
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Binary", QueryComparisons.EQUAL, middleRef.getBinary()), 1,
                    options, usePropertyResolver);

            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("BinaryPrimitive", QueryComparisons.EQUAL,
                            middleRef.getBinaryPrimitive()), 1, options, usePropertyResolver);

            // 9. Filter on Binary GTE
            executeQueryAndAssertResults(
                    TableQuery.generateFilterCondition("Binary", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getBinary()), 50, options, usePropertyResolver);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("BinaryPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getBinaryPrimitive()), 50, options,
                    usePropertyResolver);

            // 10. Complex Filter on Binary GTE
            executeQueryAndAssertResults(TableQuery.combineFilters(
                    TableQuery.generateFilterCondition(TableConstants.PARTITION_KEY, QueryComparisons.EQUAL,
                            middleRef.getPartitionKey()),
                    TableQuery.Operators.AND,
                    TableQuery.generateFilterCondition("Binary", QueryComparisons.GREATER_THAN_OR_EQUAL,
                            middleRef.getBinary())), 50, options, usePropertyResolver);

            executeQueryAndAssertResults(TableQuery.generateFilterCondition("BinaryPrimitive",
                    QueryComparisons.GREATER_THAN_OR_EQUAL, middleRef.getBinaryPrimitive()), 50, options,
                    usePropertyResolver);

        }
        finally {
            // cleanup
            TableBatchOperation delBatch = new TableBatchOperation();
            TableQuery<ComplexEntity> query = TableQuery.from(ComplexEntity.class).where(
                    String.format("PartitionKey eq '%s'", pk));

            for (ComplexEntity e : table.execute(query, options, null)) {
                delBatch.delete(e);
            }

            table.execute(delBatch, options, null);
        }
    }

    private void executeQueryAndAssertResults(String filter, int expectedResults, TableRequestOptions options,
            boolean usePropertyResolver) {
        // instantiate class to use property resolver
        ComplexEntity ent = new ComplexEntity();
        if (usePropertyResolver) {
            options.setPropertyResolver(ent);
        }

        int count = 0;
        TableQuery<ComplexEntity> query = TableQuery.from(ComplexEntity.class).where(filter);
        for (@SuppressWarnings("unused")
        ComplexEntity e : table.execute(query, options, null)) {
            count++;
        }

        assertEquals(expectedResults, count);
    }

    @Test
    public void testTableQueryIterateTwice() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryIterateTwice(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryIterateTwice(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryIterateTwice(options);
    }

    private void testTableQueryIterateTwice(TableRequestOptions options) {
        // Create entity to check against
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        final Iterable<DynamicTableEntity> result = table.execute(TableQuery.from(DynamicTableEntity.class).take(50),
                options, null);

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
    public void testTableQueryWithDynamicEntity() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithDynamicEntity(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithDynamicEntity(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithDynamicEntity(options);
    }

    private void testTableQueryWithDynamicEntity(TableRequestOptions options) {
        // Create entity to check against
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        final Iterable<DynamicTableEntity> result = table.execute(TableQuery.from(DynamicTableEntity.class), options,
                null);

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
    public void testTableQueryWithProjection() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithProjection(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithProjection(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithProjection(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithProjection(options, true);
    }

    private void testTableQueryWithProjection(TableRequestOptions options, boolean usePropertyResolver) {
        // Create entity to check against
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final Iterable<Class1> result = table.execute(TableQuery.from(Class1.class).select(new String[] { "A", "C" }),
                options, null);

        // Validate results
        for (Class1 ent : result) {
            // Validate core properties were sent.
            assertNotNull(ent.getPartitionKey());
            assertNotNull(ent.getRowKey());
            assertNotNull(ent.getTimestamp());

            // Validate correct column returned.
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), null);
            assertEquals(ent.getC(), randEnt.getC());
            assertEquals(ent.getD(), null);
        }
    }

    @Test
    public void testSelectOnlySendsReservedColumnsOnce() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testSelectOnlySendsReservedColumnsOnce(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testSelectOnlySendsReservedColumnsOnce(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testSelectOnlySendsReservedColumnsOnce(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testSelectOnlySendsReservedColumnsOnce(options, true);
    }

    private void testSelectOnlySendsReservedColumnsOnce(TableRequestOptions options, boolean usePropertyResolver) {
        // Create entity to use property resolver
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
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

        final Iterable<Class1> result = table.execute(
                TableQuery.from(Class1.class).select(new String[] { "PartitionKey", "RowKey", "Timestamp" }), options,
                opContext);

        // Validate results
        for (Class1 ent : result) {
            assertEquals(ent.getA(), null);
            assertEquals(ent.getB(), null);
            assertEquals(ent.getC(), null);
            assertEquals(ent.getD(), null);
        }
    }

    @Test
    public void testTableQueryWithReflection() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithReflection(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithReflection(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithReflection(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithReflection(options, true);
    }

    private void testTableQueryWithReflection(TableRequestOptions options, boolean usePropertyResolver) {
        // Create entity to check against
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final Iterable<Class1> result = table.execute(TableQuery.from(Class1.class), options, null);

        // Validate results
        for (Class1 ent : result) {
            assertEquals(ent.getA(), randEnt.getA());
            assertEquals(ent.getB(), randEnt.getB());
            assertEquals(ent.getC(), randEnt.getC());
            assertTrue(Arrays.equals(ent.getD(), randEnt.getD()));
        }
    }

    @Test
    public void testTableQueryWithEntityResolver() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithEntityResolver(options, true);
    }

    private void testTableQueryWithEntityResolver(TableRequestOptions options, boolean usePropertyResolver) {
        // Create entity to check against
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final Iterable<Class1> result = table.execute(TableQuery.from(TableServiceEntity.class),
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
    public void testTableQueryWithTake() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithTake(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithTake(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithTake(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithTake(options, true);
    }

    private void testTableQueryWithTake(TableRequestOptions options, boolean usePropertyResolver)
            throws StorageException {
        // Create entity to check against
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        final ResultSegment<Class1> result = table.executeSegmented(
                TableQuery.from(Class1.class).select(new String[] { "A", "C" }).take(25), null, options, null);

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
    public void testTableQueryWithFilter() {
        TableRequestOptions options = new TableRequestOptions();
        
        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithFilter(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithFilter(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithFilter(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithFilter(options, true);
    }

    private void testTableQueryWithFilter(TableRequestOptions options, boolean usePropertyResolver) {
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        TableQuery<Class1> query = TableQuery.from(Class1.class).where(
                String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

        int count = 0;

        for (Class1 ent : table.execute(query, options, null)) {
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
    public void testTableQueryWithContinuation() {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testTableQueryWithContinuation(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableQueryWithContinuation(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithContinuation(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testTableQueryWithContinuation(options, true);
    }

    private void testTableQueryWithContinuation(TableRequestOptions options, boolean usePropertyResolver) {
        Class1 randEnt = TableTestHelper.generateRandomEntity(null);

        if (usePropertyResolver) {
            options.setPropertyResolver(randEnt);
        }

        TableQuery<Class1> query = TableQuery.from(Class1.class)
                .where(String.format("(PartitionKey ge '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"))
                .take(25);

        // take will cause the query to return 25 at a time

        int count = 0;
        int pk = 1;
        for (Class1 ent : table.execute(query, options, null)) {
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
}
