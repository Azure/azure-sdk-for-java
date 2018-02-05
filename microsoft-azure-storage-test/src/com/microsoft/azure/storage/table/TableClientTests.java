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
import java.security.InvalidKeyException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.LocationMode;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestResult;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.RetryContext;
import com.microsoft.azure.storage.RetryExponentialRetry;
import com.microsoft.azure.storage.RetryInfo;
import com.microsoft.azure.storage.RetryLinearRetry;
import com.microsoft.azure.storage.RetryPolicy;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageLocation;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.table.TableTestHelper.Class1;
import com.microsoft.azure.storage.table.TableTestHelper.Class2;

import static org.junit.Assert.*;

/**
 * Table Client Tests
 */
public class TableClientTests {

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListTablesSegmented() throws URISyntaxException, StorageException {
        TableRequestOptions options = new TableRequestOptions();
        TablePayloadFormat[] formats =
                {TablePayloadFormat.JsonFullMetadata,
                TablePayloadFormat.Json,
                TablePayloadFormat.JsonNoMetadata};

        for (TablePayloadFormat format : formats) {
            options.setTablePayloadFormat(format);
            testListTablesSegmented(options);
        }
    }

    private void testListTablesSegmented(TableRequestOptions options) throws URISyntaxException, StorageException {
        final CloudTableClient tClient = TableTestHelper.createCloudTableClient();
        String tableBaseName = TableTestHelper.generateRandomTableName();

        ArrayList<String> tables = new ArrayList<String>();
        for (int m = 0; m < 20; m++) {
            String name = String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(m));
            CloudTable table = tClient.getTableReference(name);
            table.create();
            tables.add(name);
        }

        try {
            int currTable = 0;
            ResultSegment<String> segment1 = tClient.listTablesSegmented(tableBaseName, 5, null, options, null);
            assertEquals(5, segment1.getLength());
            for (String s : segment1.getResults()) {
                assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            ResultSegment<String> segment2 = tClient.listTablesSegmented(tableBaseName, 5,
                    segment1.getContinuationToken(), options, null);
            assertEquals(5, segment2.getLength());
            for (String s : segment2.getResults()) {
                assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            ResultSegment<String> segment3 = tClient.listTablesSegmented(tableBaseName, 5,
                    segment2.getContinuationToken(), options, null);
            assertEquals(5, segment3.getLength());
            for (String s : segment3.getResults()) {
                assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }
        }
        finally {
            for (String s : tables) {
                CloudTable table = tClient.getTableReference(s);
                table.delete();
            }
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListTablesSegmentedMaxResultsValidation()
            throws URISyntaxException, StorageException {
        final CloudTableClient tClient = TableTestHelper.createCloudTableClient();

        // Validation should cause each of these to fail.
        for (int i = 0; i >= -2; i--) {
            try {
                tClient.listTablesSegmented(null, i, null, null, null);
                fail();
            }
            catch (IllegalArgumentException e) {
                assertTrue(String.format(SR.PARAMETER_SHOULD_BE_GREATER_OR_EQUAL, "maxResults", 1)
                        .equals(e.getMessage()));
            }
        }

        assertNotNull(tClient.listTablesSegmented("thereshouldntbeanytableswiththisprefix"));
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListTablesSegmentedNoPrefix() throws URISyntaxException, StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        final CloudTableClient tClient = TableTestHelper.createCloudTableClient();
        String tableBaseName = TableTestHelper.generateRandomTableName();

        ArrayList<String> tables = new ArrayList<String>();
        for (int m = 0; m < 20; m++) {
            String name = String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(m));
            CloudTable table = tClient.getTableReference(name);
            table.create();
            tables.add(name);
        }

        try {
            int currTable = 0;
            ResultSegment<String> segment1 = tClient.listTablesSegmented(null, 5, null, options, null);
            assertEquals(5, segment1.getLength());
            for (String s : segment1.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }
            }

            ResultSegment<String> segment2 = tClient.listTablesSegmented(null, 5, segment1.getContinuationToken(),
                    options, null);
            assertEquals(5, segment2.getLength());
            for (String s : segment2.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }
            }

            ResultSegment<String> segment3 = tClient.listTablesSegmented(null, 5, segment2.getContinuationToken(),
                    options, null);
            assertEquals(5, segment3.getLength());
            for (String s : segment3.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }

            }
        }
        finally {
            for (String s : tables) {
                CloudTable table = tClient.getTableReference(s);
                table.delete();
            }
        }
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testListTablesWithIterator() throws URISyntaxException, StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        final CloudTableClient tClient = TableTestHelper.createCloudTableClient();
        String tableBaseName = TableTestHelper.generateRandomTableName();

