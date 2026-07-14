// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.BinaryData;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.datalake.models.DataLakeFileOpenInputStreamResult;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.ReadToFileOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that {@code enableDataLocality} on {@link ReadToFileOptions} and {@link DataLakeFileInputStreamOptions}
 * exercises the locality-aware chunk-download wiring end-to-end. {@code DataLakeFileClient} delegates
 * {@code readToFileWithResponse}/{@code openInputStream} to the wrapped {@code BlockBlobClient}, so no independent
 * chunking/routing logic exists on the Data Lake side to test directly -- these tests confirm the flag is forwarded
 * correctly and that downloaded/read content is unaffected, mirroring {@code BlobDataLocalityDownloadApiTests} in
 * azure-storage-blob.
 */
public class FileDataLocalityDownloadTests extends DataLakeTestBase {
    private DataLakeFileClient fc;
    private byte[] contentBytes;
    private Path testFile;

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
        contentBytes = new byte[16 * Constants.KB];
        for (int i = 0; i < contentBytes.length; i++) {
            contentBytes[i] = (byte) (i % 256);
        }
        fc.append(BinaryData.fromBytes(contentBytes), 0);
        fc.flush(contentBytes.length, true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (testFile != null) {
            Files.deleteIfExists(testFile);
        }
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void readToFileWithDataLocalityEnabledSingleChunk() throws IOException {
        testFile = Files.createTempFile(generatePathName(), ".dat");
        Files.deleteIfExists(testFile);

        ReadToFileOptions options = new ReadToFileOptions(testFile.toString()).setEnableDataLocality(true);

        assertDoesNotThrow(() -> fc.readToFileWithResponse(options, null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void readToFileWithDataLocalityEnabledMultipleChunks() throws IOException {
        testFile = Files.createTempFile(generatePathName(), ".dat");
        Files.deleteIfExists(testFile);

        // Force a small block size so the download spans several chunks, exercising the per-chunk
        // layout-cache-resolution wrapper inherited from BlockBlobClient (chunk 0 is a no-op passthrough; chunks 1+
        // go through the locality-aware download function).
        ReadToFileOptions options = new ReadToFileOptions(testFile.toString()).setEnableDataLocality(true)
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong((long) (2 * Constants.KB)));

        assertDoesNotThrow(() -> fc.readToFileWithResponse(options, null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void readToFileWithDataLocalityDisabledIsUnaffected() throws IOException {
        testFile = Files.createTempFile(generatePathName(), ".dat");
        Files.deleteIfExists(testFile);

        // Default (enableDataLocality unset / false) behavior must be identical to before this feature existed.
        assertDoesNotThrow(() -> fc.readToFileWithResponse(new ReadToFileOptions(testFile.toString()), null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void readToFileWithDataLocalityEnabledReturnsProperties() throws IOException {
        testFile = Files.createTempFile(generatePathName(), ".dat");
        Files.deleteIfExists(testFile);

        ReadToFileOptions options = new ReadToFileOptions(testFile.toString()).setEnableDataLocality(true);
        PathProperties properties = fc.readToFileWithResponse(options, null, null).getValue();

        assertEquals(contentBytes.length, properties.getFileSize());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void openInputStreamWithDataLocalityEnabled() throws IOException {
        DataLakeFileInputStreamOptions options = new DataLakeFileInputStreamOptions().setEnableDataLocality(true)
            .setBlockSize(2 * Constants.KB)
            .setRange(new FileRange(0, (long) contentBytes.length));

        byte[] readBytes;
        DataLakeFileOpenInputStreamResult result = fc.openInputStream(options);
        try (InputStream is = result.getInputStream()) {
            readBytes = readAll(is);
        }

        assertArrayEquals(contentBytes, readBytes);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2027-03-07")
    @Test
    public void openInputStreamWithDataLocalityEnabledPartialRange() throws IOException {
        DataLakeFileInputStreamOptions options = new DataLakeFileInputStreamOptions().setEnableDataLocality(true)
            .setBlockSize(2 * Constants.KB)
            .setRange(new FileRange(Constants.KB, (long) (4 * Constants.KB)));

        byte[] readBytes;
        DataLakeFileOpenInputStreamResult result = fc.openInputStream(options);
        try (InputStream is = result.getInputStream()) {
            readBytes = readAll(is);
        }

        byte[] expected = new byte[4 * Constants.KB];
        System.arraycopy(contentBytes, Constants.KB, expected, 0, expected.length);
        assertArrayEquals(expected, readBytes);
    }

    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
