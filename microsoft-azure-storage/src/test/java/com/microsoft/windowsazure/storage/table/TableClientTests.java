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

import com.microsoft.windowsazure.storage.AuthenticationScheme;
import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.RequestResult;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.RetryContext;
import com.microsoft.windowsazure.storage.RetryExponentialRetry;
import com.microsoft.windowsazure.storage.RetryInfo;
import com.microsoft.windowsazure.storage.RetryLinearRetry;
import com.microsoft.windowsazure.storage.RetryPolicy;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageLocation;
import com.microsoft.windowsazure.storage.TestHelper;
import com.microsoft.windowsazure.storage.TestRunners.CloudTests;
import com.microsoft.windowsazure.storage.TestRunners.DevFabricTests;
import com.microsoft.windowsazure.storage.TestRunners.DevStoreTests;
import com.microsoft.windowsazure.storage.TestRunners.SlowTests;

/**
 * Table Client Tests
 */
public class TableClientTests extends TableTestBase {

    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
    public void testListTablesSegmented() throws IOException, URISyntaxException, StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testListTablesSegmented(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testListTablesSegmented(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testListTablesSegmented(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testListTablesSegmented(options);
    }

    private void testListTablesSegmented(TableRequestOptions options) throws IOException, URISyntaxException,
            StorageException {
        String tableBaseName = generateRandomTableName();

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

    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
    public void testListTablesSegmentedNoPrefix() throws IOException, URISyntaxException, StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testListTablesSegmentedNoPrefix(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testListTablesSegmentedNoPrefix(options);
    }

    private void testListTablesSegmentedNoPrefix(TableRequestOptions options) throws IOException, URISyntaxException,
            StorageException {
        String tableBaseName = generateRandomTableName();
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

    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
    public void testListTablesWithIterator() throws IOException, URISyntaxException, StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testListTablesWithIterator(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testListTablesWithIterator(options);
    }

    private void testListTablesWithIterator(TableRequestOptions options) throws IOException, URISyntaxException,
            StorageException {
        String tableBaseName = generateRandomTableName();
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

    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
    public void testTableSASFromIdentifier() throws StorageException, URISyntaxException, InvalidKeyException,
            InterruptedException {
        String name = generateRandomTableName();
        CloudTable table = tClient.getTableReference(name);
        table.create();

        try {
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
                    Class1 ent = generateRandomEntity("javatables_batch_" + Integer.toString(i));
                    ent.setRowKey(String.format("%06d", j));
                    batch.insert(ent);
                }

                table.execute(batch);
            }

            CloudTableClient tableClientFromIdentifierSAS = TableTestBase.getTableForSas(table, null, identifier, null,
                    null, null, null);
            CloudTable tableFromIdentifierSAS = tableClientFromIdentifierSAS.getTableReference(table.getName());

            {
                Class1 randEnt = TableTestBase.generateRandomEntity(null);
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

    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
    public void testTableSASFromPermission() throws StorageException, URISyntaxException, InvalidKeyException,
            InterruptedException {
        String name = generateRandomTableName();
        CloudTable table = tClient.getTableReference(name);
        table.create();

        try {
            TablePermissions expectedPermissions = new TablePermissions();
            String identifier = UUID.randomUUID().toString();
            // Add a policy, check setting and getting.
            SharedAccessTablePolicy policy1 = new SharedAccessTablePolicy();
            Calendar now = GregorianCalendar.getInstance();
            now.add(Calendar.MINUTE, -10);
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 30);
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
                    Class1 ent = generateRandomEntity("javatables_batch_" + Integer.toString(i));
                    ent.setRowKey(String.format("%06d", j));
                    batch.insert(ent);
                }

                table.execute(batch);
            }

            CloudTableClient tableClientFromPermission = TableTestBase.getTableForSas(table, policy1, null,
                    "javatables_batch_0", "0", "javatables_batch_9", "9");
            CloudTable tableFromPermission = tableClientFromPermission.getTableReference(table.getName());

            CloudTableClient tableClientFromPermissionJustPks = TableTestBase.getTableForSas(table, policy1, null,
                    "javatables_batch_0", null, "javatables_batch_9", null);
            CloudTable tableFromPermissionJustPks = tableClientFromPermissionJustPks.getTableReference(table.getName());

            {
                TableBatchOperation batchFromSAS = new TableBatchOperation();

                for (int j = 1000; j < 1010; j++) {
                    Class1 ent = generateRandomEntity("javatables_batch_" + Integer.toString(0));
                    ent.setRowKey(String.format("%06d", j));
                    batchFromSAS.insert(ent);
                }

                tableFromPermission.execute(batchFromSAS);

                Class1 randEnt = TableTestBase.generateRandomEntity(null);
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

    @Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
    public void tableCreateAndAttemptCreateOnceExistsSharedKeyLite() throws StorageException, URISyntaxException {
        tClient.setAuthenticationScheme(AuthenticationScheme.SHAREDKEYLITE);
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        try {
            table.create();
            assertTrue(table.exists());

            // Should fail as it already exists
            try {
                table.create();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(ex.getErrorCode(), "TableAlreadyExists");
            }
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
    @Test
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

    @Category({ CloudTests.class })
    @Test
    public void testGetServiceStats() throws StorageException {
        CloudTableClient tClient = createCloudTableClient();
        tClient.setLocationMode(LocationMode.SECONDARY_ONLY);
        TestHelper.verifyServiceStats(tClient.getServiceStats());
    }
}
