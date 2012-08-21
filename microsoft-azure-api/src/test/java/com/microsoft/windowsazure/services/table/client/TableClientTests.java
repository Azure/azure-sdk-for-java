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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Client Tests
 */
public class TableClientTests extends TableTestBase {
    @Test
    public void listTablesSegmented() throws IOException, URISyntaxException, StorageException {
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
            ResultSegment<String> segment1 = tClient.listTablesSegmented(tableBaseName, 5, null, null, null);
            Assert.assertEquals(5, segment1.getLength());
            for (String s : segment1.getResults()) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            ResultSegment<String> segment2 = tClient.listTablesSegmented(tableBaseName, 5,
                    segment1.getContinuationToken(), null, null);
            Assert.assertEquals(5, segment2.getLength());
            for (String s : segment2.getResults()) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            ResultSegment<String> segment3 = tClient.listTablesSegmented(tableBaseName, 5,
                    segment2.getContinuationToken(), null, null);
            Assert.assertEquals(5, segment3.getLength());
            for (String s : segment3.getResults()) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
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
    public void listTablesSegmentedNoPrefix() throws IOException, URISyntaxException, StorageException {
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
            ResultSegment<String> segment1 = tClient.listTablesSegmented(null, 5, null, null, null);
            Assert.assertEquals(5, segment1.getLength());
            for (String s : segment1.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    Assert.assertEquals(s,
                            String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }
            }

            ResultSegment<String> segment2 = tClient.listTablesSegmented(null, 5, segment1.getContinuationToken(),
                    null, null);
            Assert.assertEquals(5, segment2.getLength());
            for (String s : segment2.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    Assert.assertEquals(s,
                            String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }
            }

            ResultSegment<String> segment3 = tClient.listTablesSegmented(null, 5, segment2.getContinuationToken(),
                    null, null);
            Assert.assertEquals(5, segment3.getLength());
            for (String s : segment3.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    Assert.assertEquals(s,
                            String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
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
    public void listTablesWithIterator() throws IOException, URISyntaxException, StorageException {
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
            Iterable<String> listTables = tClient.listTables(tableBaseName, null, null);
            for (String s : listTables) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            Assert.assertEquals(20, currTable);
            // Second Iteration
            currTable = 0;
            for (String s : listTables) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }
            Assert.assertEquals(20, currTable);

            // Without prefix
            currTable = 0;
            Iterable<String> listTablesNoPrefix = tClient.listTables();
            for (String s : listTablesNoPrefix) {
                if (s.startsWith(tableBaseName)) {
                    currTable++;
                }
            }

            Assert.assertEquals(20, currTable);
            currTable = 0;
            for (String s : listTablesNoPrefix) {
                if (s.startsWith(tableBaseName)) {
                    currTable++;
                }
            }

            Assert.assertEquals(20, currTable);
        }
        finally {
            for (String s : tables) {
                CloudTable table = tClient.getTableReference(s);
                table.delete();
            }
        }
    }

    @Test
    public void tableCreateAndAttemptCreateOnceExists() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        try {
            table.create();
            Assert.assertTrue(table.exists());

            // Should fail as it already exists
            try {
                table.create();
                fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(ex.getErrorCode(), "TableAlreadyExists");
            }
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void tableCreateExistsAndDelete() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        try {
            Assert.assertTrue(table.createIfNotExist());
            Assert.assertTrue(table.exists());
            Assert.assertTrue(table.deleteIfExists());
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void tableCreateIfNotExists() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        try {
            Assert.assertTrue(table.createIfNotExist());
            Assert.assertTrue(table.exists());
            Assert.assertFalse(table.createIfNotExist());
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void tableDeleteIfExists() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);

        Assert.assertFalse(table.deleteIfExists());

        table.create();
        Assert.assertTrue(table.exists());
        Assert.assertTrue(table.deleteIfExists());
        Assert.assertFalse(table.deleteIfExists());
    }

    @Test
    public void tableDeleteWhenExistAndNotExists() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);

        try {
            // Should fail as it doesnt already exists
            try {
                table.delete();
                fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(ex.getMessage(), "Not Found");
            }

            table.create();
            Assert.assertTrue(table.exists());
            table.delete();
            Assert.assertFalse(table.exists());
        }
        finally {
            table.deleteIfExists();
        }
    }

    @Test
    public void tableDoesTableExist() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);

        try {
            Assert.assertFalse(table.exists());
            Assert.assertTrue(table.createIfNotExist());
            Assert.assertTrue(table.exists());
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void tableGetSetPermissionTest() throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        table.create();

        TablePermissions expectedPermissions;
        TablePermissions testPermissions;

        try {
            // Test new permissions.
            expectedPermissions = new TablePermissions();
            testPermissions = table.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);

            // Test setting empty permissions.
            table.uploadPermissions(expectedPermissions);
            testPermissions = table.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);

