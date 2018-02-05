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
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.*;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class LeaseTests {

    protected CloudBlobContainer container;

    @Before
    public void leaseTestMethodSetup() throws StorageException, URISyntaxException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
    }

    @After
    public void leaseTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    @Test
    public void testContainerLeaseInvalidParams() throws StorageException, URISyntaxException {
        try {
            this.container.acquireLease(100, null);   
        } catch(StorageException ex) {
            assertEquals("The value of the parameter 'leaseTimeInSeconds' should be between 15 and 60.", 
                    ex.getMessage());
        }
        
        try {
            this.container.breakLease(100);   
        } catch(StorageException ex) {
            assertEquals("The value of the parameter 'breakPeriodInSeconds' should be between 0 and 60.", 
                    ex.getMessage());
        }
    }

    @Test
    public void testContainerAcquireLease() throws StorageException, URISyntaxException {
        CloudBlobContainer leaseContainer1 = BlobTestHelper.getRandomContainerReference();
        leaseContainer1.create();
        String proposedLeaseId1 = UUID.randomUUID().toString();

        CloudBlobContainer leaseContainer2 = BlobTestHelper.getRandomContainerReference();
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
    public void testContainerReleaseLease() throws StorageException {
        // 15 sec
        String proposedLeaseId = UUID.randomUUID().toString();
        String leaseId = this.container.acquireLease(15, proposedLeaseId);
        AccessCondition condition = new AccessCondition();
        condition.setLeaseID(leaseId);
        OperationContext operationContext1 = new OperationContext();
        this.container.releaseLease(condition, null/* BlobRequestOptions */, operationContext1);
        assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);

        // infinite
        leaseId = this.container.acquireLease();
        condition = new AccessCondition();
        condition.setLeaseID(leaseId);
        OperationContext operationContext2 = new OperationContext();
        this.container.releaseLease(condition, null/* BlobRequestOptions */, operationContext2);
        assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
    }

    @Test
    @Category(SlowTests.class)
    public void testContainerBreakLease() throws StorageException, InterruptedException {
        String proposedLeaseId = UUID.randomUUID().toString();
        try {
            // 5 sec
            this.container.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            OperationContext operationContext1 = new OperationContext();
            this.container.breakLease(0, condition, null/* BlobRequestOptions */, operationContext1);
            assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
            Thread.sleep(15 * 1000);

            // infinite
            proposedLeaseId = this.container.acquireLease();
            condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            OperationContext operationContext2 = new OperationContext();
            this.container.breakLease(0, condition, null/* BlobRequestOptions */, operationContext2);
            assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            this.container.releaseLease(condition);
        }
    }

    @Test
    public void testContainerRenewLeaseTest() throws StorageException {
        String proposedLeaseId = UUID.randomUUID().toString();
        try {
            // 5 sec
            this.container.acquireLease(15, proposedLeaseId);
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            OperationContext operationContext1 = new OperationContext();
            this.container.renewLease(condition, null/* BlobRequestOptions */, operationContext1);
            assertTrue(operationContext1.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
            this.container.releaseLease(condition);

            // infinite
            proposedLeaseId = this.container.acquireLease();
            condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            OperationContext operationContext2 = new OperationContext();
            this.container.renewLease(condition, null/* BlobRequestOptions */, operationContext2);
            assertTrue(operationContext2.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
        }
        finally {
            // cleanup
            AccessCondition condition = new AccessCondition();
            condition.setLeaseID(proposedLeaseId);
            this.container.releaseLease(condition);
        }
    }

    @Test
    public void testContainerChangeLeaseTest() throws StorageException {
        // Get Lease 
        String leaseID1;
        String leaseID2;

        OperationContext operationContext = new OperationContext();
        leaseID1 = this.container.acquireLease(null /* infinite lease */, null /*proposed lease id */,
                null /*access condition*/, null/* BlobRequestOptions */, operationContext);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

        //Change leased state with idempotent change
        final String proposedLeaseId = UUID.randomUUID().toString();
        leaseID2 = this.container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));
        leaseID2 = this.container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));

        //Change lease state with same proposed ID but different lease ID
        leaseID2 = this.container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID2));
        leaseID2 = this.container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));

        //Change lease (wrong lease ID specified)
        final String proposedLeaseId2 = UUID.randomUUID().toString();
        leaseID2 = this.container.changeLease(proposedLeaseId2, AccessCondition.generateLeaseCondition(leaseID2));
        try {
            this.container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change released lease
        this.container.releaseLease(AccessCondition.generateLeaseCondition(leaseID2));
        try {
            this.container.changeLease(leaseID2, AccessCondition.generateLeaseCondition(leaseID2));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change a breaking lease (same ID)
        leaseID1 = this.container.acquireLease(null /* infinite lease */, null /*proposed lease id */,
                null /*access condition*/, null/* BlobRequestOptions */, operationContext);
        this.container.breakLease(60);
        try {
            this.container.changeLease(proposedLeaseId2, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change broken lease
        this.container.breakLease(0);
        try {
            this.container.changeLease(proposedLeaseId, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        // Change broken lease (to previous lease)
        leaseID1 = this.container.acquireLease(null /* infinite lease */, null /*proposed lease id */,
                null /*access condition*/, null/* BlobRequestOptions */, operationContext);
        this.container.breakLease(0);
        try {
            this.container.changeLease(leaseID1, AccessCondition.generateLeaseCondition(leaseID1));
        }
        catch (StorageException ex) {
            Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }
    }

    @Test
    public void testBlobLeaseAcquireAndRelease() throws StorageException, IOException, URISyntaxException {
        final int length = 128;
        final CloudBlob blobRef = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease 
        OperationContext operationContext = new OperationContext();
        final String leaseID = blobRef.acquireLease(15, null /*proposed lease id */, null /*access condition*/,
                null/* BlobRequestOptions */, operationContext);
        final AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_CREATED);

        tryUploadWithBadLease(length, blobRef, null, StorageErrorCodeStrings.LEASE_ID_MISSING);

        // Try to upload with lease
        blobRef.upload(BlobTestHelper.getRandomDataStream(length), -1, leaseCondition, null, null);

        // Release lease
        blobRef.releaseLease(leaseCondition);

        // now upload with no lease specified.
        blobRef.upload(BlobTestHelper.getRandomDataStream(length), -1);
    }

    @Test
    public void testBlobLeaseChange() throws StorageException, IOException, URISyntaxException {
        final int length = 128;
        final CloudBlob blobRef = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "test", 128, null);

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
        blobRef.upload(BlobTestHelper.getRandomDataStream(length), -1, leaseCondition1, null, null);

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
        blobRef.upload(BlobTestHelper.getRandomDataStream(length), -1, leaseCondition2, null, null);
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
            blobRef.upload(BlobTestHelper.getRandomDataStream(length), -1, leaseCondition, null, null);
            fail("Did not throw expected exception");
        }
        catch (final StorageException ex) {
            assertEquals(ex.getHttpStatusCode(), 412);
            assertEquals(ex.getErrorCode(), expectedError);
        }
    }

    @Test
    public void testBlobLeaseBreak() throws StorageException, IOException, URISyntaxException {
        final CloudBlob blobRef = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease
        String leaseID = blobRef.acquireLease();

        OperationContext operationContext = new OperationContext();
        final AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        blobRef.breakLease(0, leaseCondition, null/* BlobRequestOptions */, operationContext);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
    }

    @Test
    @Category(SlowTests.class)
    public void testBlobLeaseRenew() throws StorageException, IOException, InterruptedException, URISyntaxException {
        final CloudBlob blobRef = BlobTestHelper.uploadNewBlob(this.container, BlobType.BLOCK_BLOB, "test", 128, null);

        // Get Lease
        final String leaseID = blobRef.acquireLease(15, null);
        Thread.sleep(1000);

        AccessCondition leaseCondition = AccessCondition.generateLeaseCondition(leaseID);
        OperationContext operationContext = new OperationContext();
        blobRef.renewLease(leaseCondition, null/* BlobRequestOptions */, operationContext);
        assertTrue(operationContext.getLastResult().getStatusCode() == HttpURLConnection.HTTP_OK);
    }
}
