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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;

/**
 * Blob Test Base
 */
public class BlobTestHelper extends TestHelper {

    public static String generateRandomContainerName() {
        String containerName = "container" + UUID.randomUUID().toString();
        return containerName.replace("-", "");
    }

    public static CloudBlobContainer getRandomContainerReference() throws URISyntaxException, StorageException {
        String containerName = generateRandomContainerName();
        CloudBlobClient bClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = bClient.getContainerReference(containerName);

        return container;
    }

    public static String generateRandomBlobNameWithPrefix(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        String blobName = prefix + UUID.randomUUID().toString();
        return blobName.replace("-", "");
    }
    
    public static CloudBlob getBlobReference(BlobType type, CloudBlobContainer container, String name)
            throws URISyntaxException, StorageException {
        CloudBlob blob = null;
        
        switch (type) {
            case APPEND_BLOB:
                blob = container.getAppendBlobReference(name);
                break;
    
            case BLOCK_BLOB:
                blob = container.getBlockBlobReference(name);
                break;
                
            case PAGE_BLOB:
                blob = container.getPageBlobReference(name);
                break;
                
            case UNSPECIFIED:
                fail();
        }
        
        return blob;
    }
    
    public static CloudBlob getBlobReference(BlobType type, StorageCredentials credentials, URI uri)
            throws StorageException, URISyntaxException {
        CloudBlob blob = null;
        
        switch (type) {
            case APPEND_BLOB:
                blob = new CloudAppendBlob(credentials.transformUri(uri));
                break;
    
            case BLOCK_BLOB:
                blob = new CloudBlockBlob(credentials.transformUri(uri));
                break;
                
            case PAGE_BLOB:
                blob = new CloudPageBlob(credentials.transformUri(uri));
                break;
                
            case UNSPECIFIED:
                fail();
        }
        
        return blob;
    }

