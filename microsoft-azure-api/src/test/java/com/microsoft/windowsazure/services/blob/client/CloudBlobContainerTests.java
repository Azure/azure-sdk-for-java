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
package com.microsoft.windowsazure.services.blob.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.AuthenticationScheme;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.SendingRequestEvent;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageEvent;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Client Tests
 */
public class CloudBlobContainerTests extends BlobTestBase {
    /**
     * test SharedAccess of container.
     * 
     * @XSCLCaseName ContainerInfoSharedAccess
     */
    @Test
    public void testContainerSaS() throws InvalidKeyException, IllegalArgumentException, StorageException,
            URISyntaxException, IOException {

        String name = generateRandomContainerName();
        CloudBlobContainer container = bClient.getContainerReference(name);
        container.create();

        CloudBlockBlob blob = container.getBlockBlobReference("test");
        blob.upload(new ByteArrayInputStream(new byte[100]), 100);

        SharedAccessBlobPolicy sp1 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE,
                        SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE), 300);
        SharedAccessBlobPolicy sp2 = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("full", sp1);
        perms.getSharedAccessPolicies().put("readlist", sp2);
        container.uploadPermissions(perms);

        String containerReadListSas = container.generateSharedAccessSignature(sp2, null);
        CloudBlobContainer readListContainer = bClient.getContainerReference(container.getUri().toString() + "?"
                + containerReadListSas);
        Assert.assertEquals(StorageCredentialsSharedAccessSignature.class.toString(), readListContainer
                .getServiceClient().getCredentials().getClass().toString());

        CloudBlockBlob blobFromSasContainer = readListContainer.getBlockBlobReference("test");
        blobFromSasContainer.download(new ByteArrayOutputStream());
        container.deleteIfExists();
    }

    @Test
    public void testBlobSaS() throws InvalidKeyException, IllegalArgumentException, StorageException,
            URISyntaxException, IOException {

        String name = generateRandomContainerName();
        CloudBlobContainer container = bClient.getContainerReference(name);
        container.create();

        CloudBlockBlob blob = container.getBlockBlobReference("test");
        blob.upload(new ByteArrayInputStream(new byte[100]), 100);

        SharedAccessBlobPolicy sp = createSharedAccessPolicy(
                EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 300);
        BlobContainerPermissions perms = new BlobContainerPermissions();

        perms.getSharedAccessPolicies().put("readperm", sp);
        container.uploadPermissions(perms);

        CloudBlockBlob sasBlob = new CloudBlockBlob(new URI(blob.getUri().toString() + "?"
                + blob.generateSharedAccessSignature(null, "readperm")));
        sasBlob.download(new ByteArrayOutputStream());
        container.deleteIfExists();
    }

    public final static SharedAccessBlobPolicy createSharedAccessPolicy(EnumSet<SharedAccessBlobPermissions> sap,
            int expireTimeInSeconds) {

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, expireTimeInSeconds);
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(sap);
        policy.setSharedAccessExpiryTime(cal.getTime());
        return policy;

    }

    @Test
    public void testContainerGetSetPermission() throws StorageException, URISyntaxException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        BlobContainerPermissions expectedPermissions;
        BlobContainerPermissions testPermissions;

        try {
            // Test new permissions.
            expectedPermissions = new BlobContainerPermissions();
            testPermissions = newContainer.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);

            // Test setting empty permissions.
            newContainer.uploadPermissions(expectedPermissions);
            testPermissions = newContainer.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);

            // Add a policy, check setting and getting.
            SharedAccessBlobPolicy policy1 = new SharedAccessBlobPolicy();
            Calendar now = GregorianCalendar.getInstance();
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 10);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.DELETE,
                    SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE));
            expectedPermissions.getSharedAccessPolicies().put(UUID.randomUUID().toString(), policy1);

            newContainer.uploadPermissions(expectedPermissions);
            testPermissions = newContainer.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    static void assertTablePermissionsEqual(BlobContainerPermissions expected, BlobContainerPermissions actual) {
        HashMap<String, SharedAccessBlobPolicy> expectedPolicies = expected.getSharedAccessPolicies();
        HashMap<String, SharedAccessBlobPolicy> actualPolicies = actual.getSharedAccessPolicies();
        Assert.assertEquals("SharedAccessPolicies.Count", expectedPolicies.size(), actualPolicies.size());
        for (String name : expectedPolicies.keySet()) {
            Assert.assertTrue("Key" + name + " doesn't exist", actualPolicies.containsKey(name));
            SharedAccessBlobPolicy expectedPolicy = expectedPolicies.get(name);
            SharedAccessBlobPolicy actualPolicy = actualPolicies.get(name);
            Assert.assertEquals("Policy: " + name + "\tPermissions\n", expectedPolicy.getPermissions().toString(),
                    actualPolicy.getPermissions().toString());
            Assert.assertEquals("Policy: " + name + "\tStartDate\n", expectedPolicy.getSharedAccessStartTime()
                    .toString(), actualPolicy.getSharedAccessStartTime().toString());
            Assert.assertEquals("Policy: " + name + "\tExpireDate\n", expectedPolicy.getSharedAccessExpiryTime()
                    .toString(), actualPolicy.getSharedAccessExpiryTime().toString());

        }

    }

    @Test
    public void testContainerAcquireLease() throws StorageException, URISyntaxException, InterruptedException {
        String name = "leased" + generateRandomContainerName();
        CloudBlobContainer leaseContainer1 = bClient.getContainerReference(name);
        leaseContainer1.create();
        String proposedLeaseId1 = UUID.randomUUID().toString();

        name = "leased" + generateRandomContainerName();
        CloudBlobContainer leaseContainer2 = bClient.getContainerReference(name);
        leaseContainer2.create();
        String proposedLeaseId2 = UUID.randomUUID().toString();

        try {
            // 15 sec

            OperationContext operationContext1 = new OperationContext();
            leaseContainer1.acquireLease(15, proposedLeaseId1, null /*access condition*/,
                    null/* BlobRequestOptions */, operationContext1);
            Assert.assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

            //infinite
            String leaseId1;
            String leaseId2;
            OperationContext operationContext2 = new OperationContext();
            leaseId1 = leaseContainer2.acquireLease(null /* infinite lease */, proposedLeaseId2,
                    null /*access condition*/, null/* BlobRequestOptions */, operationContext2);
            Assert.assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

            leaseId2 = leaseContainer2.acquireLease(null /* infinite lease */, proposedLeaseId2);
            Assert.assertEquals(leaseId1, leaseId2);

        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId1);
            leaseContainer1.releaseLease(condition);
            leaseContainer1.deleteIfExists();

            condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId2);
            leaseContainer2.releaseLease(condition);
            leaseContainer2.deleteIfExists();
        }
    }

    @Test
    public void testContainerReleaseLease() throws StorageException, URISyntaxException, InterruptedException {
        String name = "leased" + generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        try {
            // 15 sec
            String proposedLeaseId = UUID.randomUUID().toString();
            String leaseId = newContainer.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext1 = new OperationContext();
            newContainer.releaseLease(condition, null/* BlobRequestOptions */, operationContext1);
            Assert.assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);

            //infinite
            proposedLeaseId = UUID.randomUUID().toString();
            leaseId = newContainer.acquireLease(null /* infinite lease */, proposedLeaseId);
            condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext2 = new OperationContext();
            newContainer.releaseLease(condition, null/* BlobRequestOptions */, operationContext2);
            Assert.assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testContainerBreakLease() throws StorageException, URISyntaxException, InterruptedException {
        String name = "leased" + generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();
        String proposedLeaseId = UUID.randomUUID().toString();

        try {
            // 5 sec
            String leaseId = newContainer.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext1 = new OperationContext();
            newContainer.breakLease(0, condition, null/* BlobRequestOptions */, operationContext1);
            Assert.assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
            Thread.sleep(15 * 1000);

            //infinite
            proposedLeaseId = UUID.randomUUID().toString();
            leaseId = newContainer.acquireLease(null /* infinite lease */, proposedLeaseId);
            condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext2 = new OperationContext();
            newContainer.breakLease(0, condition, null/* BlobRequestOptions */, operationContext2);
            Assert.assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            newContainer.releaseLease(condition);
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testContainerRenewLeaseTest() throws StorageException, URISyntaxException, InterruptedException {
        String name = "leased" + generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();
        String proposedLeaseId = UUID.randomUUID().toString();

        try {
            // 5 sec
            String leaseId = newContainer.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext1 = new OperationContext();
            newContainer.renewLease(condition, null/* BlobRequestOptions */, operationContext1);
            Assert.assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
            newContainer.releaseLease(condition);

            //infinite
            proposedLeaseId = UUID.randomUUID().toString();
            leaseId = newContainer.acquireLease(null /* infinite lease */, proposedLeaseId);
            condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext2 = new OperationContext();
            newContainer.renewLease(condition, null/* BlobRequestOptions */, operationContext2);
            Assert.assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            newContainer.releaseLease(condition);
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testBlobLeaseAcquireAndRelease() throws URISyntaxException, StorageException, IOException {
        final int length = 128;
        final Random randGenerator = new Random();
        final byte[] buff = new byte[length];
        randGenerator.nextBytes(buff);

        String blobName = "testBlob" + Integer.toString(randGenerator.nextInt(50000));
        blobName = blobName.replace('-', '_');

        final CloudBlobContainer leasedContainer = bClient.getContainerReference(testSuiteContainerName);
        final CloudBlob blobRef = leasedContainer.getBlockBlobReference(blobName);
        final BlobRequestOptions options = new BlobRequestOptions();

        blobRef.upload(new ByteArrayInputStream(buff), -1, null, options, null);

        // Get Lease 
        OperationContext operationContext = new OperationContext();
        final String leaseID = blobRef.acquireLease(15, null /*access condition*/, null /*proposed lease id */,
                null/* BlobRequestOptions */, operationContext);
        final AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        Assert.assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

        // Try to upload without lease
        try {
            blobRef.upload(new ByteArrayInputStream(buff), -1, null, options, null);
        }
        catch (final StorageException ex) {
            Assert.assertEquals(ex.getHttpStatusCode(), 412);
            Assert.assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.LEASE_ID_MISSING);
        }

        // Try to upload with lease
        blobRef.upload(new ByteArrayInputStream(buff), -1, leaseCondition, options, null);

        // Release lease
        blobRef.releaseLease(leaseCondition);

        // now upload with no lease specified.
        blobRef.upload(new ByteArrayInputStream(buff), -1, null, options, null);
    }

    @Test
    public void testBlobLeaseBreak() throws URISyntaxException, StorageException, IOException, InterruptedException {
        final int length = 128;
        final Random randGenerator = new Random();
        final byte[] buff = new byte[length];
        randGenerator.nextBytes(buff);

        String blobName = "testBlob" + Integer.toString(randGenerator.nextInt(50000));
        blobName = blobName.replace('-', '_');

        final CloudBlobContainer existingContainer = bClient.getContainerReference(testSuiteContainerName);
        final CloudBlob blobRef = existingContainer.getBlockBlobReference(blobName);
        final BlobRequestOptions options = new BlobRequestOptions();

        blobRef.upload(new ByteArrayInputStream(buff), -1, null, options, null);

        // Get Lease
        String leaseID = blobRef.acquireLease(null, null);

        OperationContext operationContext = new OperationContext();
        final AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        blobRef.breakLease(0, leaseCondition, null/* BlobRequestOptions */, operationContext);
        Assert.assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
    }

    @Test
    public void testBlobLeaseRenew() throws URISyntaxException, StorageException, IOException, InterruptedException {
        final int length = 128;
        final Random randGenerator = new Random();
        final byte[] buff = new byte[length];
        randGenerator.nextBytes(buff);

        String blobName = "testBlob" + Integer.toString(randGenerator.nextInt(50000));
        blobName = blobName.replace('-', '_');

        final CloudBlobContainer existingContainer = bClient.getContainerReference(testSuiteContainerName);
        final CloudBlob blobRef = existingContainer.getBlockBlobReference(blobName);
        final BlobRequestOptions options = new BlobRequestOptions();

        blobRef.upload(new ByteArrayInputStream(buff), -1, null, options, null);

        // Get Lease
        final String leaseID = blobRef.acquireLease(15, null);
        Thread.sleep(1000);

        AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        OperationContext operationContext = new OperationContext();
        blobRef.renewLease(leaseCondition, null/* BlobRequestOptions */, operationContext);
        Assert.assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
    }

    static String setLeasedState(CloudBlobContainer container, int leaseTime) throws StorageException {
        String leaseId = UUID.randomUUID().toString();
        setUnleasedState(container);
        return container.acquireLease(leaseTime, leaseId);
    }

    static void setUnleasedState(CloudBlobContainer container) throws StorageException {
        if (!container.createIfNotExist()) {
            try {
                container.breakLease(0);
            }
            catch (StorageException e) {
                if (e.getHttpStatusCode() != HttpURLConnection.HTTP_BAD_REQUEST) {
                    throw e;
                }
            }
        }
    }

    @Test
    public void testCopyFromBlob() throws StorageException, URISyntaxException, IOException, InterruptedException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();
        CloudBlob originalBlob = newContainer.getBlockBlobReference("newblob");
        originalBlob.upload(new ByteArrayInputStream(testData), testData.length);

        try {
            CloudBlob copyBlob = newContainer.getBlockBlobReference(originalBlob.getName() + "copyed");
            copyBlob.copyFromBlob(originalBlob);
            Thread.sleep(1000);
            copyBlob.downloadAttributes();
            Assert.assertNotNull(copyBlob.copyState);
            Assert.assertNotNull(copyBlob.copyState.getCopyId());
            Assert.assertNotNull(copyBlob.copyState.getCompletionTime());
            Assert.assertNotNull(copyBlob.copyState.getSource());
            Assert.assertFalse(copyBlob.copyState.getBytesCopied() == 0);
            Assert.assertFalse(copyBlob.copyState.getTotalBytes() == 0);
            for (final ListBlobItem blob : newContainer.listBlobs()) {
                CloudBlob blobFromList = ((CloudBlob) blob);
                blobFromList.downloadAttributes();
            }
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testCopyFromBlobAbortTest() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();
        CloudBlob originalBlob = newContainer.getBlockBlobReference("newblob");
        byte[] data = new byte[16 * 1024 * 1024];
        Random r = new Random();
        r.nextBytes(data);
        originalBlob.upload(new ByteArrayInputStream(data), testData.length);

        try {
            CloudBlob copyBlob = newContainer.getBlockBlobReference(originalBlob.getName() + "copyed");
            copyBlob.copyFromBlob(originalBlob);

            try {
                copyBlob.abortCopy(copyBlob.copyState.getCopyId());
            }
            catch (StorageException e) {
                if (!e.getErrorCode().contains("NoPendingCopyOperation")) {
                    throw e;
                }
            }
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testBlobSnapshotValidationTest() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        try {
            final int length = 1 * 1024;
            final Random randGenerator = new Random();
            final byte[] buff = new byte[length];
            randGenerator.nextBytes(buff);

            String blockBlobName = "testBlockBlob" + Integer.toString(randGenerator.nextInt(50000));
            blockBlobName = blockBlobName.replace('-', '_');

            final CloudBlob blockBlobRef = newContainer.getBlockBlobReference(blockBlobName);
            blockBlobRef.upload(new ByteArrayInputStream(buff), -1, null, null, null);

            final CloudBlob blobSnapshot = blockBlobRef.createSnapshot();

            final ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);

            blobSnapshot.download(outStream);
            final byte[] retrievedBuff = outStream.toByteArray();
            for (int m = 0; m < length; m++) {
                Assert.assertEquals(buff[m], retrievedBuff[m]);
            }

            // Read operation should work fine. 
            blobSnapshot.downloadAttributes();

            // Expect an IllegalArgumentException from upload. 
            try {
                blobSnapshot.upload(new ByteArrayInputStream(buff), -1);
                Assert.fail("Expect an IllegalArgumentException from upload");
            }
            catch (IllegalArgumentException e) {
                Assert.assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
            }

            // Expect an IllegalArgumentException from uploadMetadata. 
            try {
                blobSnapshot.uploadMetadata();
                Assert.fail("Expect an IllegalArgumentException from uploadMetadata");
            }
            catch (IllegalArgumentException e) {
                Assert.assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
            }

            // Expect an IllegalArgumentException from uploadProperties. 
            try {
                blobSnapshot.uploadProperties();
                Assert.fail("Expect an IllegalArgumentException from uploadProperties");
            }
            catch (IllegalArgumentException e) {
                Assert.assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
            }

            // Expect an IllegalArgumentException from createSnapshot. 
            try {
                blobSnapshot.createSnapshot();
                Assert.fail("Expect an IllegalArgumentException from createSnapshot");
            }
            catch (IllegalArgumentException e) {
                Assert.assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
            }

            blobSnapshot.delete(DeleteSnapshotsOption.NONE, null, null, null);

            blockBlobRef.downloadAttributes();
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testBlobDownloadRangeValidationTest() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        try {

            final int blockLength = 1024 * 1024;
            final int length = 3 * blockLength;
            final Random randGenerator = new Random();
            final byte[] buff = new byte[length];
            randGenerator.nextBytes(buff);

            String blockBlobName = "testBlockBlob" + Integer.toString(randGenerator.nextInt(50000));
            blockBlobName = blockBlobName.replace('-', '_');

            final CloudBlockBlob blockBlobRef = newContainer.getBlockBlobReference(blockBlobName);
            blockBlobRef.upload(new ByteArrayInputStream(buff), -1, null, null, null);
            ArrayList<BlockEntry> blockList = new ArrayList<BlockEntry>();
            for (int i = 1; i <= 3; i++) {
                randGenerator.nextBytes(buff);

                String blockID = String.format("%08d", i);
                blockBlobRef.uploadBlock(blockID, new ByteArrayInputStream(buff), blockLength, null, null, null);
                blockList.add(new BlockEntry(blockID, BlockSearchMode.LATEST));
            }

            blockBlobRef.commitBlockList(blockList);

            //Download full blob
            blockBlobRef.download(new ByteArrayOutputStream());
            Assert.assertEquals(length, blockBlobRef.getProperties().getLength());

            //Download blob range.
            byte[] downloadBuffer = new byte[100];
            blockBlobRef.downloadRange(0, 100, downloadBuffer, 0);
            Assert.assertEquals(length, blockBlobRef.getProperties().getLength());

            //Download block list.
            blockBlobRef.downloadBlockList();
            Assert.assertEquals(length, blockBlobRef.getProperties().getLength());
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testBlobNamePlusEncodingTest() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        try {

            final int length = 1 * 1024;
            final Random randGenerator = new Random();
            final byte[] buff = new byte[length];
            randGenerator.nextBytes(buff);

            final CloudBlockBlob originalBlob = newContainer.getBlockBlobReference("a+b.txt");
            originalBlob.upload(new ByteArrayInputStream(buff), -1, null, null, null);

            CloudBlob copyBlob = newContainer.getBlockBlobReference(originalBlob.getName() + "copyed");
            copyBlob.copyFromBlob(originalBlob);
            Thread.sleep(1000);
            copyBlob.downloadAttributes();
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testListContainersTest() throws StorageException, URISyntaxException, IOException, InterruptedException {
        final ResultSegment<CloudBlobContainer> segment = bClient.listContainersSegmented(null,
                ContainerListingDetails.ALL, 2, null, null, null);

        for (int i = 0; i < 5 && segment.getHasMoreResults(); i++) {
            for (final CloudBlobContainer container : segment.getResults()) {
                container.downloadAttributes();
            }

            bClient.listContainersSegmented(null, ContainerListingDetails.ALL, 2, segment.getContinuationToken(), null,
                    null);
        }
    }

    @Test
    public void testSendingRequestEventBlob() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        OperationContext sendingRequestEventContext = new OperationContext();
        sendingRequestEventContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                Assert.assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                callList.add(true);
            }
        });

        try {
            Assert.assertEquals(0, callList.size());

            //Put blob
            CloudBlob blob = newContainer.getBlockBlobReference("newblob");
            blob.upload(new ByteArrayInputStream(testData), testData.length, null, null, sendingRequestEventContext);
            Assert.assertEquals(1, callList.size());

            //Get blob
            blob.download(new ByteArrayOutputStream(), null, null, sendingRequestEventContext);
            Assert.assertEquals(2, callList.size());

            //uploadMetadata
            blob.uploadMetadata(null, null, sendingRequestEventContext);
            Assert.assertEquals(3, callList.size());

            //uploadMetadata
            blob.downloadAttributes(null, null, sendingRequestEventContext);
            Assert.assertEquals(4, callList.size());

        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }

    @Test
    public void testBlobInputStream() throws URISyntaxException, StorageException, IOException {
        final int blobLength = 16 * 1024;
        final Random randGenerator = new Random();
        String blobName = "testblob" + Integer.toString(randGenerator.nextInt(50000));
        blobName = blobName.replace('-', '_');

        final CloudBlobContainer containerRef = bClient.getContainerReference(BlobTestBase.testSuiteContainerName);

        final CloudBlockBlob blobRef = containerRef.getBlockBlobReference(blobName);

        final byte[] buff = new byte[blobLength];
        randGenerator.nextBytes(buff);
        buff[0] = -1;
        buff[1] = -128;
        final ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);

        final BlobRequestOptions options = new BlobRequestOptions();
        final OperationContext operationContext = new OperationContext();
        options.setStoreBlobContentMD5(true);
        options.setTimeoutIntervalInMs(90000);
        options.setRetryPolicyFactory(new RetryNoRetry());
        blobRef.uploadFullBlob(sourceStream, blobLength, null, options, operationContext);

        BlobInputStream blobStream = blobRef.openInputStream();

        for (int i = 0; i < blobLength; i++) {
            int data = blobStream.read();
            Assert.assertTrue(data >= 0);
            Assert.assertEquals(buff[i], (byte) data);
        }

        Assert.assertEquals(-1, blobStream.read());

        blobRef.delete();
    }

    @Test
    public void testCurrentOperationByteCount() throws URISyntaxException, StorageException, IOException {
        final int blockLength = 4 * 1024 * 1024;
        final Random randGenerator = new Random();
        String blobName = "testblob" + Integer.toString(randGenerator.nextInt(50000));
        blobName = blobName.replace('-', '_');

        final CloudBlobContainer containerRef = bClient.getContainerReference(BlobTestBase.testSuiteContainerName);

        final CloudBlockBlob blobRef = containerRef.getBlockBlobReference(blobName);

        final ArrayList<byte[]> byteList = new ArrayList<byte[]>();
        final ArrayList<BlockEntry> blockList = new ArrayList<BlockEntry>();

        int numberOfBlocks = 4;

        for (int m = 0; m < numberOfBlocks; m++) {
            final byte[] buff = new byte[blockLength];
            randGenerator.nextBytes(buff);
            byteList.add(buff);
            blobRef.uploadBlock("ABC" + m, new ByteArrayInputStream(buff), blockLength);

            blockList.add(new BlockEntry("ABC" + m, BlockSearchMode.LATEST));
        }

        blobRef.commitBlockList(blockList);

        OperationContext operationContext = new OperationContext();
        BlobRequestOptions options = new BlobRequestOptions();
        options.setTimeoutIntervalInMs(2000);
        options.setRetryPolicyFactory(new RetryNoRetry());

        ByteArrayOutputStream downloadedDataStream = new ByteArrayOutputStream();
        try {
            blobRef.download(downloadedDataStream, null, options, operationContext);
        }
        catch (Exception e) {
            Assert.assertEquals(downloadedDataStream.size(), operationContext.getCurrentOperationByteCount());
        }

        operationContext = new OperationContext();
        options = new BlobRequestOptions();
        options.setTimeoutIntervalInMs(90000);

        downloadedDataStream = new ByteArrayOutputStream();
        blobRef.download(downloadedDataStream, null, options, operationContext);

        Assert.assertEquals(blockLength * numberOfBlocks, operationContext.getCurrentOperationByteCount());

        blobRef.delete();
    }

    @Test
    public void testContainerSharedKeyLite() throws StorageException, URISyntaxException {
        bClient.setAuthenticationScheme(AuthenticationScheme.SHAREDKEYLITE);
        String name = generateRandomContainerName();
        CloudBlobContainer newContainer = bClient.getContainerReference(name);
        newContainer.create();

        BlobContainerPermissions expectedPermissions;
        BlobContainerPermissions testPermissions;

        try {
            // Test new permissions.
            expectedPermissions = new BlobContainerPermissions();
            testPermissions = newContainer.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);

            // Test setting empty permissions.
            newContainer.uploadPermissions(expectedPermissions);
            testPermissions = newContainer.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);

            // Add a policy, check setting and getting.
            SharedAccessBlobPolicy policy1 = new SharedAccessBlobPolicy();
            Calendar now = GregorianCalendar.getInstance();
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 10);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.DELETE,
                    SharedAccessBlobPermissions.LIST, SharedAccessBlobPermissions.DELETE));
            expectedPermissions.getSharedAccessPolicies().put(UUID.randomUUID().toString(), policy1);

            newContainer.uploadPermissions(expectedPermissions);
            testPermissions = newContainer.downloadPermissions();
            assertTablePermissionsEqual(expectedPermissions, testPermissions);
        }
        finally {
            // cleanup
            newContainer.deleteIfExists();
        }
    }
}
