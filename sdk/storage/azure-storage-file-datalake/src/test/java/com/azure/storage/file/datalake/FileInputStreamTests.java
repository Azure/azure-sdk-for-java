// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.test.utils.TestUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.models.ConsistentReadControl;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class FileInputStreamTests extends DataLakeTestBase {
    private DataLakeFileClient fc;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
    }

    @ParameterizedTest
    @MethodSource("readInputStreamSupplier")
    public void readInputStream(int length, Integer blockSize) throws IOException {
        byte[] randomBytes = getRandomByteArray(length);
        fc.upload(new ByteArrayInputStream(randomBytes), length, true);

        try (InputStream is = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(blockSize))
            .getInputStream()) {
            TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(is, length));
        }
    }

    private static Stream<Arguments> readInputStreamSupplier() {
        return Stream.of(
            // length | blockSize
            Arguments.of(Constants.KB, null),
            Arguments.of(4 * Constants.KB, Constants.KB),
            Arguments.of(4 * Constants.KB + 5, Constants.KB)
        );
    }

    // can't port zero-size test from blobs; datalake doesn't support empty append calls
    // Test for GitHub issue #13811
    @Test
    public void blobInputStreamReadToLargeBuffer() throws IOException {
        byte[] data = getRandomByteArray(6 * Constants.MB);
        fc.upload(new ByteArrayInputStream(data), data.length, true);

        try (InputStream is = fc.openInputStream().getInputStream()) {
            byte[] outArr = new byte[10 * 1024 * 1024];
            int count = is.read(outArr);

            assertEquals(data.length, count);
            TestUtils.assertArraysEqual(data, 0, outArr, 0, data.length);
            TestUtils.assertArraysEqual(new byte[4 * 1024 * 1024], 0, outArr, data.length, 4 * 1024 * 1024);
        }
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @EnabledIf("com.azure.storage.file.datalake.DataLakeTestBase#isLiveMode")
    @ParameterizedTest
    @MethodSource("uploadDownloadBlockSizeSupplier")
    public void uploadDownloadBlockSize(Integer blockSize, int numChunks, int[] sizes) throws IOException {
        byte[] randomBytes = getRandomByteArray(6 * Constants.MB);
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

        try (InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(blockSize))
            .getInputStream()) {
            int b;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(randomBytes.length);

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void inputStreamETagLockDefault() throws IOException {
        byte[] randomBytes = getRandomByteArray(6 * Constants.MB);
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        try (InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(1))
                .getInputStream()) {
            inputStream.read();

            // Modify the blob again.
            fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

            // Reading after etag has been changed
            assertThrows(IOException.class, inputStream::read);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void inputStreamConsistentReadControlNone() throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        try (InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.NONE)).getInputStream()) {
            inputStream.read();

            // Modify the blob again.
            fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

            // Exception should not be thrown even though blob was modified
            assertDoesNotThrow(() -> inputStream.read());
        }
    }

    @Test
    public void inputStreamConsistentReadControlETagClientChoosesETag() throws IOException {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

        // No eTag specified - client will lock on latest one.
        try (InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.ETAG)).getInputStream()) {
            TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream, randomBytes.length));
        }
    }

    @Test
    public void inputStreamConsistentReadControlETagUserProvidesETag() throws IOException {
        byte[] randomBytes = getRandomByteArray(Constants.KB);
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);
        PathProperties properties = fc.getProperties();

        try (InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            // User provides eTag to use
            .setRequestConditions(new DataLakeRequestConditions().setIfMatch(properties.getETag())))
            .getInputStream()) {
            TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream, randomBytes.length));
        }
    }

    // Error case
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void inputStreamConsistentReadControlETagUserProvidesOldEtag() {
        byte[] randomBytes = getRandomByteArray(Constants.KB);
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);
        PathProperties properties = fc.getProperties();

        InputStream inputStream = fc.openInputStream(new DataLakeFileInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            .setRequestConditions(new DataLakeRequestConditions().setIfMatch(properties.getETag())))
            .getInputStream();

        // Since eTag is the only form of consistentReadControl and the blob is modified, we will throw.
        fc.upload(new ByteArrayInputStream(randomBytes), randomBytes.length, true);

        try {
            inputStream.read(); // initial block
            inputStream.read(); // trigger another download
            fail("Expected exception as the ETag should have changed");
        } catch (Exception ex) {
            assertInstanceOf(IOException.class, ex);
        }
    }
}