        ArrayList<String> tables = new ArrayList<String>();
        for (int m = 0; m < 20; m++) {
            String name = String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(m));
            CloudTable table = tClient.getTableReference(name);
            table.create();
            tables.add(name);
        }

        try {
            // With prefix
            int currTable = 0;
            Iterable<String> listTables = tClient.listTables(tableBaseName, options, null);
            for (String s : listTables) {
                assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            assertEquals(20, currTable);
            // Second Iteration
            currTable = 0;
            for (String s : listTables) {
                assertEquals(s, String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }
            assertEquals(20, currTable);

            // Without prefix
            currTable = 0;
            Iterable<String> listTablesNoPrefix = tClient.listTables();
            for (String s : listTablesNoPrefix) {
                if (s.startsWith(tableBaseName)) {
                    currTable++;
                }
            }

            assertEquals(20, currTable);
            currTable = 0;
            for (String s : listTablesNoPrefix) {
                if (s.startsWith(tableBaseName)) {
                    currTable++;
                }
            }

            assertEquals(20, currTable);
        }
        finally {
            for (String s : tables) {
                CloudTable table = tClient.getTableReference(s);
                table.delete();
            }
        }
    }

    @Test
    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableSASFromIdentifier() throws StorageException, URISyntaxException, InvalidKeyException,
            InterruptedException {
        CloudTable table = TableTestHelper.getRandomTableReference();
        try {
            table.create();

            TablePermissions expectedPermissions = new TablePermissions();
            String identifier = UUID.randomUUID().toString();
            // Add a policy, check setting and getting.
            SharedAccessTablePolicy policy1 = new SharedAccessTablePolicy();
            Calendar now = GregorianCalendar.getInstance();
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 10);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessTablePermissions.ADD, SharedAccessTablePermissions.QUERY,
                    SharedAccessTablePermissions.UPDATE, SharedAccessTablePermissions.DELETE));
            expectedPermissions.getSharedAccessPolicies().put(identifier, policy1);

            table.uploadPermissions(expectedPermissions);
            Thread.sleep(30000);

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

            String sasString = table.generateSharedAccessSignature(null, identifier, null, null, null, null);
            CloudTable tableFromIdentifierSAS = new CloudTable(PathUtility.addToQuery(table.getUri(), sasString));

            {
                Class1 randEnt = TableTestHelper.generateRandomEntity(null);
                TableQuery<Class1> query = TableQuery.from(Class1.class).where(
                        String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

                int count = 0;

                for (Class1 ent : tableFromIdentifierSAS.execute(query)) {
                    assertEquals(ent.getA(), randEnt.getA());
                    assertEquals(ent.getB(), randEnt.getB());
                    assertEquals(ent.getC(), randEnt.getC());
                    assertEquals(ent.getPartitionKey(), "javatables_batch_1");
                    assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
                    count++;
                }

                assertEquals(count, 50);
            }

            {
                Class1 baseEntity = new Class1();
                baseEntity.setA("foo_A");
                baseEntity.setB("foo_B");
                baseEntity.setC("foo_C");
                baseEntity.setD(new byte[] { 0, 1, 2 });
                baseEntity.setPartitionKey("jxscl_odata");
                baseEntity.setRowKey(UUID.randomUUID().toString());

                Class2 secondEntity = new Class2();
                secondEntity.setL("foo_L");
                secondEntity.setM("foo_M");
                secondEntity.setN("foo_N");
                secondEntity.setO("foo_O");
                secondEntity.setPartitionKey(baseEntity.getPartitionKey());
                secondEntity.setRowKey(baseEntity.getRowKey());
                secondEntity.setEtag(baseEntity.getEtag());

                // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.
                TableResult insertResult = tableFromIdentifierSAS.execute(TableOperation.insertOrMerge(baseEntity));

                assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

                // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
                tableFromIdentifierSAS.execute(TableOperation.insertOrMerge(secondEntity));

                // Retrieve entity
                TableResult queryResult = tableFromIdentifierSAS.execute(TableOperation.retrieve(
                        baseEntity.getPartitionKey(), baseEntity.getRowKey(), DynamicTableEntity.class));

                DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

                assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
                assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

                assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
                assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

                assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
                assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

                assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
                assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                        .getValueAsByteArray()));

                // Validate New properties exist
                assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
                assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

                assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
                assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

                assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
                assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

                assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
                assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
            }
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableSASFromPermission() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudTable table = TableTestHelper.getRandomTableReference();
        try {
            table.create();

            // Add a policy, check setting and getting.
            SharedAccessTablePolicy policy1 = new SharedAccessTablePolicy();
            Calendar now = GregorianCalendar.getInstance();
            now.add(Calendar.MINUTE, -10);
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 30);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessTablePermissions.ADD, SharedAccessTablePermissions.QUERY,
                    SharedAccessTablePermissions.UPDATE, SharedAccessTablePermissions.DELETE));

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

            String sasString = table.generateSharedAccessSignature(policy1, null, "javatables_batch_0", "0",
                    "javatables_batch_9", "9");
            CloudTable tableFromPermission = new CloudTable(PathUtility.addToQuery(table.getUri(), sasString));

            sasString = table.generateSharedAccessSignature(policy1, null, "javatables_batch_0", null,
                    "javatables_batch_9", null);
            CloudTable tableFromPermissionJustPks = new CloudTable(PathUtility.addToQuery(table.getUri(), sasString));

            {
                TableBatchOperation batchFromSAS = new TableBatchOperation();

                for (int j = 1000; j < 1010; j++) {
                    Class1 ent = TableTestHelper.generateRandomEntity("javatables_batch_" + Integer.toString(0));
                    ent.setRowKey(String.format("%06d", j));
                    batchFromSAS.insert(ent);
                }

                tableFromPermission.execute(batchFromSAS);

                Class1 randEnt = TableTestHelper.generateRandomEntity(null);
                TableQuery<Class1> query = TableQuery.from(Class1.class).where(
                        String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

                int count = 0;

                for (Class1 ent : tableFromPermission.execute(query)) {
                    assertEquals(ent.getA(), randEnt.getA());
                    assertEquals(ent.getB(), randEnt.getB());
                    assertEquals(ent.getC(), randEnt.getC());
                    assertEquals(ent.getPartitionKey(), "javatables_batch_1");
                    assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
                    count++;
                }

                assertEquals(count, 50);

                count = 0;

                for (Class1 ent : tableFromPermissionJustPks.execute(query)) {
                    assertEquals(ent.getA(), randEnt.getA());
                    assertEquals(ent.getB(), randEnt.getB());
                    assertEquals(ent.getC(), randEnt.getC());
                    assertEquals(ent.getPartitionKey(), "javatables_batch_1");
                    assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
                    count++;
                }

                assertEquals(count, 50);
            }

            {
                Class1 baseEntity = new Class1();
                baseEntity.setA("foo_A");
                baseEntity.setB("foo_B");
                baseEntity.setC("foo_C");
                baseEntity.setD(new byte[] { 0, 1, 2 });
                baseEntity.setPartitionKey("javatables_batch_0" + UUID.randomUUID().toString());
                baseEntity.setRowKey("0" + UUID.randomUUID().toString());

                Class2 secondEntity = new Class2();
                secondEntity.setL("foo_L");
                secondEntity.setM("foo_M");
                secondEntity.setN("foo_N");
                secondEntity.setO("foo_O");
                secondEntity.setPartitionKey(baseEntity.getPartitionKey());
                secondEntity.setRowKey(baseEntity.getRowKey());
                secondEntity.setEtag(baseEntity.getEtag());

                TableResult insertResult = tableFromPermission.execute(TableOperation.insertOrMerge(baseEntity));

                // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.

                assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

                // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
                tableFromPermission.execute(TableOperation.insertOrMerge(secondEntity));

                // Retrieve entity
                TableResult queryResult = tableFromPermission.execute(TableOperation.retrieve(
                        baseEntity.getPartitionKey(), baseEntity.getRowKey(), DynamicTableEntity.class));

                DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

                assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
                assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

                assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
                assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

                assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
                assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

                assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
                assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                        .getValueAsByteArray()));

                // Validate New properties exist
                assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
                assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

                assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
                assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

                assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
                assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

                assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
                assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
            }
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testTableSASPkRk() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudTable table = TableTestHelper.getRandomTableReference();
        try {
            table.create();

            // Add a policy, check setting and getting.
            SharedAccessTablePolicy policy1 = new SharedAccessTablePolicy();
            Calendar now = GregorianCalendar.getInstance();
            now.add(Calendar.MINUTE, -10);
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 30);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessTablePermissions.ADD, SharedAccessTablePermissions.QUERY,
                    SharedAccessTablePermissions.UPDATE, SharedAccessTablePermissions.DELETE));

            String sasString = table.generateSharedAccessSignature(policy1, null, "javatables_batch_0", "00",
                    "javatables_batch_1", "04");
            StorageCredentialsSharedAccessSignature sasCreds = new StorageCredentialsSharedAccessSignature(sasString);
            CloudTable directTable = new CloudTable(PathUtility.addToQuery(table.getUri(), sasString));
            CloudTable transformedTable = new CloudTable(sasCreds.transformUri(table.getUri()));

            Class1 ent = new Class1("javatables_batch_0", "00");
            directTable.execute(TableOperation.insert(ent));

            ent = new Class1("javatables_batch_0", "01");
            transformedTable.execute(TableOperation.insert(ent));

            ent = new Class1("javatables_batch_2", "01");
            try {
                directTable.execute(TableOperation.insert(ent));
                transformedTable.execute(TableOperation.insert(ent));
            }
            catch (StorageException e) {
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getHttpStatusCode());
                assertEquals("AuthorizationFailure", e.getErrorCode());
            }

            ent = new Class1("javatables_batch_1", "05");
            try {
                directTable.execute(TableOperation.insert(ent));
                transformedTable.execute(TableOperation.insert(ent));
            }
            catch (StorageException e) {
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getHttpStatusCode());
                assertEquals("AuthorizationFailure", e.getErrorCode());
            }

        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    public void testBackoffTimeOverflow() {
        RetryExponentialRetry exponentialRetry = new RetryExponentialRetry(4000, 100000);
        testBackoffTimeOverflow(exponentialRetry, 100000);

        RetryLinearRetry linearRetry = new RetryLinearRetry(4000, 100000);
        testBackoffTimeOverflow(linearRetry, 100000);
    }

    private void testBackoffTimeOverflow(RetryPolicy retryPolicy, int maxAttempts) {
        Exception e = new Exception();
        OperationContext context = new OperationContext();

        RequestResult requestResult = new RequestResult();
        requestResult.setException(e);
        requestResult.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        requestResult.setStopDate(new Date());
        requestResult.setTargetLocation(StorageLocation.PRIMARY);

        for (int i = 0; i < maxAttempts; i++) {
            RetryContext retryContext = new RetryContext(i, requestResult, StorageLocation.PRIMARY,
                    LocationMode.PRIMARY_ONLY);
            RetryInfo result = retryPolicy.evaluate(retryContext, context);
            assertNotNull(result);
        }

        RetryContext retryContext = new RetryContext(maxAttempts, requestResult, StorageLocation.PRIMARY,
                LocationMode.PRIMARY_ONLY);
        assertNull(retryPolicy.evaluate(retryContext, context));
    }

    @Test
    @Category({ CloudTests.class })
    public void testGetServiceStats() throws StorageException {
        CloudTableClient tClient = TableTestHelper.createCloudTableClient();
        tClient.getDefaultRequestOptions().setLocationMode(LocationMode.SECONDARY_ONLY);
        TableTestHelper.verifyServiceStats(tClient.getServiceStats());
    }
}
