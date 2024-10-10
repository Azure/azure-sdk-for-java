// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.utils.TestUtils;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.ConsistentReadControl;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockBlobInputOutputStreamTests extends BlobTestBase {
    private BlockBlobClient bc;

    @BeforeEach
    public void setup() {
        bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
    }

    @ParameterizedTest
    @MethodSource("blobInputStreamReadToLargeBufferSupplier")
    public void blobInputStreamReadToLargeBuffer(int dataSize, int retVal) throws IOException {
        byte[] data = getRandomByteArray(dataSize);
        bc.upload(new ByteArrayInputStream(data), data.length, true);
        BlobInputStream is = bc.openInputStream();
        byte[] outArr = new byte[10 * 1024 * 1024];
        byte[] emptyData = new byte[outArr.length - dataSize];
        int count = is.read(outArr);

        TestUtils.assertArraysEqual(data, 0, outArr, 0, dataSize);
        TestUtils.assertArraysEqual(emptyData, 0, outArr, dataSize, emptyData.length);
        assertEquals(count, retVal);
    }

    private static Stream<Arguments> blobInputStreamReadToLargeBufferSupplier() {
        return Stream.of(
            Arguments.of(0, -1),
            Arguments.of(6 * 1024 * 1024, 6 * 1024 * 1024) // Test for GitHub issue #13811
        );
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void uploadDownload() throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);

        BlobOutputStream outStream = bc.getBlobOutputStream(true);
        outStream.write(randomBytes, Constants.MB, 5 * Constants.MB);
        outStream.close();

        BlobInputStream inputStream = bc.openInputStream();

        BlobProperties propertiesAfter = inputStream.getProperties();
        assertEquals(propertiesAfter.getBlobType(), BlobType.BLOCK_BLOB);
        assertEquals(propertiesAfter.getBlobSize(), 5 * Constants.MB);
        TestUtils.assertArraysEqual(randomBytes, Constants.MB, convertInputStreamToByteArray(inputStream), 0, 5 * Constants.MB);
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @ParameterizedTest
    @MethodSource("uploadDownloadBlockSizeSupplier")
    public void uploadDownloadBlockSize(Integer blockSize, int numChunks, int[] sizes) throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);

        BlobOutputStream outStream = bc.getBlobOutputStream(true);
        outStream.write(randomBytes, 0, 6 * Constants.MB);
        outStream.close();

        BlobInputStream inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(blockSize));
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (int i = 0; i < numChunks; i++) {
                b = inputStream.read();
                assertTrue(b != -1);
                outputStream.write(b);
                // Make sure the internal buffer is the expected chunk size.
                assertEquals(inputStream.available(), sizes[i] - 1);
                // Read the rest of the chunk
                for (int j = 0; j < sizes[i] - 1; j++) {
                    b = inputStream.read();
                    assertTrue(b != -1);
                    outputStream.write(b);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        assertEquals(inputStream.read(), -1); // Make sure we are at the end of the stream.
        BlobProperties propertiesAfter = inputStream.getProperties();
        assertEquals(propertiesAfter.getBlobType(), BlobType.BLOCK_BLOB);
        assertEquals(propertiesAfter.getBlobSize(), 6 * Constants.MB);
        byte[] randomBytes2 = outputStream.toByteArray();
        TestUtils.assertArraysEqual(randomBytes2, randomBytes);
    }

    private static Stream<Arguments> uploadDownloadBlockSizeSupplier() {
        return Stream.of(
            Arguments.of(null, 2, new int[]{4 * Constants.MB, 2 * Constants.MB}), // Default
            Arguments.of(5 * Constants.MB, 2, new int[]{5 * Constants.MB, Constants.MB}), // Greater than default
            Arguments.of(3 * Constants.MB, 2, new int[]{3 * Constants.MB, 3 * Constants.MB}) // Smaller than default
        );
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @ParameterizedTest
    @MethodSource("blobRangeSupplier")
    public void blobRange(Integer start, Long count) throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);

        BlobOutputStream outStream = bc.getBlobOutputStream(true);
        outStream.write(randomBytes, 0, 6 * Constants.MB);
        outStream.close();

        long finalCount = count == null ? length - start : count;
        byte[] resultBytes = new byte[(int) finalCount];
        BlobInputStream inputStream = bc.openInputStream(new BlobInputStreamOptions()
            .setRange(new BlobRange(start, count))
            .setBlockSize(4 * Constants.MB));
        inputStream.read(resultBytes); // read the whole range

        assertEquals(inputStream.read(), -1);
        TestUtils.assertArraysEqual(randomBytes, start, resultBytes, 0, (int) finalCount);
    }

    private static Stream<Arguments> blobRangeSupplier() {
        return Stream.of(
            Arguments.of(0, null), // full blob
            Arguments.of(0, 100L), // Small range
            Arguments.of(0, 4L * Constants.MB), // block size
            Arguments.of(0, 5L * Constants.MB), // Requires multiple chunks
            Arguments.of(0, (Constants.KB) + 1L), // Range not a multiple of 1024
            Arguments.of(0, (Constants.KB) - 1L), // ""
            Arguments.of(5, 100L), // small offset
            Arguments.of(5, null), // full blob after an offset
            Arguments.of(Constants.MB, 2L * Constants.MB), // larger offset inside first chunk
            Arguments.of(Constants.KB, 4L * Constants.MB), // offset with range spanning chunks
            Arguments.of(5 * Constants.MB, (long) Constants.KB), // Range entirely in second chunk
            Arguments.of(5 * Constants.MB, (Constants.KB) + 1L), // Range not multiple of 1024
            Arguments.of(5 * Constants.MB, (Constants.KB) - 1L), // ""
            Arguments.of(5 * Constants.MB, null) // rest of blob after first chunk
            );
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void getPropertiesBefore() throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);

        BlobOutputStream outStream = bc.getBlobOutputStream(true);
        outStream.write(randomBytes, Constants.MB, 5 * Constants.MB);
        outStream.close();

        BlobInputStream inputStream = bc.openInputStream();
        BlobProperties propertiesBefore = inputStream.getProperties();
        assertEquals(propertiesBefore.getBlobType(), BlobType.BLOCK_BLOB);
        assertEquals(propertiesBefore.getBlobSize(), 5 * Constants.MB);
        TestUtils.assertArraysEqual(randomBytes, Constants.MB, convertInputStreamToByteArray(inputStream), 0, 5 * Constants.MB);
    }

    @Test
    public void inputStreamETagLockDefault() throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);
        bc.upload(new ByteArrayInputStream(randomBytes), length, true);

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        BlobInputStream inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(1));
        inputStream.read();

        // Modify the blob again.
        bc.upload(new ByteArrayInputStream(randomBytes), length, true);

        // when: "Reading after eTag has been changed"
        assertThrows(IOException.class, inputStream::read);
    }

    @Test
    public void inputStreamConsistentReadControlNone() throws IOException {
        int length = 6 * Constants.MB;
        byte[] randomBytes = getRandomByteArray(length);
        bc.upload(new ByteArrayInputStream(randomBytes), length, true);

        // Create the input stream and read from it.
        // Note: Setting block size to 1 is inefficient but helps demonstrate the purpose of this test.
        BlobInputStream inputStream = bc.openInputStream(new BlobInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.NONE));
        inputStream.read();

        // Modify the blob again.
        bc.upload(new ByteArrayInputStream(randomBytes), length, true);

        // then: "Exception should not be thrown even though blob was modified"
        assertDoesNotThrow(() -> inputStream.read());
    }

    @Test
    public void inputStreamConsistentReadControlETagClientChoosesETag() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);

        // No eTag specified - client will lock on latest one.
        BlobInputStream inputStream = blobClient.openInputStream(new BlobInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.ETAG));

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @Test
    public void inputStreamConsistentReadControlETagUserProvidesETag() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        BlobInputStream inputStream = blobClient.openInputStream(new BlobInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())));

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @Test
    public void inputStreamConsistentReadControlETagUserProvidesVersionAndETag() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        // User provides version client
        BlobInputStream inputStream = blobClient.getVersionClient(properties.getVersionId())
            .openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.ETAG)
            // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())));

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void inputStreamConsistentReadControlETagUserProvidesVersionClientChoosesETag() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        // User provides version client
        BlobInputStream inputStream = blobClient.getVersionClient(properties.getVersionId())
            .openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.ETAG));
        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    // Error case
    @Test
    public void inputStreamConsistentReadControlETagUserProvidesOldETag() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        BlobInputStream inputStream = blobClient.openInputStream(new BlobInputStreamOptions().setBlockSize(1)
            .setConsistentReadControl(ConsistentReadControl.ETAG)
            // Set the block size to be small enough to not retrieve the whole blob on initial download
            .setBlockSize(500)
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())));

        // Since eTag is the only form of consistentReadControl and the blob is modified, we will throw.
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);

        // BlobStorageException = ConditionNotMet
        assertThrows(IOException.class,
            () -> inputStream.read(new byte[600])); // Read enough to exceed the initial download
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void inputStreamConsistentReadControlVersionClientChoosesVersion() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);

        // No version specified - client will lock on it.
        BlobInputStream inputStream = blobClient.openInputStream(new BlobInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.VERSION_ID));

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true);

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void inputStreamConsistentReadControlVersionUserProvidesVersion() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true);

        // User provides version client
        BlobInputStream inputStream = blobClient.getVersionClient(properties.getVersionId())
            .openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.VERSION_ID));

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true);

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void inputStreamConsistentReadControlVersionUserProvidesVersionAndETag() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        // User provides version client
        BlobInputStream inputStream = blobClient.getVersionClient(properties.getVersionId())
            .openInputStream(new BlobInputStreamOptions().setConsistentReadControl(ConsistentReadControl.VERSION_ID)
            // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())));

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(getRandomByteArray(length)), length, true);

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void inputStreamConsistentReadControlVersionUserProvidesETagClientChoosesVersion() {
        int length = Constants.KB;
        byte[] randomBytes = getRandomByteArray(length);
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);
        BlobProperties properties = blobClient.getProperties();

        // No version specified - client will lock on it.
        BlobInputStream inputStream = blobClient.openInputStream(new BlobInputStreamOptions()
            .setConsistentReadControl(ConsistentReadControl.VERSION_ID)
            // User provides eTag to use
            .setRequestConditions(new BlobRequestConditions().setIfMatch(properties.getETag())));

        // When a versioned client is used it should still succeed if the blob has been modified
        blobClient.upload(new ByteArrayInputStream(randomBytes), length, true);

        TestUtils.assertArraysEqual(randomBytes, convertInputStreamToByteArray(inputStream));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("inputStreamConsistentReadControlValidStatesSupplier")
    public void inputStreamConsistentReadControlValidStates(boolean useETag, boolean useVersionId,
        ConsistentReadControl consistentReadControl) {
        BlobContainerClient blobContainerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());
        blobClient.upload(new ByteArrayInputStream(new byte[0]), 0, true);
        BlobProperties properties = blobClient.getProperties();
        BlobRequestConditions requestConditions = useETag
            ? new BlobRequestConditions().setIfMatch(properties.getETag()) : null;

        if (useVersionId) {
            assertDoesNotThrow(() -> blobClient.getVersionClient(properties.getVersionId())
                .openInputStream(new BlobInputStreamOptions().setConsistentReadControl(consistentReadControl)
                    .setRequestConditions(requestConditions)));
        } else {
            assertDoesNotThrow(() -> blobClient.openInputStream(new BlobInputStreamOptions()
                .setConsistentReadControl(consistentReadControl).setRequestConditions(requestConditions)));
        }
    }

    private static Stream<Arguments> inputStreamConsistentReadControlValidStatesSupplier() {
        return Stream.of(
            Arguments.of(true, false, ConsistentReadControl.NONE),
            Arguments.of(false, true, ConsistentReadControl.NONE),
            Arguments.of(true, false, ConsistentReadControl.VERSION_ID),
            Arguments.of(false, true, ConsistentReadControl.ETAG),
            Arguments.of(true, true, ConsistentReadControl.VERSION_ID),
            Arguments.of(true, true, ConsistentReadControl.ETAG)
        );
    }
}
