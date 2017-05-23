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
package com.microsoft.azure.storage.file;


import static org.junit.Assert.*;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.TestHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.UUID;

/**
 * File Test Base
 */
public class FileTestHelper extends TestHelper {

    protected static String generateRandomShareName() {
        String shareName = "share" + UUID.randomUUID().toString();
        return shareName.replace("-", "");
    }

    public static String generateRandomFileName() {
        String shareName = "file" + UUID.randomUUID().toString();
        return shareName.replace("-", "");
    }
    
    public static CloudFile uploadNewFile(CloudFileShare share, int length, OperationContext context)
            throws StorageException, IOException, URISyntaxException {
        
        return uploadNewFile(share, getRandomDataStream(length), length, context);
    }
    
    public static CloudFile uploadNewFile(
            CloudFileShare share, InputStream stream, int length, OperationContext context)
            throws StorageException, IOException, URISyntaxException {
        String name = generateRandomFileName();

        CloudFile file = null;

        file = share.getRootDirectoryReference().getFileReference(name);
        file.upload(stream, length, null, null, context);
        return file;
    }

    public static CloudFileShare getRandomShareReference() throws URISyntaxException, StorageException {
        String shareName = generateRandomShareName();
        CloudFileClient fileClient = TestHelper.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference(shareName);

        return share;
    }
    
    static StorageUri ensureTrailingSlash(StorageUri uri) throws URISyntaxException {
        URI primary = uri.getPrimaryUri();
        URI secondary = uri.getSecondaryUri();
        
        // Add a trailing slash to primary if it did not previously have one
        if (primary != null) {
            String primaryUri = primary.toString();
            if (!primaryUri.isEmpty() && !primaryUri.substring(primaryUri.length() - 1).equals("/")) {
                primaryUri += "/";
                primary = new URI(primaryUri);
            }
        }

        // Add a trailing slash to secondary if it did not previously have one
        if (secondary != null) {
            String secondaryUri = secondary.toString();
            if (!secondaryUri.isEmpty() &&  !secondaryUri.substring(secondaryUri.length() - 1).equals("/")) {
                secondaryUri += "/";
                secondary = new URI(secondaryUri);
            }
        }
        
        return new StorageUri(primary, secondary);
    }

    protected static void doDownloadTest(CloudFile file, int fileSize, int bufferSize, int bufferOffset)
            throws StorageException, IOException, URISyntaxException {
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[fileSize];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[bufferSize];

        FileRequestOptions options = new FileRequestOptions();
        file.upload(new ByteArrayInputStream(buffer), buffer.length);
        file.downloadToByteArray(resultBuffer, bufferOffset, null, options, null);

        for (int i = 0; i < file.getProperties().getLength(); i++) {
            assertEquals(buffer[i], resultBuffer[bufferOffset + i]);
        }

        if (bufferOffset + fileSize < bufferSize) {
            for (int k = bufferOffset + fileSize; k < bufferSize; k++) {
                assertEquals(0, resultBuffer[k]);
            }
        }
    }

    protected static void doDownloadRangeToByteArrayTest(CloudFile file, int fileSize, int bufferSize,
            int bufferOffset, Long fileOffset, Long length) throws IOException, StorageException, URISyntaxException {
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[fileSize];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[bufferSize];
        int downloadLength;

        FileRequestOptions options = new FileRequestOptions();
        file.upload(new ByteArrayInputStream(buffer), buffer.length);

        downloadLength = file.downloadRangeToByteArray(fileOffset, length, resultBuffer, bufferOffset, null, options,
                null);

        int downloadSize = Math.min(fileSize - (int) (fileOffset != null ? fileOffset : 0), bufferSize - bufferOffset);
        if (length != null && length < downloadSize) {
            downloadSize = length.intValue();
        }

        assertEquals(downloadSize, downloadLength);

        for (int i = 0; i < bufferOffset; i++) {
            assertEquals(0, resultBuffer[i]);
        }

        for (int j = 0; j < downloadLength; j++) {
            assertEquals(buffer[(int) ((fileOffset != null ? fileOffset : 0) + j)], resultBuffer[bufferOffset
                    + j]);
        }

        for (int k = bufferOffset + downloadLength; k < bufferSize; k++) {
            assertEquals(0, resultBuffer[k]);
        }
    }

    protected static void doDownloadRangeToByteArrayNegativeTests(CloudFile file) throws StorageException, IOException, URISyntaxException {
        int fileLength = 1024;
        int resultBufSize = 1024;
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[fileLength];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[resultBufSize];

        file.upload(new ByteArrayInputStream(buffer), buffer.length);

        try {
            file.downloadRangeToByteArray(1024, (long) 1, resultBuffer, 0);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(416, ex.getHttpStatusCode());
        }

        try {
            file.downloadToByteArray(resultBuffer, 1024);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {
        }

        try {
            file.downloadRangeToByteArray(0, (long) 1023, resultBuffer, 2);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative length
        try {
            file.downloadRangeToByteArray(0, (long) -10, resultBuffer, 0);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative file offset
        try {
            file.downloadRangeToByteArray(-10, (long) 20, resultBuffer, 0);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative buffer offset
        try {
            file.downloadRangeToByteArray(0, (long) 20, resultBuffer, -10);
            fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }
    }
    
    public static CloudFile defiddler(CloudFile file) throws URISyntaxException, StorageException {
        URI oldUri = file.getUri();
        URI newUri = defiddler(oldUri);

        if (newUri != oldUri) {
            CloudFile newFile = new CloudFile(newUri, file.getServiceClient().getCredentials());
            return newFile;
        }
        else {
            return file;
        }
    }
    
    public static void waitForCopy(CloudFile file) throws StorageException, InterruptedException {
        boolean copyInProgress = true;
        while (copyInProgress) {
            file.downloadAttributes();
            copyInProgress = (file.getCopyState().getStatus() == CopyStatus.PENDING)
                    || (file.getCopyState().getStatus() == CopyStatus.ABORTED);
            // One second sleep if retry is needed
            if (copyInProgress) {
                Thread.sleep(1000);
            }
        }
    }

    public static void assertAreEqual(CloudFile file1, CloudFile file2) {
        if (file1 == null) {
            assertNull(file2);
        }
        else {
            assertNotNull(file2);
            assertEquals(file1.getUri(), file2.getUri());
            assertAreEqual(file1.getProperties(), file2.getProperties());
        }
    }

    public static void assertAreEqual(FileProperties prop1, FileProperties prop2) {
        if (prop1 == null) {
            assertNull(prop2);
        }
        else {
            assertNotNull(prop2);
            assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
            assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
            assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
            assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
            assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
            assertEquals(prop1.getContentType(), prop2.getContentType());
            assertEquals(prop1.getEtag(), prop2.getEtag());
            assertEquals(prop1.getLastModified(), prop2.getLastModified());
            assertEquals(prop1.getLength(), prop2.getLength());
        }
    }

    public static void setFileProperties(CloudFile file) {
        file.getProperties().setCacheControl("no-transform");
        file.getProperties().setContentDisposition("attachment");
        file.getProperties().setContentEncoding("gzip");
        file.getProperties().setContentLanguage("tr,en");
        file.getProperties().setContentMD5("MDAwMDAwMDA=");
        file.getProperties().setContentType("text/html");
    }
}