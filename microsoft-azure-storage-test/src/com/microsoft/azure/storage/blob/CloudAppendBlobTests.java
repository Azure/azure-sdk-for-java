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
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResponseReceivedEvent;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Random;

import static org.junit.Assert.*;

@Category({ DevFabricTests.class, CloudTests.class })
public class CloudAppendBlobTests {
    protected CloudBlobContainer container;

    @Before
    public void appendBlobTestMethodSetup() throws URISyntaxException,
            StorageException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
    }

    @After
    public void appendBlobTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    /**
     * Create an append blob.
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    public void testAppendBlobCreate() throws StorageException, URISyntaxException {
        final CloudAppendBlob blob = this.container.getAppendBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlob"));

        assertFalse(blob.exists());

        // Create
        blob.createOrReplace();
        assertTrue(blob.exists());

        // Create again (should succeed)
        blob.createOrReplace();
        assertTrue(blob.exists());

        // Create again, specifying not to if it already exists
        // This should fail
        // Add 15 min to account for clock skew
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 15);
        AccessCondition condition = AccessCondition.generateIfModifiedSinceCondition(cal.getTime());

        try {
            blob.createOrReplace(condition, null, null);
            fail("Create should fail due to access condition.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getHttpStatusCode());
            assertEquals("The condition specified using HTTP conditional header(s) is not met.", ex.getMessage());
            assertEquals("ConditionNotMet", ex.getErrorCode());
        }
        
        // Create again, specifying not to if it already exists
        // This should fail
        condition = AccessCondition.generateIfNotExistsCondition();
        try {
            blob.createOrReplace(condition, null, null);
            fail("Create should fail due to access condition.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
            assertEquals("The specified blob already exists.", ex.getMessage());
            assertEquals("BlobAlreadyExists", ex.getErrorCode());
        }
    }

    /**
     * Delete an append blob.
     * 
     * @throws StorageException
     * @throws URISyntaxException 
     */
    @Test
    public void testAppendBlobDelete() throws StorageException, URISyntaxException {
        final CloudAppendBlob blob = this.container.getAppendBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlob"));
        
        assertFalse(blob.exists());
        
        // create
        blob.createOrReplace();
        assertTrue(blob.exists());
        
        // delete
        blob.delete();
        assertFalse(blob.exists());
        
        // delete again, should fail as it doesn't exist
        try {
            blob.delete();
            fail("Delete should fail as blob does not exist.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
            assertEquals("The specified blob does not exist.", ex.getMessage());
            assertEquals("BlobNotFound", ex.getErrorCode());
        }                     
    }
    
    /**
     * Delete an append blob if it exists.
     * 
     * @throws StorageException
     * @throws URISyntaxException 
     */
    @Test
    public void testAppendBlobDeleteIfExists() throws URISyntaxException, StorageException {
        final CloudAppendBlob blob = this.container.getAppendBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlob"));

        assertFalse(blob.exists());
        assertFalse(blob.deleteIfExists());

        blob.createOrReplace();
        assertTrue(blob.exists());

        assertTrue(blob.deleteIfExists());
        assertFalse(blob.deleteIfExists());

        // check if second condition works in delete if exists
        OperationContext ctx = new OperationContext();
        ctx.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                if (((HttpURLConnection) eventArg.getConnectionObject()).getRequestMethod().equals("DELETE")) {
                    try {
                        blob.delete();
                        assertFalse(blob.exists());
                    }
                    catch (StorageException e) {
                        fail("Delete should succeed.");
                    }
                }
            }
        });

        // The second delete of a blob will return a 404
        blob.createOrReplace();
        assertFalse(blob.deleteIfExists(DeleteSnapshotsOption.NONE, null, null, ctx));
    }

    /**
     * Start copying a blob and then abort
     */
    @Test
    public void testCopyFromAppendBlobAbortTest() throws StorageException,
            URISyntaxException, IOException {
        final int length = 512;
        CloudAppendBlob originalBlob = (CloudAppendBlob) BlobTestHelper.uploadNewBlob(
                this.container, BlobType.APPEND_BLOB, "originalBlob", length, null);
        CloudAppendBlob copyBlob = this.container.getAppendBlobReference(originalBlob.getName() + "copyed");
        copyBlob.startCopy(originalBlob);

        try {
            copyBlob.abortCopy(copyBlob.getProperties().getCopyState().getCopyId());
        } catch (StorageException e) {
            if (!e.getErrorCode().contains("NoPendingCopyOperation")) {
                throw e;
            }
        }
    }

    /**
     * Create a snapshot
     */
    @Test
    public void testAppendBlobSnapshotValidationTest() throws StorageException,
            URISyntaxException, IOException {
        final int length = 1024;
        CloudAppendBlob appendBlobRef = (CloudAppendBlob) BlobTestHelper
                .uploadNewBlob(this.container, BlobType.APPEND_BLOB,
                        "originalBlob", length, null);
        final CloudBlob blobSnapshot = appendBlobRef.createSnapshot();

        for (ListBlobItem blob : this.container.listBlobs(null, true,
                EnumSet.allOf(BlobListingDetails.class), null, null)) {
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream(
                    length);
            ((CloudBlob) blob).download(outStream);
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);

        blobSnapshot.download(outStream);
        byte[] retrievedBuff = outStream.toByteArray();
        assertEquals(length, retrievedBuff.length);

        // Read operation should work fine.
        blobSnapshot.downloadAttributes();

        final CloudAppendBlob blobSnapshotUsingRootUri = this.container
                .getAppendBlobReference(appendBlobRef.getName(),
                        blobSnapshot.getSnapshotID());
        outStream = new ByteArrayOutputStream(length);

        blobSnapshotUsingRootUri.download(outStream);
        retrievedBuff = outStream.toByteArray();
        assertEquals(length, retrievedBuff.length);
        assertEquals(blobSnapshot.getSnapshotID(),
                blobSnapshotUsingRootUri.getSnapshotID());

        // Expect an IllegalArgumentException from upload.
        try {
            final Random randGenerator = new Random();
            final byte[] buff = new byte[length];
            randGenerator.nextBytes(buff);
            blobSnapshot.upload(new ByteArrayInputStream(buff), -1);
            fail("Expect an IllegalArgumentException from upload");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Cannot perform this operation on a blob representing a snapshot.",
                    e.getMessage());
        }

        // Expect an IllegalArgumentException from uploadMetadata.
        try {
            blobSnapshot.uploadMetadata();
            fail("Expect an IllegalArgumentException from uploadMetadata");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Cannot perform this operation on a blob representing a snapshot.",
                    e.getMessage());
        }

        // Expect an IllegalArgumentException from uploadProperties.
        try {
            blobSnapshot.uploadProperties();
            fail("Expect an IllegalArgumentException from uploadProperties");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Cannot perform this operation on a blob representing a snapshot.",
                    e.getMessage());
        }

        // Expect an IllegalArgumentException from createSnapshot.
        try {
            blobSnapshot.createSnapshot();
            fail("Expect an IllegalArgumentException from createSnapshot");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Cannot perform this operation on a blob representing a snapshot.",
                    e.getMessage());
        }
    }

    /**
     * Create a blob and try to download a range of its contents
     */
    @Test
    public void testAppendBlobDownloadRangeValidationTest()
            throws StorageException, URISyntaxException, IOException {
        final int length = 5 * 1024 * 1024;

        final String appendBlobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudAppendBlob appendBlobRef = this.container
                .getAppendBlobReference(appendBlobName);

        appendBlobRef
                .upload(BlobTestHelper.getRandomDataStream(length), length);

        // Download full blob
        appendBlobRef.download(new ByteArrayOutputStream());
        assertEquals(length, appendBlobRef.getProperties().getLength());

        // Download blob range.
        byte[] downloadBuffer = new byte[100];
        int downloadLength = appendBlobRef.downloadRangeToByteArray(0,
                (long) 100, downloadBuffer, 0);
        assertEquals(length, appendBlobRef.getProperties().getLength());
        assertEquals(100, downloadLength);
    }

    @Test
    public void testAppendBlobUploadFromStreamTest() throws URISyntaxException,
            StorageException, IOException {
        final String appendBlobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testAppendBlob");
        final CloudAppendBlob appendBlobRef = this.container
                .getAppendBlobReference(appendBlobName);

        int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper
                .getRandomDataStream(length);
        appendBlobRef.upload(srcStream, length);
        ByteArrayOutputStream dstStream = new ByteArrayOutputStream();
        appendBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream,
                new ByteArrayInputStream(dstStream.toByteArray()));

        length = 5 * 1024 * 1024;
        srcStream = BlobTestHelper.getRandomDataStream(length);
        appendBlobRef.upload(srcStream, length);
        dstStream = new ByteArrayOutputStream();
        appendBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream,
                new ByteArrayInputStream(dstStream.toByteArray()));
    }

    @Test
    public void testBlobUploadWithoutMD5Validation() throws URISyntaxException,
            StorageException, IOException {
        final String appendBlobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testAppendBlob");
        final CloudAppendBlob appendBlobRef = this.container
                .getAppendBlobReference(appendBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper
                .getRandomDataStream(length);
        BlobRequestOptions options = new BlobRequestOptions();
        options.setDisableContentMD5Validation(false);
        options.setStoreBlobContentMD5(false);

        appendBlobRef.upload(srcStream, length, null, options, null);
        appendBlobRef.downloadAttributes();
        appendBlobRef.getProperties().setContentMD5("MDAwMDAwMDA=");
        appendBlobRef.uploadProperties(null, options, null);

        try {
            appendBlobRef.download(new ByteArrayOutputStream(), null, options,
                    null);
            fail();
        } catch (StorageException ex) {
            assertEquals(306, ex.getHttpStatusCode());
            assertEquals("InvalidMd5", ex.getErrorCode());
        }

        options.setDisableContentMD5Validation(true);
        appendBlobRef
                .download(new ByteArrayOutputStream(), null, options, null);
    }

    @Test
    public void testBlobEmptyHeaderSigningTest() throws URISyntaxException,
            StorageException, IOException {
        final String appendBlobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testAppendBlob");
        final CloudAppendBlob appendBlobRef = this.container
                .getAppendBlobReference(appendBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper
                .getRandomDataStream(length);

        OperationContext context = new OperationContext();
        context.getSendingRequestEventHandler().addListener(
                new StorageEvent<SendingRequestEvent>() {

                    @Override
                    public void eventOccurred(SendingRequestEvent eventArg) {
                        HttpURLConnection connection = (HttpURLConnection) eventArg
                                .getConnectionObject();
                        connection.setRequestProperty("x-ms-foo", "");
                    }
                });

        appendBlobRef.upload(srcStream, length, null, null, context);
        appendBlobRef
                .download(new ByteArrayOutputStream(), null, null, context);
    }

    @Test
    public void testAppendBlobDownloadRangeTest() throws URISyntaxException,
            StorageException, IOException {
        byte[] buffer = BlobTestHelper.getRandomBuffer(2 * 1024);

        CloudAppendBlob blob = this.container.getAppendBlobReference("blob1");
        ByteArrayInputStream wholeBlob = new ByteArrayInputStream(buffer);
        BlobRequestOptions opt = new BlobRequestOptions();
        opt.setStoreBlobContentMD5(false);
        blob.upload(wholeBlob, 2 * 1024, null, opt, null);

        ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
        try {
            blob.downloadRange(0, Long.valueOf(0), blobStream);
        } catch (IndexOutOfBoundsException ex) {

        }

        blob.downloadRange(0, Long.valueOf(1024), blobStream);
        assertEquals(blobStream.size(), 1024);
        BlobTestHelper.assertStreamsAreEqualAtIndex(new ByteArrayInputStream(
                blobStream.toByteArray()), wholeBlob, 0, 0, 1024, 2 * 1024);

        CloudAppendBlob blob2 = this.container.getAppendBlobReference("blob1");
        try {
            blob.downloadRange(1024, Long.valueOf(0), blobStream);
        } catch (IndexOutOfBoundsException ex) {

        }

        ByteArrayOutputStream blobStream2 = new ByteArrayOutputStream();
        blob2.downloadRange(1024, Long.valueOf(1024), blobStream2);
        BlobTestHelper.assertStreamsAreEqualAtIndex(new ByteArrayInputStream(
                blobStream2.toByteArray()), wholeBlob, 0, 1024, 1024,
                2 * 1024);

        BlobTestHelper.assertAreEqual(blob, blob2);
    }

    @Test
    public void testCloudAppendBlobDownloadToByteArray()
            throws URISyntaxException, StorageException, IOException {
        CloudAppendBlob blob = this.container.getAppendBlobReference("blob1");
        BlobTestHelper.doDownloadTest(blob, 1 * 512, 2 * 512, 0);
        BlobTestHelper.doDownloadTest(blob, 1 * 512, 2 * 512, 1 * 512);
        BlobTestHelper.doDownloadTest(blob, 2 * 512, 4 * 512, 1 * 512);
        BlobTestHelper
                .doDownloadTest(blob, 5 * 1024 * 1024, 5 * 1024 * 1024, 0);
        BlobTestHelper.doDownloadTest(blob, 5 * 1024 * 1024, 6 * 1024 * 1024,
                512);
    }

    @Test
    public void testCloudAppendBlobDownloadRangeToByteArray()
            throws URISyntaxException, StorageException, IOException {
        CloudAppendBlob blob = this.container
                .getAppendBlobReference(BlobTestHelper
                        .generateRandomBlobNameWithPrefix("downloadrange"));

        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024,
                8 * 1024 * 1024, 1 * 1024 * 1024, Long.valueOf(1 * 1024 * 1024),
                Long.valueOf(5 * 1024 * 1024));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024,
                8 * 1024 * 1024, 2 * 1024 * 1024, Long.valueOf(2 * 1024 * 1024),
                Long.valueOf(6 * 1024 * 1024));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024,
                8 * 1024 * 1024, 1 * 1024 * 1024, Long.valueOf(4 * 1024 * 1024),
                Long.valueOf(4 * 1024 * 1024));

        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512,
                0, Long.valueOf(1 * 512), Long.valueOf(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512,
                1 * 512, Long.valueOf(0), null);
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512,
                1 * 512, Long.valueOf(1 * 512), null);
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512,
                1 * 512, Long.valueOf(0), Long.valueOf(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512,
                2 * 512, Long.valueOf(1 * 512), Long.valueOf(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512,
                2 * 512, Long.valueOf(1 * 512), Long.valueOf(2 * 512));

        // Edge cases
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 1023,
                Long.valueOf(1023), Long.valueOf(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0,
                Long.valueOf(1023), Long.valueOf(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0,
                Long.valueOf(0), Long.valueOf(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0,
                Long.valueOf(512), Long.valueOf(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 512,
                Long.valueOf(1023), Long.valueOf(1));
    }

    @Test
    public void testCloudAppendBlobDownloadRangeToByteArrayNegativeTest()
            throws URISyntaxException, StorageException, IOException {
        CloudAppendBlob blob = this.container
                .getAppendBlobReference(BlobTestHelper
                        .generateRandomBlobNameWithPrefix("downloadrangenegative"));
        BlobTestHelper.doDownloadRangeToByteArrayNegativeTests(blob);
    }

    @Test
    public void testCloudAppendBlobUploadFromStreamWithAccessCondition()
            throws URISyntaxException, StorageException, IOException {
        CloudAppendBlob blob1 = this.container.getAppendBlobReference("blob1");
        AccessCondition accessCondition = AccessCondition
                .generateIfNoneMatchCondition("\"*\"");
        final int length = 6 * 512;
        ByteArrayInputStream srcStream = BlobTestHelper
                .getRandomDataStream(length);
        blob1.upload(srcStream, length, accessCondition, null, null);

        srcStream.reset();
        blob1.createOrReplace();
        accessCondition = AccessCondition.generateIfNoneMatchCondition(blob1
                .getProperties().getEtag());
        try {
            blob1.upload(srcStream, length, accessCondition, null, null);
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED,
                    ex.getHttpStatusCode());
        }

        srcStream.reset();
        accessCondition = AccessCondition.generateIfMatchCondition(blob1
                .getProperties().getEtag());
        blob1.upload(srcStream, length, accessCondition, null, null);

        srcStream.reset();
        CloudAppendBlob blob2 = this.container.getAppendBlobReference("blob2");
        blob2.createOrReplace();
        accessCondition = AccessCondition.generateIfMatchCondition(blob1
                .getProperties().getEtag());
        try {
            blob1.upload(srcStream, length, accessCondition, null, null);
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED,
                    ex.getHttpStatusCode());
        }

        srcStream.reset();
        accessCondition = AccessCondition.generateIfNoneMatchCondition(blob2
                .getProperties().getEtag());
        blob1.upload(srcStream, length, accessCondition, null, null);
    }

    @Test
    public void testAppendBlobNamePlusEncodingTest() throws StorageException,
            URISyntaxException, IOException, InterruptedException {
        final int length = 1 * 1024;

        final CloudAppendBlob originalBlob = (CloudAppendBlob) BlobTestHelper.uploadNewBlob(
                this.container, BlobType.APPEND_BLOB, "a+b.txt", length, null);
        final CloudAppendBlob copyBlob = this.container.getAppendBlobReference(originalBlob.getName() + "copyed");

        copyBlob.startCopy(originalBlob);
        BlobTestHelper.waitForCopy(copyBlob);
        copyBlob.downloadAttributes();
    }

    @Test
    public void testAppendBlobInputStream() throws URISyntaxException,
            StorageException, IOException {
        final int blobLength = 16 * 1024;
        final Random randGenerator = new Random();
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blobRef = this.container
                .getAppendBlobReference(blobName);

        final byte[] buff = new byte[blobLength];
        randGenerator.nextBytes(buff);
        buff[0] = -1;
        buff[1] = -128;
        final ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);

        final BlobRequestOptions options = new BlobRequestOptions();
        final OperationContext operationContext = new OperationContext();
        options.setTimeoutIntervalInMs(90000);
        options.setRetryPolicyFactory(new RetryNoRetry());
        blobRef.upload(sourceStream, blobLength, null, options,
                operationContext);

        BlobInputStream blobStream = blobRef.openInputStream();

        for (int i = 0; i < blobLength; i++) {
            int data = blobStream.read();
            assertTrue(data >= 0);
            assertEquals(buff[i], (byte) data);
        }

        assertEquals(-1, blobStream.read());

        blobRef.delete();
    }

    @Test
    public void testAppendBlobUploadNegativeLength() throws URISyntaxException,
            StorageException, IOException {
        final int blobLength = 16 * 1024;

        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blobRef = this.container
                .getAppendBlobReference(blobName);

        final byte[] buff = BlobTestHelper.getRandomBuffer(blobLength);
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);
        
        blobRef.upload(sourceStream, -1);
        
        assertTrue(blobRef.exists());
        assertEquals(blobRef.getProperties().getLength(), blobLength);
    }

    @Test
    public void testAppendBlobMaxSizeCondition() throws URISyntaxException,
            StorageException, IOException {
        final int blobLength = 16 * 1024;

        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blobRef = this.container
                .getAppendBlobReference(blobName);

        final byte[] buff = BlobTestHelper.getRandomBuffer(blobLength);
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);
        
        // Max length position failure
        AccessCondition cond = new AccessCondition();
        cond.setIfMaxSizeLessThanOrEqual(blobLength - 1L);
        
        try {
            blobRef.upload(sourceStream, blobLength, cond, null, null);
            fail("Expected IOException for exceeding the max size");
        } catch (IOException ex) {
            assertEquals(SR.INVALID_BLOCK_SIZE, ex.getMessage());
        }
    }

    @Test
    public void testAppendBlobWriteStreamConditionalRetry() throws URISyntaxException,
            StorageException, IOException {
        final int blobLength = 16 * 1024;

        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blobRef = this.container
                .getAppendBlobReference(blobName);

        final byte[] buff = BlobTestHelper.getRandomBuffer(blobLength);

        final BlobRequestOptions options = new BlobRequestOptions();
        options.setAbsorbConditionalErrorsOnRetry(true);
        
        // Append position failure
        OperationContext ctx = new OperationContext();
        ctx.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            int count = 0;
            
            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                // This is the first try at appending. This error code will cause it to retry.
                // On the retry we set the error to append pos which should be ignored as the
                // absorb conditional errors on retry flag is set to true.
                if (count == 1) {
                    eventArg.getRequestResult().setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                } else if (count == 2) {
                    eventArg.getRequestResult().setStatusCode(HttpURLConnection.HTTP_PRECON_FAILED);
                    eventArg.getRequestResult().setStatusMessage(StorageErrorCodeStrings.INVALID_APPEND_POSITION);
                }
                count++;
            }
        });
        
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);
        blobRef.upload(sourceStream, blobLength, null, options, ctx);
        
        // Max length position failure
        AccessCondition cond = new AccessCondition();
        cond.setIfMaxSizeLessThanOrEqual((long)blobLength);
        
        ctx = new OperationContext();
        ctx.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            int count = 0;
            
            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                // This is the first try at appending. This error code will cause it to retry.
                // On the retry we set the error to max size which should be ignored as the
                // absorb conditional errors on retry flag is set to true.
                if (count == 1) {
                    eventArg.getRequestResult().setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                } else if (count == 2) {
                    eventArg.getRequestResult().setStatusCode(HttpURLConnection.HTTP_PRECON_FAILED);
                    eventArg.getRequestResult().setStatusMessage(StorageErrorCodeStrings.INVALID_MAX_BLOB_SIZE_CONDITION);
                }
                count++;
            }
        });
        
        sourceStream = new ByteArrayInputStream(buff);
        blobRef.upload(sourceStream, blobLength, cond, options, ctx);
    }

    @Test
    public void testUploadFromByteArray() throws Exception {
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blob = this.container
                .getAppendBlobReference(blobName);

        this.doUploadFromByteArrayTest(blob, 4 * 512, 0, 4 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 0, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 1 * 512, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 2 * 512, 2 * 512);
    }

    @Test
    public void testUploadDownloadFromFile() throws IOException,
            StorageException, URISyntaxException {
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blob = this.container
                .getAppendBlobReference(blobName);

        this.doUploadDownloadFileTest(blob, 512);
        this.doUploadDownloadFileTest(blob, 4096);
        this.doUploadDownloadFileTest(blob, 5 * 1024 * 1024);
        this.doUploadDownloadFileTest(blob, 11 * 1024 * 1024);
    }

    @Test
    public void testAppendBlobCopyTest() throws URISyntaxException,
            StorageException, InterruptedException, IOException {
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);

        CloudAppendBlob source = this.container
                .getAppendBlobReference("source");

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        source.upload(stream, buffer.length);
        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudAppendBlob copy = this.container.getAppendBlobReference("copy");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(source));
        BlobTestHelper.waitForCopy(copy);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        assertEquals(source.getSnapshotQualifiedUri().getPath(), copy.getCopyState()
                .getSource().getPath());
        assertEquals(buffer.length, copy.getCopyState().getTotalBytes()
                .intValue());
        assertEquals(buffer.length, copy.getCopyState().getBytesCopied()
                .intValue());
        assertEquals(copyId, copy.getCopyState().getCopyId());
        assertTrue(copy.getCopyState().getCompletionTime()
                .compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        try {
            copy.abortCopy(copy.getCopyState().getCopyId());
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_CONFLICT,
                    ex.getHttpStatusCode());
        }

        source.downloadAttributes();
        assertNotNull(copy.getProperties().getEtag());
        assertFalse(source.getProperties().getEtag()
                .equals(copy.getProperties().getEtag()));
        assertTrue(copy.getProperties().getLastModified()
                .compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        copy.download(copyStream);
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(
                copyStream.toByteArray()));

        copy.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = source.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(),
                prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", copy.getMetadata().get("Test"));

        copy.delete();
    }

    @Test
    public void testAppendBlobCopyWithMetadataOverride()
            throws URISyntaxException, StorageException, IOException, InterruptedException {
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);
        CloudAppendBlob source = this.container
                .getAppendBlobReference("source");

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        source.upload(stream, buffer.length);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudAppendBlob copy = this.container.getAppendBlobReference("copy");
        copy.getMetadata().put("Test2", "value2");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(source));
        BlobTestHelper.waitForCopy(copy);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        assertEquals(source.getSnapshotQualifiedUri().getPath(), copy.getCopyState()
                .getSource().getPath());
        assertEquals(buffer.length, copy.getCopyState().getTotalBytes()
                .intValue());
        assertEquals(buffer.length, copy.getCopyState().getBytesCopied()
                .intValue());
        assertEquals(copyId, copy.getCopyState().getCopyId());
        assertTrue(copy.getCopyState().getCompletionTime()
                .compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        copy.download(copyStream);
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(
                copyStream.toByteArray()));

        copy.downloadAttributes();
        source.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = source.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(),
                prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value2", copy.getMetadata().get("Test2"));
        assertFalse(copy.getMetadata().containsKey("Test"));

        copy.delete();
    }

    @Test
    public void testAppendBlobCopyFromSnapshot() throws StorageException,
            IOException, URISyntaxException, InterruptedException {
        CloudAppendBlob source = this.container
                .getAppendBlobReference("source");

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        source.upload(stream, buffer.length);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudAppendBlob snapshot = (CloudAppendBlob) source.createSnapshot();

        // Modify source
        byte[] buffer2 = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream2 = new ByteArrayInputStream(buffer2);
        source.getMetadata().put("Test", "newvalue");
        source.uploadMetadata();
        source.getProperties().setContentMD5(null);
        source.upload(stream2, buffer.length);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        source.download(outputStream);

        ByteArrayOutputStream snapshotStream = new ByteArrayOutputStream();
        snapshot.download(snapshotStream);
        BlobTestHelper.assertStreamsAreEqual(stream2, new ByteArrayInputStream(
                outputStream.toByteArray()));
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(
                snapshotStream.toByteArray()));

        source.downloadAttributes();
        snapshot.downloadAttributes();
        assertFalse(source.getMetadata().get("Test")
                .equals(snapshot.getMetadata().get("Test")));

        CloudAppendBlob copy = this.container.getAppendBlobReference("copy");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(snapshot));
        BlobTestHelper.waitForCopy(copy);

        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        copy.download(copyStream);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(
                copyStream.toByteArray()));
        assertEquals(copyId, copy.getProperties().getCopyState().getCopyId());

        copy.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = snapshot.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(),
                prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", copy.getMetadata().get("Test"));

        copy.delete();
    }

    private void doUploadFromByteArrayTest(CloudAppendBlob blob,
            int bufferSize, int bufferOffset, int count) throws Exception {
        byte[] buffer = BlobTestHelper.getRandomBuffer(bufferSize);
        byte[] downloadedBuffer = new byte[bufferSize];

        blob.uploadFromByteArray(buffer, bufferOffset, count);
        blob.downloadToByteArray(downloadedBuffer, 0);

        int i = 0;
        for (; i < count; i++) {
            assertEquals(buffer[i + bufferOffset], downloadedBuffer[i]);
        }

        for (; i < downloadedBuffer.length; i++) {
            assertEquals(0, downloadedBuffer[i]);
        }
    }

    private void doUploadDownloadFileTest(CloudAppendBlob blob, int fileSize)
            throws IOException, StorageException {
        File sourceFile = File.createTempFile("sourceFile", ".tmp");
        File destinationFile = new File(sourceFile.getParentFile(),
                "destinationFile.tmp");

        try {

            byte[] buffer = BlobTestHelper.getRandomBuffer(fileSize);
            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.write(buffer);
            fos.close();
            blob.uploadFromFile(sourceFile.getAbsolutePath());

            blob.downloadToFile(destinationFile.getAbsolutePath());
            assertTrue("Destination file does not exist.",
                    destinationFile.exists());
            assertEquals("Destination file does not match input file.",
                    fileSize, destinationFile.length());
            FileInputStream fis = new FileInputStream(destinationFile);

            byte[] readBuffer = new byte[fileSize];
            fis.read(readBuffer);
            fis.close();

            for (int i = 0; i < fileSize; i++) {
                assertEquals("File contents do not match.", buffer[i],
                        readBuffer[i]);
            }
        } finally {
            if (sourceFile.exists()) {
                sourceFile.delete();
            }

            if (destinationFile.exists()) {
                destinationFile.delete();
            }
        }
    }

    @Test
    public void testUploadDownloadBlobProperties() throws URISyntaxException,
            StorageException, IOException {
        final int length = 512;

        // do this to make sure the set MD5 can be compared without an exception
        // being thrown
        BlobRequestOptions options = new BlobRequestOptions();
        options.setDisableContentMD5Validation(true);

        // with explicit upload/download of properties
        String appendBlobName1 = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlockBlob");
        CloudAppendBlob appendBlobRef1 = this.container
                .getAppendBlobReference(appendBlobName1);

        appendBlobRef1.upload(BlobTestHelper.getRandomDataStream(length),
                length);

        // this is not set by upload (it is for page blob!), so set this
        // manually
        appendBlobRef1.getProperties().setLength(length);

        BlobTestHelper.setBlobProperties(appendBlobRef1);
        BlobProperties props1 = appendBlobRef1.getProperties();
        appendBlobRef1.uploadProperties();

        appendBlobRef1.downloadAttributes(null, options, null);
        BlobProperties props2 = appendBlobRef1.getProperties();

        BlobTestHelper.assertAreEqual(props1, props2);

        // by uploading/downloading the blob
        appendBlobName1 = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testBlockBlob");
        appendBlobRef1 = this.container.getAppendBlobReference(appendBlobName1);

        BlobTestHelper.setBlobProperties(appendBlobRef1);
        props1 = appendBlobRef1.getProperties();

        appendBlobRef1.upload(BlobTestHelper.getRandomDataStream(length),
                length);

        // this is not set by upload (it is for page blob!), so set this
        // manually
        appendBlobRef1.getProperties().setLength(length);

        appendBlobRef1.download(new ByteArrayOutputStream(), null, options,
                null);
        props2 = appendBlobRef1.getProperties();

        BlobTestHelper.assertAreEqual(props1, props2);
    }

    @Test
    public void testOpenOutputStream() throws URISyntaxException,
            StorageException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(8 * 512);

        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        final CloudAppendBlob blobRef = this.container
                .getAppendBlobReference(blobName);
        blobRef.createOrReplace();

        BlobOutputStream blobOutputStream = blobRef.openWriteNew();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        blobOutputStream = blobRef.openWriteNew();
        inputStream = new ByteArrayInputStream(buffer);
        blobOutputStream.write(inputStream, 512);

        inputStream = new ByteArrayInputStream(buffer, 512, 3 * 512);
        blobOutputStream.write(inputStream, 3 * 512);

        blobOutputStream.close();

        byte[] result = new byte[blobLengthToUse];
        blobRef.downloadToByteArray(result, 0);

        int i = 0;
        for (; i < 4 * 512; i++) {
            assertEquals(buffer[i], result[i]);
        }

        for (; i < 8 * 512; i++) {
            assertEquals(0, result[i]);
        }
    }

    @Test
    public void testOpenOutputStreamNoArgs() throws URISyntaxException,
            StorageException {
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        CloudAppendBlob appendBlob = this.container
                .getAppendBlobReference(blobName);

        try {
            appendBlob.openWriteExisting();
        } catch (StorageException ex) {
            assertEquals("The specified blob does not exist.", ex.getMessage());
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                    ex.getHttpStatusCode());
        }

        appendBlob.openWriteNew();
        appendBlob.openWriteExisting();

        CloudAppendBlob appendBlob2 = this.container
                .getAppendBlobReference(blobName);
        appendBlob2.downloadAttributes();
        assertEquals(0, appendBlob2.getProperties().getLength());
        assertEquals(BlobType.APPEND_BLOB, appendBlob2.getProperties().getBlobType());
    }

    @Test
    public void testOpenOutputStreamWithConditions() throws StorageException, IOException, URISyntaxException
    {
        int blobSize = 1024;
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        CloudAppendBlob blob = container.getAppendBlobReference(blobName);
       
        BlobOutputStream str = blob.openWriteNew();
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobSize);
        str.write(buffer);
        str.close();
        
        // Succeeded max size condition
        AccessCondition accessCondition = new AccessCondition();
        accessCondition.setIfMaxSizeLessThanOrEqual(1024*2+1L);
        str = blob.openWriteExisting(accessCondition, null, null);
        str.write(buffer);
        str.close();
        
        // Succeeded append position condition
        accessCondition = new AccessCondition();
        accessCondition.setIfAppendPositionEqual(1024*2L);
        str = blob.openWriteExisting(accessCondition, null, null);
        str.write(buffer);
        str.close();
        
        // Failed max size condition
        accessCondition = new AccessCondition();
        accessCondition.setIfMaxSizeLessThanOrEqual(1024 - 1L);
        try {
            str = blob.openWriteExisting(accessCondition, null, null);
            str.write(buffer);
            str.close();
            fail("Expected a condition failure.");
        } catch (IOException ex) {
            assertEquals(SR.INVALID_BLOCK_SIZE, ex.getMessage());
        }
        
        // Failed append position condition
        accessCondition = new AccessCondition();
        accessCondition.setIfAppendPositionEqual(1024 - 1L);
        try {
            str = blob.openWriteExisting(accessCondition, null, null);
            str.write(buffer);
            str.close();
            fail("Expected a condition failure.");
        } catch (IOException ex) {
            StorageException internalException = (StorageException)ex.getCause();
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, internalException.getHttpStatusCode());
            assertEquals("The append position condition specified was not met.", internalException.getMessage());
            assertEquals("AppendPositionConditionNotMet", internalException.getErrorCode());
        }
    }

    @Test
    public void testOpenOutputStreamMultiWriterFail() throws StorageException,
            IOException, URISyntaxException {
        int blobSize = 1024;
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobSize);
        
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        CloudAppendBlob blob = container.getAppendBlobReference(blobName);
        blob.createOrReplace();
        final ByteArrayInputStream sourceStream = new ByteArrayInputStream(buffer);
        blob.appendBlock(sourceStream, blobSize);
        
        // Create two streams to the same blob
        BlobOutputStream str = blob.openWriteExisting();
        BlobOutputStream str2 = blob.openWriteExisting();
        
        // These will write to an internal buffer and not throw as they do
        // not yet make a service call
        str.write(buffer);
        str2.write(buffer);
        
        // Append the data from the first stream (service call happens)
        str.close();
        
        // Failed append position condition
        try {
            // Append the data from the second stream which expects append
            // position to be 1024 from the intial download properties when
            // the stream was opened
            str2.close();
            fail("Expected a condition failure.");
        } catch (IOException ex) {
            StorageException internalException = (StorageException) ex
                    .getCause();
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED,
                    internalException.getHttpStatusCode());
            assertEquals(
                    "The append position condition specified was not met.",
                    internalException.getMessage());
            assertEquals("AppendPositionConditionNotMet",
                    internalException.getErrorCode());
        }
    }

    @Test
    public void testAppendBlockFromStream() throws StorageException, IOException, URISyntaxException
    {
        int blobSize = 2 * 1024;
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        CloudAppendBlob blob = container.getAppendBlobReference(blobName);
        blob.createOrReplace();

        // Append a block
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobSize);
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buffer);
        long pos = blob.appendBlock(sourceStream, -1);
        assertEquals(0, pos);

        // Download and verify equality
        byte[] resultBuffer = new byte[blobSize];
        blob.downloadToByteArray(resultBuffer, 0);
        for (int i = 0; i < blob.getProperties().getLength(); i++) {
            assertEquals(buffer[i], resultBuffer[i]);
        }
        
        // Append another block to check the position is updated correctly
        sourceStream = new ByteArrayInputStream(buffer);
        pos = blob.appendBlock(sourceStream, -1);
        assertEquals(blobSize, pos);
    }

    @Test
    public void testAppendBlockFromStreamWithConditions() throws StorageException, IOException, URISyntaxException
    {
        int blobSize = 1024;
        String blobName = BlobTestHelper
                .generateRandomBlobNameWithPrefix("testblob");
        CloudAppendBlob blob = this.container.getAppendBlobReference(blobName);
        blob.createOrReplace();

        byte[] buffer = BlobTestHelper.getRandomBuffer(blobSize);
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buffer);
        blob.appendBlock(sourceStream, -1); 
        
        // Succeeded max size condition
        AccessCondition accessCondition = new AccessCondition();
        accessCondition.setIfMaxSizeLessThanOrEqual(1024*2+1L);
        sourceStream = new ByteArrayInputStream(buffer);
        blob.appendBlock(sourceStream, -1, accessCondition, null, null);
        
        // Succeeded append position condition
        accessCondition = new AccessCondition();
        accessCondition.setIfAppendPositionEqual(1024*2L);
        sourceStream = new ByteArrayInputStream(buffer);
        blob.appendBlock(sourceStream, -1, accessCondition, null, null);
        
        // Failed max size condition
        accessCondition = new AccessCondition();
        accessCondition.setIfMaxSizeLessThanOrEqual(1024 - 1L);
        try {
            sourceStream = new ByteArrayInputStream(buffer);
            blob.appendBlock(sourceStream, -1, accessCondition, null, null);
            fail("Expected a condition failure.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED,
                    ex.getHttpStatusCode());
            assertEquals("The max blob size condition specified was not met.", ex.getMessage());
            assertEquals("MaxBlobSizeConditionNotMet", ex.getErrorCode());
        }
        
        // Failed append position condition
        accessCondition = new AccessCondition();
        accessCondition.setIfAppendPositionEqual(1024 - 1L);
        try {
            sourceStream = new ByteArrayInputStream(buffer);
            blob.appendBlock(sourceStream, -1, accessCondition, null, null);
            fail("Expected a condition failure.");
        } catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED,
                    ex.getHttpStatusCode());
            assertEquals("The append position condition specified was not met.", ex.getMessage());
            assertEquals("AppendPositionConditionNotMet", ex.getErrorCode());
        }
    }

    @Test
    public void testAppendBlobUploadFromStream() throws StorageException, IOException, URISyntaxException
    {
        byte[] buffer = BlobTestHelper.getRandomBuffer(6 * 1024 * 1024);

        CloudAppendBlob blob = this.container.getAppendBlobReference("blob1");

        blob.upload(new ByteArrayInputStream(buffer), buffer.length);
        blob.downloadAttributes();
        assertEquals(6 * 1024 * 1024, blob.getProperties().getLength());

        blob.upload(new ByteArrayInputStream(buffer), buffer.length);
        blob.downloadAttributes();
        assertEquals(6 * 1024 * 1024, blob.getProperties().getLength());

        blob.upload(new ByteArrayInputStream(buffer), buffer.length, null /* accessCondition */, null /* options */,
                null /* operationContext */);
        blob.downloadAttributes();
        assertEquals(6 * 1024 * 1024, blob.getProperties().getLength());
    }

    @Test
    public void testAppendBlobAppendFromStream() throws StorageException, IOException, URISyntaxException
    {
        // Every time append a buffer that is bigger than a single block.
        byte[] buffer = BlobTestHelper.getRandomBuffer(6 * 1024 * 1024);

        CloudAppendBlob blob = this.container.getAppendBlobReference("blob1");
        blob.createOrReplace();

        blob.append(new ByteArrayInputStream(buffer), buffer.length);
        blob.downloadAttributes();
        assertEquals(6 * 1024 * 1024, blob.getProperties().getLength());

        blob.append(new ByteArrayInputStream(buffer), buffer.length);
        blob.downloadAttributes();
        assertEquals(12 * 1024 * 1024, blob.getProperties().getLength());

        blob.append(new ByteArrayInputStream(buffer), buffer.length, null /* accessCondition */, null /* options */, 
                null /* operationContext */);
        blob.downloadAttributes();
        assertEquals(18 * 1024 * 1024, blob.getProperties().getLength());
    }

    @Test
    public void testAppendBlobAppendFromStreamWithLength() throws StorageException, IOException, URISyntaxException
    {
        // Every time append a buffer that is bigger than a single block.
        byte[] buffer = BlobTestHelper.getRandomBuffer(6 * 1024 * 1024);

        CloudAppendBlob blob = this.container.getAppendBlobReference("blob1");
        blob.createOrReplace();

        blob.append(new ByteArrayInputStream(buffer), 5 * 1024 * 1024);
        blob.downloadAttributes();
        assertEquals(5 * 1024 * 1024, blob.getProperties().getLength());

        blob.append(new ByteArrayInputStream(buffer), 5 * 1024 * 1024);
        blob.downloadAttributes();
        assertEquals(10 * 1024 * 1024, blob.getProperties().getLength());

        blob.append(new ByteArrayInputStream(buffer), 5 * 1024 * 1024, null /* accessCondition */, null /* options */, 
                null /* operationContext */);
        blob.downloadAttributes();
        assertEquals(15 * 1024 * 1024, blob.getProperties().getLength());
    }
}