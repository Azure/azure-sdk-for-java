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
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.RetryNoRetry;
import com.microsoft.windowsazure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.TestRunners.CloudTests;
import com.microsoft.windowsazure.storage.TestRunners.DevFabricTests;
import com.microsoft.windowsazure.storage.TestRunners.DevStoreTests;
import com.microsoft.windowsazure.storage.TestRunners.SlowTests;
import com.microsoft.windowsazure.storage.core.PathUtility;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class TableTests extends TableTestBase {

    /**
     * Get permissions from string
     */
    @Test
    public void testTablePermissionsFromString() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        Date start = cal.getTime();
        cal.add(Calendar.MINUTE, 30);
        Date expiry = cal.getTime();

        SharedAccessTablePolicy policy = new SharedAccessTablePolicy();
        policy.setSharedAccessStartTime(start);
        policy.setSharedAccessExpiryTime(expiry);

        policy.setPermissionsFromString("raud");
        assertEquals(EnumSet.of(SharedAccessTablePermissions.QUERY, SharedAccessTablePermissions.ADD,
                SharedAccessTablePermissions.UPDATE, SharedAccessTablePermissions.DELETE), policy.getPermissions());

        policy.setPermissionsFromString("rad");
        assertEquals(EnumSet.of(SharedAccessTablePermissions.QUERY, SharedAccessTablePermissions.ADD,
                SharedAccessTablePermissions.DELETE), policy.getPermissions());

        policy.setPermissionsFromString("ar");
        assertEquals(EnumSet.of(SharedAccessTablePermissions.QUERY, SharedAccessTablePermissions.ADD),
                policy.getPermissions());

        policy.setPermissionsFromString("u");
        assertEquals(EnumSet.of(SharedAccessTablePermissions.UPDATE), policy.getPermissions());
    }

    @Test
    public void testTableCreateAndAttemptCreateOnceExists() throws StorageException, IOException, URISyntaxException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableCreateAndAttemptCreateOnceExists(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableCreateAndAttemptCreateOnceExists(tClient);
    }

    private void testTableCreateAndAttemptCreateOnceExists(CloudTableClient tClient) throws StorageException,
            URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
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

    @Test
    public void testTableCreateExistsAndDelete() throws StorageException, IOException, URISyntaxException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableCreateExistsAndDelete(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableCreateExistsAndDelete(tClient);
    }

    private void testTableCreateExistsAndDelete(CloudTableClient tClient) throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        try {
            assertTrue(table.createIfNotExists());
            assertTrue(table.exists());
            assertTrue(table.deleteIfExists());
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void testTableCreateIfNotExists() throws StorageException, IOException, URISyntaxException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableCreateIfNotExists(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableCreateIfNotExists(tClient);
    }

    private void testTableCreateIfNotExists(CloudTableClient tClient) throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        try {
            assertTrue(table.createIfNotExists());
            assertTrue(table.exists());
            assertFalse(table.createIfNotExists());
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Test
    public void testTableDeleteIfExists() throws StorageException, IOException, URISyntaxException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableDeleteIfExists(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableDeleteIfExists(tClient);
    }

    private void testTableDeleteIfExists(CloudTableClient tClient) throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);
        assertFalse(table.deleteIfExists());

        table.create();
        assertTrue(table.exists());
        assertTrue(table.deleteIfExists());

        assertFalse(table.deleteIfExists());
    }

    @Test
    public void testTableDeleteWhenExistAndNotExists() throws StorageException, IOException, URISyntaxException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableDeleteWhenExistAndNotExists(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableDeleteWhenExistAndNotExists(tClient);
    }

    private void testTableDeleteWhenExistAndNotExists(CloudTableClient tClient) throws StorageException,
            URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);

        try {
            // Should fail as it doesnt already exists
            try {
                table.delete();
                fail();
            }
            catch (StorageException ex) {
                assertEquals(ex.getMessage(), "Not Found");
            }

            table.create();
            assertTrue(table.exists());
            table.delete();
            assertFalse(table.exists());
        }
        finally {
            table.deleteIfExists();
        }
    }

    @Test
    public void testTableDoesTableExist() throws StorageException, IOException, URISyntaxException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableDoesTableExist(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableDoesTableExist(tClient);
    }

    private void testTableDoesTableExist(CloudTableClient tClient) throws StorageException, URISyntaxException {
        String tableName = generateRandomTableName();
        CloudTable table = tClient.getTableReference(tableName);

        try {
            assertFalse(table.exists());
            assertTrue(table.createIfNotExists());
            assertTrue(table.exists());
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Category(SlowTests.class)
    @Test
    public void testTableGetSetPermissionTest() throws StorageException, IOException, URISyntaxException,
            InterruptedException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableGetSetPermissionTest(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableGetSetPermissionTest(tClient);
    }

    private void testTableGetSetPermissionTest(CloudTableClient tClient) throws StorageException, URISyntaxException,
            InterruptedException {
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
            Thread.sleep(30000);

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
            Thread.sleep(30000);
            testPermissions = table.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);
        }
        finally {
            // cleanup
            table.deleteIfExists();
        }
    }

    @Category(SlowTests.class)
    @Test
    public void testTableSas() throws StorageException, IOException, URISyntaxException, InvalidKeyException,
            InterruptedException {
        CloudTableClient tClient = createCloudTableClient();

        tClient.setTablePayloadFormat(TablePayloadFormat.AtomPub);
        testTableSas(tClient);

        tClient.setTablePayloadFormat(TablePayloadFormat.Json);
        testTableSas(tClient);
    }

    private void testTableSas(CloudTableClient tClient) throws InvalidKeyException, URISyntaxException,
            StorageException, InterruptedException {
        // use capital letters to make sure SAS signature converts name to lower case correctly
        String name = "CAPS" + generateRandomTableName();
        CloudTable table = tClient.getTableReference(name);
        table.create();

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

        CloudTable policySasTable = tableClientFromPermission.getTableReference(name);
        policySasTable.exists();

        // do not give the client and check that the new table's client has the correct perms
        CloudTable tableFromUri = new CloudTable(PathUtility.addToQuery(table.getStorageUri(), table
                .generateSharedAccessSignature((SharedAccessTablePolicy) null, identifier, "javatables_batch_0", "0",
                        "javatables_batch_9", "9")), null);
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), tableFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        // pass in a client which will have different permissions and check the sas permissions are used
        // and that the properties set in the old service client are passed to the new client
        CloudTableClient tableClient = policySasTable.getServiceClient();

        // set some arbitrary settings to make sure they are passed on
        tableClient.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        tableClient.setTimeoutInMs(1000);
        tableClient.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        tableClient.setRetryPolicyFactory(new RetryNoRetry());

        tableFromUri = new CloudTable(PathUtility.addToQuery(table.getStorageUri(), table
                .generateSharedAccessSignature((SharedAccessTablePolicy) null, identifier, "javatables_batch_0", "0",
                        "javatables_batch_9", "9")), tableClient);
        assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), tableFromUri.getServiceClient()
                .getCredentials().getClass().toString());

        assertEquals(tableClient.getLocationMode(), tableFromUri.getServiceClient().getLocationMode());
        assertEquals(tableClient.getTimeoutInMs(), tableFromUri.getServiceClient().getTimeoutInMs());
        assertEquals(tableClient.getTablePayloadFormat(), tableFromUri.getServiceClient().getTablePayloadFormat());
        assertEquals(tableClient.getRetryPolicyFactory().getClass(), tableFromUri.getServiceClient()
                .getRetryPolicyFactory().getClass());
    }

    private static void assertTablePermissionsEqual(TablePermissions expected, TablePermissions actual) {
        HashMap<String, SharedAccessTablePolicy> expectedPolicies = expected.getSharedAccessPolicies();
        HashMap<String, SharedAccessTablePolicy> actualPolicies = actual.getSharedAccessPolicies();
        assertEquals("SharedAccessPolicies.Count", expectedPolicies.size(), actualPolicies.size());
        for (String name : expectedPolicies.keySet()) {
            assertTrue("Key" + name + " doesn't exist", actualPolicies.containsKey(name));
            SharedAccessTablePolicy expectedPolicy = expectedPolicies.get(name);
            SharedAccessTablePolicy actualPolicy = actualPolicies.get(name);
            assertEquals("Policy: " + name + "\tPermissions\n", expectedPolicy.getPermissions().toString(),
                    actualPolicy.getPermissions().toString());
            assertEquals("Policy: " + name + "\tStartDate\n", expectedPolicy.getSharedAccessStartTime().toString(),
                    actualPolicy.getSharedAccessStartTime().toString());
            assertEquals("Policy: " + name + "\tExpireDate\n", expectedPolicy.getSharedAccessExpiryTime().toString(),
                    actualPolicy.getSharedAccessExpiryTime().toString());

        }

    }
}
