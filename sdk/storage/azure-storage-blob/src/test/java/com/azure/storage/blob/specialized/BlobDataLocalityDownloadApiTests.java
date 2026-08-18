// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the default locality-aware chunk-download wiring works end-to-end (layout cache construction,
 * per-chunk endpoint resolution, and {@code Context} propagation to {@code DataLocalityPolicy}) without altering the
 * bytes returned.
 * <p>
 * Most test accounts will not return an {@code x-ms-download-hint: Layout} header, in which case the wiring is a
 * documented no-op &mdash; these tests therefore primarily assert data integrity while still exercising the new code
 * paths for real, including across multiple chunks.
 */
public class BlobDataLocalityDownloadApiTests extends BlobTestBase {
    private BlobClient bc;
    private byte[] contentBytes;
    private Path testFile;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
        contentBytes = new byte[16 * Constants.KB];
        for (int i = 0; i < contentBytes.length; i++) {
            contentBytes[i] = (byte) (i % 256);
        }
        bc.getBlockBlobClient().upload(new java.io.ByteArrayInputStream(contentBytes), contentBytes.length, true);
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (testFile != null) {
            Files.deleteIfExists(testFile);
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void downloadToFileWithDefaultDataLocalitySingleChunk() throws IOException {
        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        assertDoesNotThrow(
            () -> bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString()), null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void downloadToFileWithDefaultDataLocalityMultipleChunks() throws IOException {
        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        // Force a small block size so the download spans several chunks, exercising the per-chunk
        // layout-cache-resolution wrapper (chunk 0 is a no-op passthrough; chunks 1+ go through the
        // locality-aware download function).
        assertDoesNotThrow(() -> bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong((long) (2 * Constants.KB))),
            null, null));

        assertArrayEquals(contentBytes, Files.readAllBytes(testFile));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void openInputStreamWithDefaultDataLocality() throws IOException {
        BlobInputStreamOptions options = new BlobInputStreamOptions().setBlockSize(2 * Constants.KB)
            .setRange(new BlobRange(0, (long) contentBytes.length));

        byte[] readBytes;
        try (InputStream is = bc.openInputStream(options, null)) {
            readBytes = readAll(is);
        }

        assertArrayEquals(contentBytes, readBytes);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void openInputStreamWithDefaultDataLocalityPartialRange() throws IOException {
        BlobInputStreamOptions options = new BlobInputStreamOptions().setBlockSize(2 * Constants.KB)
            .setRange(new BlobRange(Constants.KB, (long) (4 * Constants.KB)));

        byte[] readBytes;
        try (InputStream is = bc.openInputStream(options, null)) {
            readBytes = readAll(is);
        }

        byte[] expected = new byte[4 * Constants.KB];
        System.arraycopy(contentBytes, Constants.KB, expected, 0, expected.length);
        assertArrayEquals(expected, readBytes);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void downloadToFileWithDefaultDataLocalityReturnsProperties() throws IOException {
        testFile = Files.createTempFile(generateBlobName(), ".dat");
        Files.deleteIfExists(testFile);

        BlobProperties properties
            = bc.downloadToFileWithResponse(new BlobDownloadToFileOptions(testFile.toString()), null, null).getValue();

        assertEquals(contentBytes.length, properties.getBlobSize());
    }

    private static byte[] readAll(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
