// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.test.utils.TestUtils;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.DataLakeFileOutputStreamOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FileOutputStreamTests extends DataLakeTestBase {
    private DataLakeFileClient fc;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
    }

    // Only run this test in live mode since blocks are dynamically assigned
    @EnabledIf("com.azure.storage.file.datalake.DataLakeTestBase#isLiveMode")
    @Test
    public void uploadDownload() throws IOException {
        byte[] randomBytes = getRandomByteArray(6 * Constants.MB);

        try (OutputStream outStream = fc.getOutputStream()) {
            outStream.write(randomBytes, Constants.MB, 5 * Constants.MB);
        }

        try (InputStream inputStream = fc.openInputStream().getInputStream()) {
            TestUtils.assertArraysEqual(randomBytes, Constants.MB,
                convertInputStreamToByteArray(inputStream, 5 * Constants.MB), 0, 5 * Constants.MB);
        }
    }

    // Only run this test in live mode since blocks are dynamically assigned
    @EnabledIf("com.azure.storage.file.datalake.DataLakeTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("uploadDownloadBlockSizeSupplier")
    public void uploadDownloadBlockSize(Integer blockSize, int numChunks, int[] sizes) throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);

        try (OutputStream outStream = fc.getOutputStream()) {
            outStream.write(randomBytes);
        }

        try (InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(blockSize))
            .getInputStream()) {
            int b;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(length);

            for (int i = 0; i < numChunks; i++) {
                // Read the first byte of the chunk to force the internal buffer to be filled from DataLake.
                b = inputStream.read();
                assertNotEquals(-1, b);
                outputStream.write(b);
                // Make sure the internal buffer is the expected chunk size.
                assertEquals(sizes[i] - 1, inputStream.available());
                // Read the rest of the chunk
                int remainingChunk = sizes[i] - 1;
                byte[] buffer = new byte[remainingChunk];
                while ((b = inputStream.read(buffer, 0, remainingChunk)) != -1 && remainingChunk > 0) {
                    outputStream.write(buffer, 0, b);
                    remainingChunk -= b;
                }
            }

            assertEquals(-1, inputStream.read()); // Make sure we are at the end of the stream.
            TestUtils.assertArraysEqual(randomBytes, outputStream.toByteArray());
        }
    }

    private static Stream<Arguments> uploadDownloadBlockSizeSupplier() {
        return Stream.of(
            // blockSize || numChunks | sizes
            Arguments.of(null, 2, new int[]{4 * Constants.MB, 2 * Constants.MB}), // Default
            Arguments.of(5 * Constants.MB, 2, new int[]{5 * Constants.MB, Constants.MB}), // Greater than default
            Arguments.of(3 * Constants.MB, 2, new int[]{3 * Constants.MB, 3 * Constants.MB}) // Smaller than default
        );
    }

    @EnabledIf("com.azure.storage.file.datalake.DataLakeTestBase#isLiveMode")
    @Test
    public void outputStreamWithCloseMultipleTimes() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);

        // set option for allowing multiple close() calls
        OutputStream outputStream = fc.getOutputStream();

        outputStream.write(data);
        outputStream.close();
        String etag = fc.getProperties().getETag();

        assertEquals(etag, fc.getProperties().getETag());
        // call again, no exceptions should be thrown
        outputStream.close();
        assertEquals(etag, fc.getProperties().getETag());
        outputStream.close();
        assertEquals(etag, fc.getProperties().getETag());

        assertEquals(data.length, fc.getProperties().getFileSize());
        TestUtils.assertArraysEqual(data, convertInputStreamToByteArray(fc.openInputStream().getInputStream(), data.length));
    }

    @EnabledIf("com.azure.storage.file.datalake.DataLakeTestBase#isLiveMode")
    @Test
    public void outputStreamDefaultNoOverwrite() throws IOException {
        byte[] data = getRandomByteArray(Constants.KB);
        DataLakeFileOutputStreamOptions options = new DataLakeFileOutputStreamOptions().setRequestConditions(
            new DataLakeRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD));

        // Write the file.
        try (OutputStream outputStream = fc.getOutputStream(options)) {
            outputStream.write(data);
        }

        // Attempt to overwrite the file without HTTP precondition permissions.
        try {
            try (OutputStream outputStream = fc.getOutputStream(options)) {
                outputStream.write(data);
            }

            fail("Expected second open write to fail due to precondition.");
        } catch (IOException ex) {
            BlobStorageException e = assertInstanceOf(BlobStorageException.class, ex.getCause());
            assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, e.getErrorCode());
        }
    }

    @EnabledIf("com.azure.storage.file.datalake.DataLakeTestBase#isLiveMode")
    @Test
    public void outputStreamBufferReuse() throws IOException {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        byte[] data = getRandomByteArray(10 * Constants.KB);
        InputStream inputStream = new ByteArrayInputStream(data);
        byte[] buffer = new byte[1024];

        try (OutputStream outputStream = fc.getOutputStream()) {
            int b;
            while ((b = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        }

        assertEquals(data.length, fc.getProperties().getFileSize());
        TestUtils.assertArraysEqual(data, convertInputStreamToByteArray(fc.openInputStream().getInputStream(), data.length));
    }
}