            // Add a policy, check setting and getting.
            SharedAccessTablePolicy policy1 = new SharedAccessTablePolicy();
            Calendar now = GregorianCalendar.getInstance();
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 10);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessTablePermissions.ADD, SharedAccessTablePermissions.QUERY,
                    SharedAccessTablePermissions.UPDATE, SharedAccessTablePermissions.DELETE));
            expectedPermissions.getSharedAccessPolicies().put(UUID.randomUUID().toString(), policy1);

            table.uploadPermissions(expectedPermissions);
            testPermissions = table.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    static void assertTablePermissionsEqual(TablePermissions expected, TablePermissions actual) {
        HashMap<String, SharedAccessTablePolicy> expectedPolicies = expected.getSharedAccessPolicies();
        HashMap<String, SharedAccessTablePolicy> actualPolicies = actual.getSharedAccessPolicies();
        Assert.assertEquals("SharedAccessPolicies.Count", expectedPolicies.size(), actualPolicies.size());
        for (String name : expectedPolicies.keySet()) {
            Assert.assertTrue("Key" + name + " doesn't exist", actualPolicies.containsKey(name));
            SharedAccessTablePolicy expectedPolicy = expectedPolicies.get(name);
            SharedAccessTablePolicy actualPolicy = actualPolicies.get(name);
            Assert.assertEquals("Policy: " + name + "\tPermissions\n", expectedPolicy.getPermissions().toString(),
                    actualPolicy.getPermissions().toString());
            Assert.assertEquals("Policy: " + name + "\tStartDate\n", expectedPolicy.getSharedAccessStartTime()
                    .toString(), actualPolicy.getSharedAccessStartTime().toString());
            Assert.assertEquals("Policy: " + name + "\tExpireDate\n", expectedPolicy.getSharedAccessExpiryTime()
                    .toString(), actualPolicy.getSharedAccessExpiryTime().toString());

        }

    }

    @Test
    public void testTableSASFromIdentifier() throws StorageException, URISyntaxException, InvalidKeyException {
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

            // Insert 500 entities in Batches to query
            for (int i = 0; i < 5; i++) {
                TableBatchOperation batch = new TableBatchOperation();

                for (int j = 0; j < 100; j++) {
                    class1 ent = generateRandomEnitity("javatables_batch_" + Integer.toString(i));
                    ent.setRowKey(String.format("%06d", j));
                    batch.insert(ent);
                }

                tClient.execute(name, batch);
            }

            CloudTableClient tableClientFromIdentifierSAS = getTableForSas(table, null, identifier, null, null, null,
                    null);

            {
                class1 randEnt = TableTestBase.generateRandomEnitity(null);
                TableQuery<class1> query = TableQuery.from(name, class1.class).where(
                        String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

                int count = 0;

                for (class1 ent : tableClientFromIdentifierSAS.execute(query)) {
                    Assert.assertEquals(ent.getA(), randEnt.getA());
                    Assert.assertEquals(ent.getB(), randEnt.getB());
                    Assert.assertEquals(ent.getC(), randEnt.getC());
                    Assert.assertEquals(ent.getPartitionKey(), "javatables_batch_1");
                    Assert.assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
                    count++;
                }

                Assert.assertEquals(count, 50);
            }

            {
                class1 baseEntity = new class1();
                baseEntity.setA("foo_A");
                baseEntity.setB("foo_B");
                baseEntity.setC("foo_C");
                baseEntity.setD(new byte[] { 0, 1, 2 });
                baseEntity.setPartitionKey("jxscl_odata");
                baseEntity.setRowKey(UUID.randomUUID().toString());

                class2 secondEntity = new class2();
                secondEntity.setL("foo_L");
                secondEntity.setM("foo_M");
                secondEntity.setN("foo_N");
                secondEntity.setO("foo_O");
                secondEntity.setPartitionKey(baseEntity.getPartitionKey());
                secondEntity.setRowKey(baseEntity.getRowKey());
                secondEntity.setEtag(baseEntity.getEtag());

                // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.
                TableResult insertResult = tableClientFromIdentifierSAS.execute(name,
                        TableOperation.insertOrMerge(baseEntity));

                Assert.assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

                // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
                tableClientFromIdentifierSAS.execute(name, TableOperation.insertOrMerge(secondEntity));

                // Retrieve entity
                TableResult queryResult = tableClientFromIdentifierSAS.execute(name, TableOperation.retrieve(
                        baseEntity.getPartitionKey(), baseEntity.getRowKey(), DynamicTableEntity.class));

                DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

                Assert.assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
                Assert.assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

                Assert.assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
                Assert.assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

                Assert.assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
                Assert.assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

                Assert.assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
                Assert.assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                        .getValueAsByteArray()));

                // Validate New properties exist
                Assert.assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
                Assert.assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

                Assert.assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
                Assert.assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

                Assert.assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
                Assert.assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

                Assert.assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
                Assert.assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
            }
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void testTableSASFromPermission() throws StorageException, URISyntaxException, InvalidKeyException {
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

            policy1.setPermissions(EnumSet.of(SharedAccessTablePermissions.QUERY, SharedAccessTablePermissions.UPDATE,
                    SharedAccessTablePermissions.DELETE));
            expectedPermissions.getSharedAccessPolicies().put(identifier, policy1);

            table.uploadPermissions(expectedPermissions);

            // Insert 500 entities in Batches to query
            for (int i = 0; i < 5; i++) {
                TableBatchOperation batch = new TableBatchOperation();

                for (int j = 0; j < 100; j++) {
                    class1 ent = generateRandomEnitity("javatables_batch_" + Integer.toString(i));
                    ent.setRowKey(String.format("%06d", j));
                    batch.insert(ent);
                }

                tClient.execute(name, batch);
            }

            CloudTableClient tableClientFromPermission = getTableForSas(table, policy1, null, "javatables_batch_0",
                    "0", "javatables_batch_9", "9");
            CloudTableClient tableClientFromPermissionJustPks = getTableForSas(table, policy1, null,
                    "javatables_batch_0", null, "javatables_batch_9", null);

            {
                TableBatchOperation batchFromSAS = new TableBatchOperation();

                for (int j = 1000; j < 1010; j++) {
                    class1 ent = generateRandomEnitity("javatables_batch_" + Integer.toString(0));
                    ent.setRowKey(String.format("%06d", j));
                    batchFromSAS.insert(ent);
                }

                tableClientFromPermission.execute(name, batchFromSAS);

                class1 randEnt = TableTestBase.generateRandomEnitity(null);
                TableQuery<class1> query = TableQuery.from(name, class1.class).where(
                        String.format("(PartitionKey eq '%s') and (RowKey ge '%s')", "javatables_batch_1", "000050"));

                int count = 0;

                for (class1 ent : tableClientFromPermission.execute(query)) {
                    Assert.assertEquals(ent.getA(), randEnt.getA());
                    Assert.assertEquals(ent.getB(), randEnt.getB());
                    Assert.assertEquals(ent.getC(), randEnt.getC());
                    Assert.assertEquals(ent.getPartitionKey(), "javatables_batch_1");
                    Assert.assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
                    count++;
                }

                Assert.assertEquals(count, 50);

                count = 0;

                for (class1 ent : tableClientFromPermissionJustPks.execute(query)) {
                    Assert.assertEquals(ent.getA(), randEnt.getA());
                    Assert.assertEquals(ent.getB(), randEnt.getB());
                    Assert.assertEquals(ent.getC(), randEnt.getC());
                    Assert.assertEquals(ent.getPartitionKey(), "javatables_batch_1");
                    Assert.assertEquals(ent.getRowKey(), String.format("%06d", count + 50));
                    count++;
                }

                Assert.assertEquals(count, 50);
            }

            {
                class1 baseEntity = new class1();
                baseEntity.setA("foo_A");
                baseEntity.setB("foo_B");
                baseEntity.setC("foo_C");
                baseEntity.setD(new byte[] { 0, 1, 2 });
                baseEntity.setPartitionKey("javatables_batch_0" + UUID.randomUUID().toString());
                baseEntity.setRowKey("0" + UUID.randomUUID().toString());

                class2 secondEntity = new class2();
                secondEntity.setL("foo_L");
                secondEntity.setM("foo_M");
                secondEntity.setN("foo_N");
                secondEntity.setO("foo_O");
                secondEntity.setPartitionKey(baseEntity.getPartitionKey());
                secondEntity.setRowKey(baseEntity.getRowKey());
                secondEntity.setEtag(baseEntity.getEtag());

                TableResult insertResult = tableClientFromPermission.execute(name,
                        TableOperation.insertOrMerge(baseEntity));

                // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.

                Assert.assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

                // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
                tableClientFromPermission.execute(name, TableOperation.insertOrMerge(secondEntity));

                // Retrieve entity
                TableResult queryResult = tableClientFromPermission.execute(name, TableOperation.retrieve(
                        baseEntity.getPartitionKey(), baseEntity.getRowKey(), DynamicTableEntity.class));

                DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

                Assert.assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
                Assert.assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

                Assert.assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
                Assert.assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

                Assert.assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
                Assert.assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

                Assert.assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
                Assert.assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                        .getValueAsByteArray()));

                // Validate New properties exist
                Assert.assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
                Assert.assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

                Assert.assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
                Assert.assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

                Assert.assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
                Assert.assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

                Assert.assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
                Assert.assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
            }
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    private CloudTableClient getTableForSas(CloudTable table, SharedAccessTablePolicy policy, String accessIdentifier,
            String startPk, String startRk, String endPk, String endRk) throws InvalidKeyException, StorageException {
        String sasString = table
                .generateSharedAccessSignature(policy, accessIdentifier, startPk, startRk, endPk, endRk);
        return new CloudTableClient(tClient.getEndpoint(), new StorageCredentialsSharedAccessSignature(sasString));
    }
}
