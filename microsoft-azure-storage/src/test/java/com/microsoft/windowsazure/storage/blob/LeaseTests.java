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
package com.microsoft.windowsazure.storage.blob;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.windowsazure.storage.AccessCondition;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.TestRunners.CloudTests;
import com.microsoft.windowsazure.storage.TestRunners.DevFabricTests;
import com.microsoft.windowsazure.storage.TestRunners.DevStoreTests;
import com.microsoft.windowsazure.storage.TestRunners.SlowTests;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class LeaseTests extends BlobTestBase {

    protected CloudBlobContainer container;

    @Before
    public void leaseTestMethodSetup() throws URISyntaxException, StorageException {
        container = getRandomContainerReference();
        container.create();
    }

    @After
    public void leaseTestMethodTearDown() throws StorageException {
        container.deleteIfExists();
    }

    @Test
    public void testContainerAcquireLease() throws StorageException, URISyntaxException, InterruptedException {
        CloudBlobContainer leaseContainer1 = getRandomContainerReference();
        leaseContainer1.create();
        String proposedLeaseId1 = UUID.randomUUID().toString();

        CloudBlobContainer leaseContainer2 = getRandomContainerReference();
        leaseContainer2.create();
        String proposedLeaseId2 = UUID.randomUUID().toString();

        try {
            // 15 sec
            OperationContext operationContext1 = new OperationContext();
            leaseContainer1.acquireLease(15, proposedLeaseId1, null /*access condition*/,
                    null/* BlobRequestOptions */, operationContext1);
            assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

            // infinite
            String leaseId1;
            String leaseId2;
            OperationContext operationContext2 = new OperationContext();
            leaseId1 = leaseContainer2.acquireLease(null /* infinite lease */, proposedLeaseId2,
                    null /*access condition*/, null/* BlobRequestOptions */, operationContext2);
            assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

            leaseId2 = leaseContainer2.acquireLease(null /* infinite lease */, proposedLeaseId2);
            assertEquals(leaseId1, leaseId2);

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
        // 15 sec
        String proposedLeaseId = UUID.randomUUID().toString();
        String leaseId = container.acquireLease(15, proposedLeaseId);
        AccessCondition condition = new AccessCondition();
        condition.setLeaseID(leaseId);
        OperationContext operationContext1 = new OperationContext();
        container.releaseLease(condition, null/* BlobRequestOptions */, operationContext1);
        assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);

        // infinite
        proposedLeaseId = UUID.randomUUID().toString();
        leaseId = container.acquireLease(null /* infinite lease */, proposedLeaseId);
        condition = new AccessCondition();
        condition.setLeaseID(leaseId);
        OperationContext operationContext2 = new OperationContext();
        container.releaseLease(condition, null/* BlobRequestOptions */, operationContext2);
        assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
    }

    @Test
    @Category(SlowTests.class)
    public void testContainerBreakLease() throws StorageException, URISyntaxException, InterruptedException {
        String proposedLeaseId = UUID.randomUUID().toString();
        try {
            // 5 sec
            String leaseId = container.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext1 = new OperationContext();
            container.breakLease(0, condition, null/* BlobRequestOptions */, operationContext1);
            assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
            Thread.sleep(15 * 1000);

            // infinite
            proposedLeaseId = UUID.randomUUID().toString();
            leaseId = container.acquireLease(null /* infinite lease */, proposedLeaseId);
            condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext2 = new OperationContext();
            container.breakLease(0, condition, null/* BlobRequestOptions */, operationContext2);
            assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            container.releaseLease(condition);
        }
    }

    @Test
    public void testContainerRenewLeaseTest() throws StorageException, URISyntaxException, InterruptedException {
        String proposedLeaseId = UUID.randomUUID().toString();
        try {
            // 5 sec
            String leaseId = container.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext1 = new OperationContext();
            container.renewLease(condition, null/* BlobRequestOptions */, operationContext1);
            assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
            container.releaseLease(condition);

            // infinite
            proposedLeaseId = UUID.randomUUID().toString();
            leaseId = container.acquireLease(null /* infinite lease */, proposedLeaseId);
            condition = new AccessCondition();
            condition.setLeaseID(leaseId);
            OperationContext operationContext2 = new OperationContext();
            container.renewLease(condition, null/* BlobRequestOptions */, operationContext2);
            assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            container.releaseLease(condition);
        }
    }

    @Test
    public void testContainerChangeLeaseTest() throws StorageException, URISyntaxException, InterruptedException {
        // Get Lease 
        String leaseID1;
        String leaseID2;

        OperationContext operationContext = new OperationContext();
        leaseID1 = container.acquireLease(null /* infinite lease */, null /*proposed lease id */,
                null /*access condition*/, null/* BlobRequestOptions */, operationContext);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

        //Change leased state with idempotent change
        final String proposedLeaseId = UUID.randomUUID().toString();
        leaseID2 = container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));
        leaseID2 = container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));

        //Change lease state with same proposed ID but different lease ID
        leaseID2 = container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID2));
        leaseID2 = container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));

        //Change lease (wrong lease ID specified)
        final String proposedLeaseId2 = UUID.randomUUID().toString();
        leaseID2 = container.changeLease(proposedLeaseId2, AccessCondition.generateLeaseCondition(leaseID2));
        try {
            container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change released lease
        container.releaseLease(AccessCondition.generateLeaseCondition(leaseID2));
        try {
            container.changeLease(leaseID2, AccessCondition.generateLeaseCondition(leaseID2));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change a breaking lease (same ID)
        leaseID1 = container.acquireLease(null /* infinite lease */, null /*proposed lease id */,
                null /*access condition*/, null/* BlobRequestOptions */, operationContext);
        container.breakLease(60);
        try {
            container.changeLease(proposedLeaseId2, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change broken lease
        container.breakLease(0);
        try {
            container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change broken lease (to previous lease)
        leaseID1 = container.acquireLease(null /* infinite lease */, null /*proposed lease id */,
                null /*access condition*/, null/* BlobRequestOptions */, operationContext);
        container.breakLease(0);
        try {
            container.changeLease(leaseID1, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }
    }

    @Test
    public void testBlobLeaseAcquireAndRelease() throws URISyntaxException, StorageException, IOException {
        final int length = 128;
        final CloudBlob blobRef = uploadNewBlob(container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease 
        OperationContext operationContext = new OperationContext();
        final String leaseID = blobRef.acquireLease(15, null /*proposed lease id */, null /*access condition*/,
                null/* BlobRequestOptions */, operationContext);
        final AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

        tryUploadWithBadLease(length, blobRef, null, StorageErrorCodeStrings.LEASE_ID_MISSING);

        // Try to upload with lease
        blobRef.upload(getRandomDataStream(length), -1, leaseCondition, null, null);

        // Release lease
        blobRef.releaseLease(leaseCondition);

        // now upload with no lease specified.
        blobRef.upload(getRandomDataStream(length), -1);
    }

    @Test
    public void testBlobLeaseChange() throws StorageException, IOException, URISyntaxException {
        final int length = 128;
        final CloudBlob blobRef = uploadNewBlob(container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease 
        OperationContext operationContext = new OperationContext();
        final String leaseID1 = blobRef.acquireLease(15, null /*proposed lease id */, null /*access condition*/,
                null/* BlobRequestOptions */, operationContext);
        final AccessCondition leaseCondition1 = AccessCondition.generateLeaseCondition(leaseID1);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

        final String leaseID2 = UUID.randomUUID().toString();
        final AccessCondition leaseCondition2 = AccessCondition.generateLeaseCondition(leaseID2);

        // Try to upload without lease
        tryUploadWithBadLease(length, blobRef, null, StorageErrorCodeStrings.LEASE_ID_MISSING);

        // Try to upload with incorrect lease
        tryUploadWithBadLease(length, blobRef, leaseCondition2,
                StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);

        // Try to upload with correct lease
        blobRef.upload(getRandomDataStream(length), -1, leaseCondition1, null, null);

        // Fail to change the lease with a bad accessCondition
        try {
            blobRef.changeLease(leaseID2, leaseCondition2);
            fail("Did not throw expected exception.");
        }
        catch (final StorageException ex) {
            assertEquals(ex.getHttpStatusCode(), 409);
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_LEASE_OPERATION);
        }

        // Change the lease
        blobRef.changeLease(leaseID2, leaseCondition1);

        // Try to upload without lease
        tryUploadWithBadLease(length, blobRef, null, StorageErrorCodeStrings.LEASE_ID_MISSING);

        // Try to upload with incorrect lease
        tryUploadWithBadLease(length, blobRef, leaseCondition1,
                StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);

        // Try to upload with correct lease
        blobRef.upload(getRandomDataStream(length), -1, leaseCondition2, null, null);
    }

    /**
     * Try to upload with a bad lease
     * 
     * @param length
     * @param blobRef
     * @throws IOException
     */
    private void tryUploadWithBadLease(final int length, final CloudBlob blobRef, final AccessCondition leaseCondition,
            final String expectedError) throws IOException {
        try {
            blobRef.upload(getRandomDataStream(length), -1, leaseCondition, null, null);
            fail("Did not throw expected exception");
        }
        catch (final StorageException ex) {
            assertEquals(ex.getHttpStatusCode(), 412);
            assertEquals(ex.getErrorCode(), expectedError);
        }
    }

    @Test
    public void testBlobLeaseBreak() throws URISyntaxException, StorageException, IOException, InterruptedException {
        final CloudBlob blobRef = uploadNewBlob(container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease
        String leaseID = blobRef.acquireLease(null, null);

        OperationContext operationContext = new OperationContext();
        final AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        blobRef.breakLease(0, leaseCondition, null/* BlobRequestOptions */, operationContext);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
    }

    @Test
    @Category(SlowTests.class)
    public void testBlobLeaseRenew() throws URISyntaxException, StorageException, IOException, InterruptedException {
        final CloudBlob blobRef = uploadNewBlob(container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease
        final String leaseID = blobRef.acquireLease(15, null);
        Thread.sleep(1000);

        AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        OperationContext operationContext = new OperationContext();
        blobRef.renewLease(leaseCondition, null/* BlobRequestOptions */, operationContext);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
    }
}
