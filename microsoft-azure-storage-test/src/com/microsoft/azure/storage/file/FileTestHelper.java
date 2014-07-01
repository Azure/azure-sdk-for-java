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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.UUID;

import junit.framework.Assert;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;

/**
 * File Test Base
 */
public class FileTestHelper extends TestHelper {

    protected static String generateRandomShareName() {
        String shareName = "share" + UUID.randomUUID().toString();
        return shareName.replace("-", "");
    }

    protected static String generateRandomFileName() {
        String shareName = "file" + UUID.randomUUID().toString();
        return shareName.replace("-", "");
    }

    public static CloudFileShare getRandomShareReference() throws URISyntaxException, StorageException {
        String shareName = generateRandomShareName();
        CloudFileClient fileClient = TestHelper.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference(shareName);

        return share;
    }

    protected static void doDownloadTest(CloudFile file, int fileSize, int bufferSize, int bufferOffset)
            throws StorageException, IOException {
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[fileSize];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[bufferSize];

        FileRequestOptions options = new FileRequestOptions();
        file.upload(new ByteArrayInputStream(buffer), buffer.length);
        file.downloadToByteArray(resultBuffer, bufferOffset, null, options, null);

        for (int i = 0; i < file.getProperties().getLength(); i++) {
            Assert.assertEquals(buffer[i], resultBuffer[bufferOffset + i]);
        }

        if (bufferOffset + fileSize < bufferSize) {
            for (int k = bufferOffset + fileSize; k < bufferSize; k++) {
                Assert.assertEquals(0, resultBuffer[k]);
            }
        }
    }

    protected static void doDownloadRangeToByteArrayTest(CloudFile file, int fileSize, int bufferSize,
            int bufferOffset, Long fileOffset, Long length) throws IOException, StorageException {
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

        Assert.assertEquals(downloadSize, downloadLength);

        for (int i = 0; i < bufferOffset; i++) {
            Assert.assertEquals(0, resultBuffer[i]);
        }

        for (int j = 0; j < downloadLength; j++) {
            Assert.assertEquals(buffer[(int) ((fileOffset != null ? fileOffset : 0) + j)], resultBuffer[bufferOffset
                    + j]);
        }

        for (int k = bufferOffset + downloadLength; k < bufferSize; k++) {
            Assert.assertEquals(0, resultBuffer[k]);
        }
    }

    protected static void doDownloadRangeToByteArrayNegativeTests(CloudFile file) throws StorageException, IOException {
        int fileLength = 1024;
        int resultBufSize = 1024;
        final Random randGenerator = new Random();
        final byte[] buffer = new byte[fileLength];
        randGenerator.nextBytes(buffer);
        byte[] resultBuffer = new byte[resultBufSize];

        file.upload(new ByteArrayInputStream(buffer), buffer.length);

        try {
            file.downloadRangeToByteArray(1024, (long) 1, resultBuffer, 0);
            Assert.fail();
        }
        catch (StorageException ex) {
            Assert.assertEquals(416, ex.getHttpStatusCode());
        }

        try {
            file.downloadToByteArray(resultBuffer, 1024);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ex) {
        }

        try {
            file.downloadRangeToByteArray(0, (long) 1023, resultBuffer, 2);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative length
        try {
            file.downloadRangeToByteArray(0, (long) -10, resultBuffer, 0);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative blob offset
        try {
            file.downloadRangeToByteArray(-10, (long) 20, resultBuffer, 0);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }

        // negative buffer offset
        try {
            file.downloadRangeToByteArray(0, (long) 20, resultBuffer, -10);
            Assert.fail();
        }
        catch (IndexOutOfBoundsException ex) {

        }
    }

    public static void assertAreEqual(CloudFile file1, CloudFile file2) {
        if (file1 == null) {
            Assert.assertNull(file2);
        }
        else {
            Assert.assertNotNull(file2);
            Assert.assertEquals(file1.getUri(), file2.getUri());
            assertAreEqual(file1.getProperties(), file2.getProperties());
        }
    }

    public static void assertAreEqual(FileProperties prop1, FileProperties prop2) {
        if (prop1 == null) {
            Assert.assertNull(prop2);
        }
        else {
            Assert.assertNotNull(prop2);
            Assert.assertEquals(prop1.getCacheControl(), prop2.getCacheControl());
            Assert.assertEquals(prop1.getContentDisposition(), prop2.getContentDisposition());
            Assert.assertEquals(prop1.getContentEncoding(), prop2.getContentEncoding());
            Assert.assertEquals(prop1.getContentLanguage(), prop2.getContentLanguage());
            Assert.assertEquals(prop1.getContentMD5(), prop2.getContentMD5());
            Assert.assertEquals(prop1.getContentType(), prop2.getContentType());
            Assert.assertEquals(prop1.getEtag(), prop2.getEtag());
            Assert.assertEquals(prop1.getLastModified(), prop2.getLastModified());
            Assert.assertEquals(prop1.getLength(), prop2.getLength());
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
