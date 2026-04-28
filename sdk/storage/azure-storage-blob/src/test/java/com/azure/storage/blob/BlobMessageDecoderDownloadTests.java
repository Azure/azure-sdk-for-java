// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sync tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class BlobMessageDecoderDownloadTests extends BlobTestBase {

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidation() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        client.downloadStreamWithResponse(outputStream,
            new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null,
            Context.NONE);

        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidation() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        byte[] result
            = client
                .downloadContentWithResponse(
                    new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
                    null, Context.NONE)
                .getValue()
                .toBytes();

        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadToFileWithResponse with CRC64 content validation (parallel, multiple block sizes).
     */
    @ParameterizedTest
    @ValueSource(ints = { 512, 2048 })
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void downloadToFileWithResponseContentValidation(int blockSize) throws IOException {
        int payloadSize = (4 * blockSize) + 1;
        byte[] randomData = getRandomByteArray(payloadSize);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(randomData));

        Path tempFile = Files.createTempFile("structured-download-sync", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        try {
            assertNotNull(client.downloadToFileWithResponse(options, null, Context.NONE).getValue());
            TestUtils.assertArraysEqual(randomData, Files.readAllBytes(tempFile));
            assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * openInputStream with CRC64 content validation.
     */
    @Test
    public void openInputStreamContentValidation() throws IOException {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        try (BlobInputStream blobInputStream = client.openInputStream(
            new BlobInputStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
            Context.NONE)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = blobInputStream.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            TestUtils.assertArraysEqual(data, baos.toByteArray());
        }
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     *  openSeekableByteChannelRead with CRC64 content validation.
     */
    @Test
    public void openSeekableByteChannelReadContentValidation() throws IOException {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        try (SeekableByteChannel channel = client.openSeekableByteChannelRead(
            new BlobSeekableByteChannelReadOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
            Context.NONE).getChannel()) {
            ByteBuffer buf = ByteBuffer.allocate(data.length + 100);
            int totalRead = 0;
            int bytesRead;
            while ((bytesRead = channel.read(buf)) > 0) {
                totalRead += bytesRead;
            }
            buf.flip();
            byte[] result = new byte[totalRead];
            buf.get(result, 0, totalRead);
            TestUtils.assertArraysEqual(data, result);
        }
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }
}