    public static List<String> uploadNewBlobs(CloudBlobContainer container, BlobType type, int count, int length,
            OperationContext context) throws StorageException, IOException, URISyntaxException {
        CloudBlob blob = null;
        List<String> blobs = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            switch (type) {
                case BLOCK_BLOB:
                    blob = uploadNewBlob(container, type, "bb", length, context);
                    blobs.add(blob.getName());
                    break;

                case PAGE_BLOB:
                    blob = uploadNewBlob(container, type, "pb", length, context);
                    blobs.add(blob.getName());
                    break;
                    
                case APPEND_BLOB:
                    blob = uploadNewBlob(container, type, "ab", length, context);
                    blobs.add(blob.getName());
                    break;


                default:
                    break;
            }
        }
        return blobs;
    }

    public static CloudBlob uploadNewBlob(CloudBlobContainer container, BlobType type, String prefix, int length,
            OperationContext context) throws StorageException, IOException, URISyntaxException {
        if (prefix == null) {
            prefix = generateRandomBlobNameWithPrefix("");
        }

        String name = generateRandomBlobNameWithPrefix(prefix);

        CloudBlob blob = null;

        switch (type) {
            case BLOCK_BLOB:
                blob = container.getBlockBlobReference(name);
                blob.upload(getRandomDataStream(length), length, null, null, context);
                break;
                
            case PAGE_BLOB:
                blob = container.getPageBlobReference(name);
                if (length == 0) {
                    ((CloudPageBlob) blob).create(length, null, null, context);
                } else {
                    blob.upload(getRandomDataStream(length), length, null, null, context);
                }
                break;
                
            case APPEND_BLOB:
                blob = container.getAppendBlobReference(name);
                if (length == 0) {
                    ((CloudAppendBlob) blob).createOrReplace(null, null, context);
                } else {
                    blob.upload(getRandomDataStream(length), length, null, null, context);
                }
                break;
                    
            default:
                fail();
        }
        
        return blob;
    }

    protected static void doDownloadTest(CloudBlob blob, int blobSize, int bufferSize, int bufferOffset)
            throws StorageException, IOException {
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[blobSize];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[bufferSize];

        BlobRequestOptions options = new BlobRequestOptions();
        if (blob.getClass() == CloudBlockBlob.class) {
            options.setUseTransactionalContentMD5(true);
        }
        blob.upload(new ByteArrayInputStream(buffer), buffer.length);
        blob.downloadToByteArray(resultBuffer, bufferOffset, null, options, null);

        for (int i = 0; i < blob.getProperties().getLength(); i++) {
            assertEquals(buffer[i], resultBuffer[bufferOffset + i]);
        }

        if (bufferOffset + blobSize < bufferSize) {
            for (int k = bufferOffset + blobSize; k < bufferSize; k++) {
                assertEquals(0, resultBuffer[k]);
            }
        }
    }

    protected static void doDownloadRangeToByteArrayTest(CloudBlob blob, int blobSize, int bufferSize,
            int bufferOffset, Long blobOffset, Long length) throws IOException, StorageException {
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[blobSize];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[bufferSize];
        int downloadLength;

        BlobRequestOptions options = new BlobRequestOptions();
        blob.upload(new ByteArrayInputStream(buffer), buffer.length);

        downloadLength = blob.downloadRangeToByteArray(blobOffset, length, resultBuffer, bufferOffset, null, options,
                null);

        int downloadSize = Math.min(blobSize - (int) (blobOffset != null ? blobOffset : 0), bufferSize - bufferOffset);
        if (length != null && length < downloadSize) {
            downloadSize = length.intValue();
        }

        assertEquals(downloadSize, downloadLength);

        for (int i = 0; i < bufferOffset; i++) {
            assertEquals(0, resultBuffer[i]);
        }

        for (int j = 0; j < downloadLength; j++) {
            assertEquals(buffer[(int) ((blobOffset != null ? blobOffset : 0) + j)], resultBuffer[bufferOffset
                    + j]);
        }

        for (int k = bufferOffset + downloadLength; k < bufferSize; k++) {
            assertEquals(0, resultBuffer[k]);
        }
    }

    protected static void doDownloadRangeToByteArrayNegativeTests(CloudBlob blob) throws StorageException, IOException {
        int blobLength = 1024;
        int resultBufSize = 1024;
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[blobLength];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[resultBufSize];

        blob.upload(new ByteArrayInputStream(buffer), buffer.length);

        try {
            blob.downloadRangeToByteArray(1024, (long) 1, resultBuffer, 0);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(416, ex.getHttpStatusCode());
        }

        try {
            blob.downloadToByteArray(resultBuffer, 1024);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {
        }

        try {
            blob.downloadRangeToByteArray(0, (long) 1023, resultBuffer, 2);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative length
        try {
            blob.downloadRangeToByteArray(0, (long) -10, resultBuffer, 0);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative blob offset
        try {
            blob.downloadRangeToByteArray(-10, (long) 20, resultBuffer, 0);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative buffer offset
        try {
            blob.downloadRangeToByteArray(0, (long) 20, resultBuffer, -10);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }
    }
    
    public static CloudAppendBlob defiddler(CloudAppendBlob blob) throws URISyntaxException, StorageException {
        URI oldUri = blob.getUri();
        URI newUri = defiddler(oldUri);

        if (newUri != oldUri) {
            CloudAppendBlob newBlob = new CloudAppendBlob(newUri, blob.getServiceClient().getCredentials());
            newBlob.setSnapshotID(blob.snapshotID);
            return newBlob;
        }
        else {
            return blob;
        }
    }

    public static CloudBlockBlob defiddler(CloudBlockBlob blob) throws URISyntaxException, StorageException {
        URI oldUri = blob.getUri();
        URI newUri = defiddler(oldUri);

        if (newUri != oldUri) {
            CloudBlockBlob newBlob = new CloudBlockBlob(newUri, blob.getServiceClient().getCredentials());
            newBlob.setSnapshotID(blob.snapshotID);
            return newBlob;
        }
        else {
            return blob;
        }
    }

    public static CloudPageBlob defiddler(CloudPageBlob blob) throws URISyntaxException, StorageException {
        URI oldUri = blob.getUri();
        URI newUri = defiddler(oldUri);

        if (newUri != oldUri) {
            CloudPageBlob newBlob = new CloudPageBlob(newUri, blob.getServiceClient().getCredentials());
            newBlob.setSnapshotID(blob.snapshotID);
            return newBlob;
        }
        else {
            return blob;
        }
    }

    public static void waitForCopy(CloudBlob blob) throws StorageException, InterruptedException {
        boolean copyInProgress = true;
        while (copyInProgress) {
            blob.downloadAttributes();
            copyInProgress = (blob.getCopyState().getStatus() == CopyStatus.PENDING)
                    || (blob.getCopyState().getStatus() == CopyStatus.ABORTED);
            // One second sleep if retry is needed
            if (copyInProgress) {
                Thread.sleep(1000);
            }
        }
    }

    public static byte[] getRandomBuffer(int size) {
        byte[] buffer = new byte[size];
        Random random = new Random();
        random.nextBytes(buffer);
        return buffer;
    }

    public static Map<String, BlockEntry> getBlockEntryList(int count) {
        Map<String, BlockEntry> blocks = new HashMap<String, BlockEntry>();
        for (int i = 0; i < count; i++) {
            final String name = generateRandomBlobNameWithPrefix(null);
            blocks.put(name, new BlockEntry(name, BlockSearchMode.LATEST));
        }
        return blocks;
    }

    public static void setBlobProperties(CloudBlob blob) {
        blob.getProperties().setCacheControl("no-transform");
        blob.getProperties().setContentDisposition("attachment");
        blob.getProperties().setContentEncoding("gzip");
        blob.getProperties().setContentLanguage("tr,en");
        blob.getProperties().setContentMD5("MDAwMDAwMDA=");
        blob.getProperties().setContentType("text/html");
    }

    public static void assertAreEqual(CloudBlob blob1, CloudBlob blob2) throws URISyntaxException, StorageException {
        if (blob1 == null) {
            assertNull(blob2);
        }
        else {
            assertNotNull(blob2);
            assertEquals(blob1.getClass(), blob2.getClass());
            assertEquals(blob1.getUri(), blob2.getUri());
            assertEquals(blob1.getSnapshotID(), blob2.getSnapshotID());
            assertEquals(blob1.isSnapshot(), blob2.isSnapshot());
            assertEquals(blob1.getQualifiedStorageUri(), blob2.getQualifiedStorageUri());
            assertAreEqual(blob1.getProperties(), blob2.getProperties());
            assertAreEqual(blob1.getCopyState(), blob2.getCopyState());
        }
    }

    public static void assertAreEqual(BlobProperties prop1, BlobProperties prop2) {
        if (prop1 == null) {
            assertNull(prop2);
        }
        else {
            assertNotNull(prop2);
            assertEquals(prop1.getBlobType(), prop2.getBlobType());
            assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
            assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
            assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
            assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
            assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
            assertEquals(prop1.getContentType(), prop2.getContentType());
            assertEquals(prop1.getEtag(), prop2.getEtag());
            assertEquals(prop1.getLastModified(), prop2.getLastModified());
            assertEquals(prop1.getLength(), prop2.getLength());
            assertEquals(prop1.getPageBlobSequenceNumber(), prop2.getPageBlobSequenceNumber());
        }
    }

    public static void assertAreEqual(CopyState copy1, CopyState copy2) {
        if (copy1 == null) {
            assertNull(copy2);
        }
        else {
            assertNotNull(copy2);
            assertEquals(copy1.getBytesCopied(), copy2.getBytesCopied());
            assertEquals(copy1.getCompletionTime(), copy2.getCompletionTime());
            assertEquals(copy1.getCopyId(), copy2.getCopyId());
            assertEquals(copy1.getSource(), copy2.getSource());
            assertEquals(copy1.getStatus(), copy2.getStatus());
            assertEquals(copy1.getTotalBytes(), copy2.getTotalBytes());
        }
    }
}
