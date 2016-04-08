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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class CloudPageBlobTests {
    protected CloudBlobContainer container;

    @Before
    public void pageBlobTestMethodSetup() throws URISyntaxException, StorageException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
    }

    @After
    public void pageBlobTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    /**
     * Start copying a blob and then abort
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCopyFromPageBlobAbortTest() throws StorageException, URISyntaxException, IOException {
        final int length = 512;
        CloudPageBlob originalBlob = (CloudPageBlob) BlobTestHelper.uploadNewBlob(
                this.container, BlobType.PAGE_BLOB, "originalBlob", length, null);
        CloudPageBlob copyBlob = this.container.getPageBlobReference(originalBlob.getName() + "copyed");
        copyBlob.startCopy(originalBlob);

        try {
            copyBlob.abortCopy(copyBlob.getProperties().getCopyState().getCopyId());
        }
        catch (StorageException e) {
            if (!e.getErrorCode().contains("NoPendingCopyOperation")) {
                throw e;
            }
        }
    }

    /**
     * Create a snapshot
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testPageBlobSnapshotValidationTest() throws StorageException, URISyntaxException, IOException {
        final int length = 1024;
        CloudPageBlob blockBlobRef = (CloudPageBlob) BlobTestHelper.uploadNewBlob(this.container, BlobType.PAGE_BLOB,
                "originalBlob", length, null);
        final CloudBlob blobSnapshot = blockBlobRef.createSnapshot();

        for (ListBlobItem blob : this.container.listBlobs(null, true, EnumSet.allOf(BlobListingDetails.class), null,
                null)) {
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);
            ((CloudBlob) blob).download(outStream);
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);

        blobSnapshot.download(outStream);
        byte[] retrievedBuff = outStream.toByteArray();
        assertEquals(length, retrievedBuff.length);

        // Read operation should work fine.
        blobSnapshot.downloadAttributes();

        final CloudPageBlob blobSnapshotUsingRootUri = this.container.getPageBlobReference(blockBlobRef.getName(),
                blobSnapshot.getSnapshotID());
        outStream = new ByteArrayOutputStream(length);

        blobSnapshotUsingRootUri.download(outStream);
        retrievedBuff = outStream.toByteArray();
        assertEquals(length, retrievedBuff.length);
        assertEquals(blobSnapshot.getSnapshotID(), blobSnapshotUsingRootUri.getSnapshotID());

        // Expect an IllegalArgumentException from upload.
        try {
            final Random randGenerator = new Random();
            final byte[] buff = new byte[length];
            randGenerator.nextBytes(buff);
            blobSnapshot.upload(new ByteArrayInputStream(buff), -1);
            fail("Expect an IllegalArgumentException from upload");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }

        // Expect an IllegalArgumentException from uploadMetadata.
        try {
            blobSnapshot.uploadMetadata();
            fail("Expect an IllegalArgumentException from uploadMetadata");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }

        // Expect an IllegalArgumentException from uploadProperties.
        try {
            blobSnapshot.uploadProperties();
            fail("Expect an IllegalArgumentException from uploadProperties");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }

        // Expect an IllegalArgumentException from createSnapshot.
        try {
            blobSnapshot.createSnapshot();
            fail("Expect an IllegalArgumentException from createSnapshot");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot perform this operation on a blob representing a snapshot.", e.getMessage());
        }
    }

    /**
     * Create a blob and try to download a range of its contents
     * 
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testPageBlobDownloadRangeValidationTest() throws StorageException, URISyntaxException, IOException {
        final int length = 5 * 1024 * 1024;

        final String blockBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        final CloudPageBlob pageBlobRef = this.container.getPageBlobReference(blockBlobName);

        pageBlobRef.upload(BlobTestHelper.getRandomDataStream(length), length);

        //Download full blob
        pageBlobRef.download(new ByteArrayOutputStream());
        assertEquals(length, pageBlobRef.getProperties().getLength());

        //Download blob range.
        byte[] downloadBuffer = new byte[100];
        int downloadLength = pageBlobRef.downloadRangeToByteArray(0, (long) 100, downloadBuffer, 0);
        assertEquals(length, pageBlobRef.getProperties().getLength());
        assertEquals(100, downloadLength);
    }

    @Test
    public void testPageBlobUploadFromStreamTest() throws URISyntaxException, StorageException, IOException {
        final String pageBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testPageBlob");
        final CloudPageBlob pageBlobRef = this.container.getPageBlobReference(pageBlobName);

        int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        pageBlobRef.upload(srcStream, length);
        ByteArrayOutputStream dstStream = new ByteArrayOutputStream();
        pageBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream, new ByteArrayInputStream(dstStream.toByteArray()));

        length = 5 * 1024 * 1024;
        srcStream = BlobTestHelper.getRandomDataStream(length);
        pageBlobRef.upload(srcStream, length);
        dstStream = new ByteArrayOutputStream();
        pageBlobRef.download(dstStream);
        BlobTestHelper.assertStreamsAreEqual(srcStream, new ByteArrayInputStream(dstStream.toByteArray()));
    }

    @Test
    public void testBlobUploadWithoutMD5Validation() throws URISyntaxException, StorageException, IOException {
        final String pageBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testPageBlob");
        final CloudPageBlob pageBlobRef = this.container.getPageBlobReference(pageBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        BlobRequestOptions options = new BlobRequestOptions();
        options.setDisableContentMD5Validation(false);
        options.setStoreBlobContentMD5(false);

        pageBlobRef.upload(srcStream, length, null, options, null);
        pageBlobRef.downloadAttributes();
        pageBlobRef.getProperties().setContentMD5("MDAwMDAwMDA=");
        pageBlobRef.uploadProperties(null, options, null);

        try {
            pageBlobRef.download(new ByteArrayOutputStream(), null, options, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(306, ex.getHttpStatusCode());
            assertEquals("InvalidMd5", ex.getErrorCode());
        }

        options.setDisableContentMD5Validation(true);
        pageBlobRef.download(new ByteArrayOutputStream(), null, options, null);
    }

    @Test
    public void testBlobEmptyHeaderSigningTest() throws URISyntaxException, StorageException, IOException {
        final String pageBlobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testPageBlob");
        final CloudPageBlob pageBlobRef = this.container.getPageBlobReference(pageBlobName);

        final int length = 2 * 1024;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);

        OperationContext context = new OperationContext();
        context.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                HttpURLConnection connection = (HttpURLConnection) eventArg.getConnectionObject();
                connection.setRequestProperty("x-ms-foo", "");
            }
        });

        pageBlobRef.upload(srcStream, length, null, null, context);
        pageBlobRef.download(new ByteArrayOutputStream(), null, null, context);
    }

    @Test
    public void testPageBlobDownloadRangeTest() throws URISyntaxException, StorageException, IOException {
        byte[] buffer = BlobTestHelper.getRandomBuffer(2 * 1024);

        CloudPageBlob blob = this.container.getPageBlobReference("blob1");
        ByteArrayInputStream wholeBlob = new ByteArrayInputStream(buffer);
        blob.upload(wholeBlob, 2 * 1024);

        ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
        try {
            blob.downloadRange(0, new Long(0), blobStream);
        }
        catch (IndexOutOfBoundsException ex) {

        }

        blob.downloadRange(0, new Long(1024), blobStream);
        assertEquals(blobStream.size(), 1024);
        BlobTestHelper.assertStreamsAreEqualAtIndex(new ByteArrayInputStream(blobStream.toByteArray()), wholeBlob, 0,
                0, 1024, 2 * 1024);

        CloudPageBlob blob2 = this.container.getPageBlobReference("blob1");
        try {
            blob.downloadRange(1024, new Long(0), blobStream);
        }
        catch (IndexOutOfBoundsException ex) {

        }

        ByteArrayOutputStream blobStream2 = new ByteArrayOutputStream();
        blob2.downloadRange(1024, new Long(1024), blobStream2);
        BlobTestHelper.assertStreamsAreEqualAtIndex(new ByteArrayInputStream(blobStream2.toByteArray()), wholeBlob,
                0, 1024, 1024, 2 * 1024);

        BlobTestHelper.assertAreEqual(blob, blob2);
    }

    @Test
    public void testCloudPageBlobDownloadToByteArray() throws URISyntaxException, StorageException, IOException {
        CloudPageBlob blob = this.container.getPageBlobReference("blob1");
        BlobTestHelper.doDownloadTest(blob, 1 * 512, 2 * 512, 0);
        BlobTestHelper.doDownloadTest(blob, 1 * 512, 2 * 512, 1 * 512);
        BlobTestHelper.doDownloadTest(blob, 2 * 512, 4 * 512, 1 * 512);
        BlobTestHelper.doDownloadTest(blob, 5 * 1024 * 1024, 5 * 1024 * 1024, 0);
        BlobTestHelper.doDownloadTest(blob, 5 * 1024 * 1024, 6 * 1024 * 1024, 512);
    }

    @Test
    public void testCloudPageBlobDownloadRangeToByteArray() throws URISyntaxException, StorageException, IOException {
        CloudPageBlob blob = this.container.getPageBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("downloadrange"));

        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024, 8 * 1024 * 1024, 1 * 1024 * 1024,
                new Long(1 * 1024 * 1024), new Long(5 * 1024 * 1024));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024, 8 * 1024 * 1024, 2 * 1024 * 1024,
                new Long(2 * 1024 * 1024), new Long(6 * 1024 * 1024));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 8 * 1024 * 1024, 8 * 1024 * 1024, 1 * 1024 * 1024,
                new Long(4 * 1024 * 1024), new Long(4 * 1024 * 1024));

        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 0, new Long(1 * 512), new Long(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 1 * 512, new Long(0), null);
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 1 * 512, new Long(1 * 512), null);
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 1 * 512, new Long(0), new Long(1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 2 * 512, new Long(1 * 512), new Long(
                1 * 512));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 2 * 512, 4 * 512, 2 * 512, new Long(1 * 512), new Long(
                2 * 512));

        // Edge cases
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 1023, new Long(1023), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0, new Long(1023), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0, new Long(0), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 0, new Long(512), new Long(1));
        BlobTestHelper.doDownloadRangeToByteArrayTest(blob, 1024, 1024, 512, new Long(1023), new Long(1));
    }

    @Test
    public void testCloudPageBlobDownloadRangeToByteArrayNegativeTest() throws URISyntaxException, StorageException,
            IOException {
        CloudPageBlob blob = this.container.getPageBlobReference(BlobTestHelper
                .generateRandomBlobNameWithPrefix("downloadrangenegative"));
        BlobTestHelper.doDownloadRangeToByteArrayNegativeTests(blob);
    }

    @Test
    public void testCloudPageBlobUploadFromStreamWithAccessCondition() throws URISyntaxException, StorageException,
            IOException {
        CloudPageBlob blob1 = this.container.getPageBlobReference("blob1");
        AccessCondition accessCondition = AccessCondition.generateIfNoneMatchCondition("\"*\"");
        final int length = 6 * 512;
        ByteArrayInputStream srcStream = BlobTestHelper.getRandomDataStream(length);
        blob1.upload(srcStream, length, accessCondition, null, null);

        srcStream.reset();
        blob1.create(1024);
        accessCondition = AccessCondition.generateIfNoneMatchCondition(blob1.getProperties().getEtag());
        try {
            blob1.upload(srcStream, length, accessCondition, null, null);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getHttpStatusCode());
        }

        srcStream.reset();
        accessCondition = AccessCondition.generateIfMatchCondition(blob1.getProperties().getEtag());
        blob1.upload(srcStream, length, accessCondition, null, null);

        srcStream.reset();
        CloudPageBlob blob2 = this.container.getPageBlobReference("blob2");
        blob2.create(1024);
        accessCondition = AccessCondition.generateIfMatchCondition(blob1.getProperties().getEtag());
        try {
            blob1.upload(srcStream, length, accessCondition, null, null);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getHttpStatusCode());
        }

        srcStream.reset();
        accessCondition = AccessCondition.generateIfNoneMatchCondition(blob2.getProperties().getEtag());
        blob1.upload(srcStream, length, accessCondition, null, null);
    }

    /**
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testPageBlobNamePlusEncodingTest() throws StorageException, URISyntaxException, IOException,
            InterruptedException {
        final int length = 1 * 1024;

        final CloudPageBlob originalBlob = (CloudPageBlob) BlobTestHelper.uploadNewBlob(this.container,
                BlobType.PAGE_BLOB, "a+b.txt", length, null);
        final CloudPageBlob copyBlob = this.container.getPageBlobReference(originalBlob.getName() + "copyed");

        copyBlob.startCopy(originalBlob);
        BlobTestHelper.waitForCopy(copyBlob);
        copyBlob.downloadAttributes();
    }

    /**
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    @Test
    public void testPageBlobInputStream() throws URISyntaxException, StorageException, IOException {
        final int blobLength = 16 * 1024;
        final Random randGenerator = new Random();
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blobRef = this.container.getPageBlobReference(blobName);

        final byte[] buff = new byte[blobLength];
        randGenerator.nextBytes(buff);
        buff[0] = -1;
        buff[1] = -128;
        final ByteArrayInputStream sourceStream = new ByteArrayInputStream(buff);

        final BlobRequestOptions options = new BlobRequestOptions();
        final OperationContext operationContext = new OperationContext();
        options.setTimeoutIntervalInMs(90000);
        options.setRetryPolicyFactory(new RetryNoRetry());
        blobRef.upload(sourceStream, blobLength, null, options, operationContext);

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
    public void testUploadFromByteArray() throws Exception {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blob = this.container.getPageBlobReference(blobName);

        this.doUploadFromByteArrayTest(blob, 4 * 512, 0, 4 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 0, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 1 * 512, 2 * 512);
        this.doUploadFromByteArrayTest(blob, 4 * 512, 2 * 512, 2 * 512);
    }

    @Test
    public void testUploadDownloadFromFile() throws IOException, StorageException, URISyntaxException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blob = this.container.getPageBlobReference(blobName);

        this.doUploadDownloadFileTest(blob, 512);
        this.doUploadDownloadFileTest(blob, 4096);
        this.doUploadDownloadFileTest(blob, 5 * 1024 * 1024);
        this.doUploadDownloadFileTest(blob, 11 * 1024 * 1024);
    }

    @Test
    public void testPageBlobCopyTest() throws URISyntaxException, StorageException, InterruptedException, IOException {
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);

        CloudPageBlob source = this.container.getPageBlobReference("source");

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        source.upload(stream, buffer.length);
        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudPageBlob copy = this.container.getPageBlobReference("copy");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(source));
        BlobTestHelper.waitForCopy(copy);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        assertEquals(source.getQualifiedUri().getPath(), copy.getCopyState().getSource().getPath());
        assertEquals(buffer.length, copy.getCopyState().getTotalBytes().intValue());
        assertEquals(buffer.length, copy.getCopyState().getBytesCopied().intValue());
        assertEquals(copyId, copy.getCopyState().getCopyId());
        assertTrue(copy.getCopyState().getCompletionTime().compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        try {
            copy.abortCopy(copy.getCopyState().getCopyId());
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_CONFLICT, ex.getHttpStatusCode());
        }

        source.downloadAttributes();
        assertNotNull(copy.getProperties().getEtag());
        assertFalse(source.getProperties().getEtag().equals(copy.getProperties().getEtag()));
        assertTrue(copy.getProperties().getLastModified().compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        copy.download(copyStream);
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(copyStream.toByteArray()));

        copy.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = source.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", copy.getMetadata().get("Test"));

        copy.delete();
    }

    @Test
    public void testPageBlobCopyWithMetadataOverride() throws URISyntaxException, StorageException, IOException,
            InterruptedException {
        Calendar calendar = Calendar.getInstance(Utility.UTC_ZONE);
        CloudPageBlob source = this.container.getPageBlobReference("source");

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        source.upload(stream, buffer.length);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudPageBlob copy = this.container.getPageBlobReference("copy");
        copy.getMetadata().put("Test2", "value2");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(source));
        BlobTestHelper.waitForCopy(copy);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        assertEquals(source.getQualifiedUri().getPath(), copy.getCopyState().getSource().getPath());
        assertEquals(buffer.length, copy.getCopyState().getTotalBytes().intValue());
        assertEquals(buffer.length, copy.getCopyState().getBytesCopied().intValue());
        assertEquals(copyId, copy.getCopyState().getCopyId());
        assertTrue(copy.getCopyState().getCompletionTime().compareTo(new Date(calendar.get(Calendar.MINUTE) - 1)) > 0);

        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        copy.download(copyStream);
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(copyStream.toByteArray()));

        copy.downloadAttributes();
        source.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = source.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value2", copy.getMetadata().get("Test2"));
        assertFalse(copy.getMetadata().containsKey("Test"));

        copy.delete();
    }

    @Test
    public void testPageBlobCopyFromSnapshot() throws StorageException, IOException, URISyntaxException,
            InterruptedException {
        CloudPageBlob source = this.container.getPageBlobReference("source");

        byte[] buffer = BlobTestHelper.getRandomBuffer(512);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        source.upload(stream, buffer.length);

        source.getMetadata().put("Test", "value");
        source.uploadMetadata();

        CloudPageBlob snapshot = (CloudPageBlob) source.createSnapshot();

        //Modify source
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
        BlobTestHelper.assertStreamsAreEqual(stream2, new ByteArrayInputStream(outputStream.toByteArray()));
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(snapshotStream.toByteArray()));

        source.downloadAttributes();
        snapshot.downloadAttributes();
        assertFalse(source.getMetadata().get("Test").equals(snapshot.getMetadata().get("Test")));

        CloudPageBlob copy = this.container.getPageBlobReference("copy");
        String copyId = copy.startCopy(BlobTestHelper.defiddler(snapshot));
        BlobTestHelper.waitForCopy(copy);

        ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        copy.download(copyStream);

        assertEquals(CopyStatus.SUCCESS, copy.getCopyState().getStatus());
        BlobTestHelper.assertStreamsAreEqual(stream, new ByteArrayInputStream(copyStream.toByteArray()));
        assertEquals(copyId, copy.getProperties().getCopyState().getCopyId());

        copy.downloadAttributes();
        BlobProperties prop1 = copy.getProperties();
        BlobProperties prop2 = snapshot.getProperties();

        assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
        assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
        assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
        assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
        assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
        assertEquals(prop1.getContentType(), prop2.getContentType());

        assertEquals("value", copy.getMetadata().get("Test"));

        copy.delete();
    }

    private void doUploadFromByteArrayTest(CloudPageBlob blob, int bufferSize, int bufferOffset, int count)
            throws Exception {
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

    private void doUploadDownloadFileTest(CloudPageBlob blob, int fileSize) throws IOException, StorageException {
        File sourceFile = File.createTempFile("sourceFile", ".tmp");
        File destinationFile = new File(sourceFile.getParentFile(), "destinationFile.tmp");

        try {

            byte[] buffer = BlobTestHelper.getRandomBuffer(fileSize);
            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.write(buffer);
            fos.close();
            blob.uploadFromFile(sourceFile.getAbsolutePath());

            blob.downloadToFile(destinationFile.getAbsolutePath());
            assertTrue("Destination file does not exist.", destinationFile.exists());
            assertEquals("Destination file does not match input file.", fileSize, destinationFile.length());
            FileInputStream fis = new FileInputStream(destinationFile);

            byte[] readBuffer = new byte[fileSize];
            fis.read(readBuffer);
            fis.close();

            for (int i = 0; i < fileSize; i++) {
                assertEquals("File contents do not match.", buffer[i], readBuffer[i]);
            }
        }
        finally {
            if (sourceFile.exists()) {
                sourceFile.delete();
            }

            if (destinationFile.exists()) {
                destinationFile.delete();
            }
        }
    }

    @Test
    public void testUploadPages() throws URISyntaxException, StorageException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(8 * 512);

        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blobRef = this.container.getPageBlobReference(blobName);
        blobRef.create(blobLengthToUse);
        assertNull(blobRef.getProperties().getPageBlobSequenceNumber());

        // Upload one page (page 0)
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        blobRef.uploadPages(inputStream, 0, 512);
        assertNotNull(blobRef.getProperties().getPageBlobSequenceNumber());

        // Upload pages 2-4
        inputStream = new ByteArrayInputStream(buffer, 512, 3 * 512);
        blobRef.uploadPages(inputStream, 2 * 512, 3 * 512);
        assertNotNull(blobRef.getProperties().getPageBlobSequenceNumber());

        // Now, we expect the first 512 bytes of the blob to be the first 512 bytes of the random buffer (page 0)
        // the next 512 bytes should be 0 (page 1)
        // The next 3 * 512 bytes should be equal to bytes (512 -> 4 * 512) of the random buffer (pages 2-4)
        // The next 3 * 512 bytes should be 0 (pages 5-7)

        byte[] result = new byte[blobLengthToUse];
        blobRef.downloadToByteArray(result, 0);

        for (int i = 0; i < 512; i++) {
            assertEquals(buffer[i], result[i]);
        }

        for (int i = 0; i < 512; i++) {
            assertEquals(0, result[i + 512]);
        }

        for (int i = 0; i < 3 * 512; i++) {
            assertEquals(buffer[i + 512], result[i + 2 * 512]);
        }

        for (int i = 0; i < 3 * 512; i++) {
            assertEquals(0, result[i + 5 * 512]);
        }

        inputStream = new ByteArrayInputStream(buffer);

        try {
            blobRef.uploadPages(inputStream, 0, 256);
            fail("Did not throw expected exception on non-512-byte-aligned length");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_PAGE_BLOB_LENGTH, ex.getMessage());
        }

        try {
            blobRef.uploadPages(inputStream, 3 * 256, 3 * 512);
            fail("Did not throw expected exception on non-512-byte-aligned offset");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_PAGE_START_OFFSET, ex.getMessage());
        }
    }

    @Test
    public void testClearPages() throws URISyntaxException, StorageException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(8 * 512);

        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blobRef = this.container.getPageBlobReference(blobName);
        blobRef.create(blobLengthToUse);
        assertNull(blobRef.getProperties().getPageBlobSequenceNumber());

        // Upload one page (page 0)
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        blobRef.uploadPages(inputStream, 0, blobLengthToUse);
        assertNotNull(blobRef.getProperties().getPageBlobSequenceNumber());

        try {
            blobRef.clearPages(0, 256);
            fail("Did not throw expected exception on non-512-byte-aligned length");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_PAGE_BLOB_LENGTH, ex.getMessage());
        }

        try {
            blobRef.clearPages(3 * 256, 3 * 512);
            fail("Did not throw expected exception on non-512-byte-aligned offset");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_PAGE_START_OFFSET, ex.getMessage());
        }

        blobRef.clearPages(3 * 512, 2 * 512);
        assertNotNull(blobRef.getProperties().getPageBlobSequenceNumber());

        byte[] result = new byte[blobLengthToUse];
        blobRef.downloadToByteArray(result, 0);

        int i = 0;

        for (; i < 3 * 512; i++) {
            assertEquals(buffer[i], result[i]);
        }

        for (; i < 5 * 512; i++) {
            assertEquals(0, result[i]);
        }

        for (; i < 8 * 512; i++) {
            assertEquals(buffer[i], result[i]);
        }
    }

    @Test
    public void testResize() throws StorageException, URISyntaxException {
        CloudPageBlob blob = this.container.getPageBlobReference("blob1");
        CloudPageBlob blob2 = this.container.getPageBlobReference("blob1");

        blob.create(1024);
        assertEquals(1024, blob.getProperties().getLength());
        assertNull(blob.getProperties().getPageBlobSequenceNumber());

        blob2.downloadAttributes();
        assertEquals(1024, blob2.getProperties().getLength());
        assertNull(blob.getProperties().getPageBlobSequenceNumber());

        blob2.getProperties().setContentType("text/plain");
        blob2.uploadProperties();

        blob.resize(2048);
        assertEquals(2048, blob.getProperties().getLength());
        assertNotNull(blob.getProperties().getPageBlobSequenceNumber());

        blob.downloadAttributes();
        assertEquals("text/plain", blob.getProperties().getContentType());
        assertNotNull(blob.getProperties().getPageBlobSequenceNumber());

        blob2.downloadAttributes();
        assertEquals(2048, blob2.getProperties().getLength());
        assertNotNull(blob.getProperties().getPageBlobSequenceNumber());
    }

    private CloudPageBlob setUpPageRanges() throws StorageException, URISyntaxException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobLengthToUse);

        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blobRef = this.container.getPageBlobReference(blobName);
        blobRef.create(blobLengthToUse);

        // Upload page 0
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        blobRef.uploadPages(inputStream, 0, 512);

        // Upload pages 2-4
        inputStream = new ByteArrayInputStream(buffer, 512, 3 * 512);
        blobRef.uploadPages(inputStream, 2 * 512, 3 * 512);
        
        // Upload page 6
        inputStream = new ByteArrayInputStream(buffer, 3 * 512, 512);
        blobRef.uploadPages(inputStream, 6 * 512, 512);

        // Page0: 512 bytes should be the first 512 bytes of the random buffer (page 0)
        // Page1: 512 bytes should be 0
        // Page2-4: 3 * 512 bytes should be equal to bytes (512 -> 4 * 512) of the random buffer (pages 2-4)
        // Page5: 512 bytes should be 0
        // Page6: 512 bytes should be the 4th 512 byte segmented of the random buffer
        // Page7-8: 2 * 512 bytes should be 0
        return blobRef;
    }
    
    @Test
    public void testDownloadPages() throws StorageException, URISyntaxException, IOException {
        final CloudPageBlob blobRef = setUpPageRanges();

        ArrayList<PageRange> actualPageRanges = blobRef.downloadPageRanges();
        ArrayList<PageRange> expectedPageRanges = new ArrayList<PageRange>();
        expectedPageRanges.add(new PageRange(0, 512 - 1));
        expectedPageRanges.add(new PageRange(2 * 512, 5 * 512 - 1));
        expectedPageRanges.add(new PageRange(6 * 512, 7 * 512 - 1));

        assertEquals(expectedPageRanges.size(), actualPageRanges.size());
        for (int i = 0; i < expectedPageRanges.size(); i++) {
            assertEquals(expectedPageRanges.get(i).getStartOffset(), actualPageRanges.get(i).getStartOffset());
            assertEquals(expectedPageRanges.get(i).getEndOffset(), actualPageRanges.get(i).getEndOffset());
        }
    }
    
    @Test
    public void testDownloadPageRangesWithOffset() throws StorageException, URISyntaxException, IOException {
        final CloudPageBlob blobRef = setUpPageRanges();
        
        List<PageRange> actualPageRanges = blobRef.downloadPageRanges((long)1*512, null);
        List<PageRange> expectedPageRanges = new ArrayList<PageRange>();
        expectedPageRanges.add(new PageRange(2 * 512, 5 * 512 - 1));
        expectedPageRanges.add(new PageRange(6 * 512, 7 * 512 - 1));

        assertEquals(expectedPageRanges.size(), actualPageRanges.size());
        for (int i = 0; i < expectedPageRanges.size(); i++) {
            assertEquals(expectedPageRanges.get(i).getStartOffset(), actualPageRanges.get(i).getStartOffset());
            assertEquals(expectedPageRanges.get(i).getEndOffset(), actualPageRanges.get(i).getEndOffset());
        }
    }

    @Test
    public void testDownloadPageRangesWithOffsetAndLength() throws StorageException, URISyntaxException, IOException {
        final CloudPageBlob blobRef = setUpPageRanges();
        
        List<PageRange> actualPageRanges = blobRef.downloadPageRanges((long)1*512, (long)5*512);
        List<PageRange> expectedPageRanges = new ArrayList<PageRange>();
        expectedPageRanges.add(new PageRange(2 * 512, 5 * 512 - 1));

        assertEquals(expectedPageRanges.size(), actualPageRanges.size());
        for (int i = 0; i < expectedPageRanges.size(); i++) {
            assertEquals(expectedPageRanges.get(i).getStartOffset(), actualPageRanges.get(i).getStartOffset());
            assertEquals(expectedPageRanges.get(i).getEndOffset(), actualPageRanges.get(i).getEndOffset());
        }
    }
    
    @Test
    public void testDownloadPageRangeDiff() throws StorageException, URISyntaxException, IOException {
        final CloudPageBlob blobRef = setUpPageRanges();
        final CloudPageBlob snapshot = (CloudPageBlob) blobRef.createSnapshot();
        
        // Add page 1
        InputStream inputStream = new ByteArrayInputStream(BlobTestHelper.getRandomBuffer(512));        
        inputStream = new ByteArrayInputStream(BlobTestHelper.getRandomBuffer(512));
        blobRef.uploadPages(inputStream, 0, 512);        
        
        // Clear page 6
        blobRef.clearPages(6 * 512, 512);

        List<PageRangeDiff> actualPageRanges = blobRef.downloadPageRangesDiff(snapshot.getSnapshotID());
        List<PageRangeDiff> expectedPageRanges = new ArrayList<PageRangeDiff>();
        expectedPageRanges.add(new PageRangeDiff(0, 512 - 1, false));
        expectedPageRanges.add(new PageRangeDiff(6 * 512, 7 * 512 - 1, true));

        assertEquals(expectedPageRanges.size(), actualPageRanges.size());
        for (int i = 0; i < expectedPageRanges.size(); i++) {
            assertEquals(expectedPageRanges.get(i).getStartOffset(), actualPageRanges.get(i).getStartOffset());
            assertEquals(expectedPageRanges.get(i).getEndOffset(), actualPageRanges.get(i).getEndOffset());
            assertEquals(expectedPageRanges.get(i).isCleared(), actualPageRanges.get(i).isCleared());
        }
    }
    
    @Test
    public void testDownloadPageRangeDiffWithOffsetAndLength() throws StorageException, URISyntaxException, IOException {
        final CloudPageBlob blobRef = setUpPageRanges();
        final CloudPageBlob snapshot = (CloudPageBlob) blobRef.createSnapshot();
        
        // Add page 1
        InputStream inputStream = new ByteArrayInputStream(BlobTestHelper.getRandomBuffer(512));
        blobRef.uploadPages(inputStream, 0, 512);

        List<PageRangeDiff> actualPageRanges = blobRef.downloadPageRangesDiff(snapshot.getSnapshotID(), (long) 0,
                (long) 5 * 512, null, null, null);
        
        List<PageRangeDiff> expectedPageRanges = new ArrayList<PageRangeDiff>();
        expectedPageRanges.add(new PageRangeDiff(0, 512 - 1, false));

        assertEquals(expectedPageRanges.size(), actualPageRanges.size());
        for (int i = 0; i < expectedPageRanges.size(); i++) {
            assertEquals(expectedPageRanges.get(i).getStartOffset(), actualPageRanges.get(i).getStartOffset());
            assertEquals(expectedPageRanges.get(i).getEndOffset(), actualPageRanges.get(i).getEndOffset());
            assertEquals(expectedPageRanges.get(i).isCleared(), actualPageRanges.get(i).isCleared());
        }
    }
    
    @Test
    public void testUploadDownloadBlobProperties() throws URISyntaxException, StorageException, IOException {
        final int length = 512;

        // do this to make sure the set MD5 can be compared without an exception being thrown
        BlobRequestOptions options = new BlobRequestOptions();
        options.setDisableContentMD5Validation(true);

        // with explicit upload/download of properties 
        String pageBlobName1 = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        CloudPageBlob pageBlobRef1 = this.container.getPageBlobReference(pageBlobName1);

        pageBlobRef1.upload(BlobTestHelper.getRandomDataStream(length), length);

        BlobTestHelper.setBlobProperties(pageBlobRef1);
        BlobProperties props1 = pageBlobRef1.getProperties();
        pageBlobRef1.uploadProperties();

        pageBlobRef1.downloadAttributes(null, options, null);
        BlobProperties props2 = pageBlobRef1.getProperties();

        BlobTestHelper.assertAreEqual(props1, props2);

        // by uploading/downloading the blob   
        pageBlobName1 = BlobTestHelper.generateRandomBlobNameWithPrefix("testBlockBlob");
        pageBlobRef1 = this.container.getPageBlobReference(pageBlobName1);

        BlobTestHelper.setBlobProperties(pageBlobRef1);
        props1 = pageBlobRef1.getProperties();

        pageBlobRef1.upload(BlobTestHelper.getRandomDataStream(length), length);

        pageBlobRef1.download(new ByteArrayOutputStream(), null, options, null);
        props2 = pageBlobRef1.getProperties();

        BlobTestHelper.assertAreEqual(props1, props2);
    }

    @Test
    public void testOpenOutputStreamNotAligned() throws StorageException, URISyntaxException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(8 * 512);

        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blobRef = this.container.getPageBlobReference(blobName);
        blobRef.create(blobLengthToUse);

        try {
            blobRef.openWriteNew(blobLengthToUse + 1);
            fail("Did not throw expected exception on non-512-byte-aligned offset");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_PAGE_BLOB_LENGTH, ex.getMessage());
        }

        BlobOutputStream blobOutputStream = blobRef.openWriteNew(blobLengthToUse);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        try {
            blobOutputStream.write(inputStream, 511);
            blobOutputStream.close();
            fail("Did not throw expected exception on non-512-byte-aligned length");
        }
        catch (IOException ex) {
            assertEquals(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER, 511), ex.getMessage());
        }
    }

    @Test
    public void testOpenOutputStream() throws URISyntaxException, StorageException, IOException {
        int blobLengthToUse = 8 * 512;
        byte[] buffer = BlobTestHelper.getRandomBuffer(8 * 512);

        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        final CloudPageBlob blobRef = this.container.getPageBlobReference(blobName);
        blobRef.create(blobLengthToUse);

        BlobOutputStream blobOutputStream = blobRef.openWriteNew(blobLengthToUse);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        blobOutputStream = blobRef.openWriteNew(blobLengthToUse);
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
    public void testOpenOutputStreamNoArgs() throws URISyntaxException, StorageException {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("testblob");
        CloudPageBlob pageBlob = this.container.getPageBlobReference(blobName);

        try {
            pageBlob.openWriteExisting();
        }
        catch (StorageException ex) {
            assertEquals("The specified blob does not exist.", ex.getMessage());
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
        }

        pageBlob.openWriteNew(1024);
        pageBlob.openWriteExisting();

        CloudPageBlob pageBlob2 = this.container.getPageBlobReference(blobName);
        pageBlob2.downloadAttributes();
        assertEquals(1024, pageBlob2.getProperties().getLength());
        assertEquals(BlobType.PAGE_BLOB, pageBlob2.getProperties().getBlobType());
    }
}
