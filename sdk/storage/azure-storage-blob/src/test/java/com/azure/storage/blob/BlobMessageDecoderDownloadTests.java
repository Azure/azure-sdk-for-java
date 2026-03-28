// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageChecksumAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Sync tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class BlobMessageDecoderDownloadTests extends BlobTestBase {

    private BlobAsyncClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null).block();
    }

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidationSync() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobClient syncClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        syncClient.downloadStreamWithResponse(outputStream,
            new BlobDownloadStreamOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64), null,
            Context.NONE);

        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidationSync() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobClient syncClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        byte[] result
            = syncClient
                .downloadContentWithResponse(
                    new BlobDownloadContentOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64), null,
                    Context.NONE)
                .getValue()
                .toBytes();

        TestUtils.assertArraysEqual(data, result);
    }

    /**
     * downloadToFileWithResponse with CRC64 content validation (parallel, multiple block sizes).
     */
    @ParameterizedTest
    @ValueSource(ints = { 512, 2048 })
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void downloadToFileWithResponseContentValidationSync(int blockSize) throws IOException {
        int payloadSize = (4 * blockSize) + 1;
        byte[] randomData = getRandomByteArray(payloadSize);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        Path tempFile = Files.createTempFile("structured-download-sync", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize)
            .setInitialTransferSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        try {
            assertNotNull(downloadClient.downloadToFileWithResponse(options, null, Context.NONE).getValue());
            TestUtils.assertArraysEqual(randomData, Files.readAllBytes(tempFile));
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
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobClient syncClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        try (BlobInputStream blobInputStream = syncClient.openInputStream(
            new BlobInputStreamOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64), Context.NONE)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = blobInputStream.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            TestUtils.assertArraysEqual(data, baos.toByteArray());
        }
    }

    /**
     *  openSeekableByteChannelRead with CRC64 content validation.
     */
    @Test
    public void openSeekableByteChannelReadContentValidation() throws IOException {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobClient syncClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        try (SeekableByteChannel channel = syncClient.openSeekableByteChannelRead(
            new BlobSeekableByteChannelReadOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64),
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
    }
}
