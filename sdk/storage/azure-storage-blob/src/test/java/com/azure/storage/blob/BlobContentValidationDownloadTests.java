// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobSeekableByteChannelReadResult;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static com.azure.storage.blob.specialized.BlobSeekableByteChannelTests.copy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sync tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
public class BlobContentValidationDownloadTests extends BlobTestBase {
    private static final int TEN_MB = 10 * Constants.MB;
    private static final long TWO_HUNDRED_GIB_BYTES = 200L * Constants.GB;
    private final List<File> createdFiles = new ArrayList<>();

    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

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
        byte[] data = getRandomByteArray(TEN_MB);

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
    @ValueSource(
        ints = {
            0, // empty file
            20, // small file
            16 * 1024 * 1024, // medium file in several chunks
            8 * 1026 * 1024 + 10, // medium file not aligned to block
        })
    public void downloadToFileWithResponseContentValidation(int fileSize) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        assertNotNull(client.downloadToFileWithResponse(options, null, Context.NONE).getValue());
        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadToFileWithResponse with CRC64 content validation (parallel, multiple block sizes).
     */
    @LiveOnly
    @ParameterizedTest
    @ValueSource(
        ints = {
            50 * Constants.MB, //large file requiring multiple requests
            50 * Constants.MB + 22 // large file not on MB boundary
        })
    public void downloadToFileLargeWithResponseContentValidation(int fileSize) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        assertNotNull(client.downloadToFileWithResponse(options, null, Context.NONE).getValue());
        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Range download without content validation works correctly.
     */
    @Test
    public void downloadStreamWithResponseContentValidationRange() {
        byte[] randomData = getRandomByteArray(4 * Constants.KB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(randomData));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setRange(new BlobRange(0, 512L));
        client.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertEquals(512, outputStream.toByteArray().length);
        assertFalse(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Default behavior: when no algorithm is specified, default is NONE (no validation).
     */
    @Test
    public void downloadStreamDefaultAlgorithmIsNone() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        client.downloadStreamWithResponse(outputStream, new BlobDownloadStreamOptions(), null, Context.NONE);

        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertFalse(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * AUTO on downloadStream resolves to CRC64 behavior.
     */
    @Test
    public void downloadStreamWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options
            = new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO);
        client.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with NONE: no validation triggered.
     */
    @Test
    public void downloadContentWithNone() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        byte[] result
            = client
                .downloadContentWithResponse(
                    new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.NONE),
                    null, Context.NONE)
                .getValue()
                .toBytes();

        TestUtils.assertArraysEqual(data, result);
        assertFalse(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with AUTO resolves to CRC64 behavior.
     */
    @Test
    public void downloadContentWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        byte[] result
            = client
                .downloadContentWithResponse(
                    new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO),
                    null, Context.NONE)
                .getValue()
                .toBytes();

        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Interrupt with proper rewind to segment boundary; verifies retry range headers.
     */
    @Test
    public void interruptAndVerifyProperRewind() {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(2 * segmentSize);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();

        BlobClient uploadClient = createBlobClientWithRequestSniffer(recorded);
        uploadClient.upload(BinaryData.fromBytes(randomData));

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(1, interruptPos, uploadClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recorded.add(context.getHttpRequest().getHeaders());
            return next.process();
        };

        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            uploadClient.getBlobUrl(), sniffPolicy, mockPolicy);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        TestUtils.assertArraysEqual(randomData, outputStream.toByteArray());
        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Proper decode across retries (single and multiple interrupts).
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) {
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        byte[] randomData = getRandomByteArray(dataSize);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();

        BlobClient uploadClient = createBlobClientWithRequestSniffer(recorded);
        uploadClient.upload(BinaryData.fromBytes(randomData));

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, uploadClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recorded.add(context.getHttpRequest().getHeaders());
            return next.process();
        };

        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            uploadClient.getBlobUrl(), sniffPolicy, mockPolicy);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        byte[] result = outputStream.toByteArray();
        assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
        TestUtils.assertArraysEqual(randomData, result);
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void openInputStreamContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        BlobInputStreamOptions options
            = new BlobInputStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobInputStream inputStream = client.openInputStream(options, Context.NONE);

        TestUtils.assertArraysEqual(data, convertInputStreamToByteArray(inputStream));
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void openInputStreamRangeContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        int start = Constants.MB;
        int count = 3 * Constants.MB + 257;

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        BlobInputStreamOptions options = new BlobInputStreamOptions().setRange(new BlobRange(start, (long) count))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64)
            .setBlockSize(Constants.MB);
        BlobInputStream inputStream = client.openInputStream(options, Context.NONE);

        byte[] downloadedRange = convertInputStreamToByteArray(inputStream);
        assertEquals(count, downloadedRange.length);
        TestUtils.assertArraysEqual(data, start, downloadedRange, 0, count);
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     *  openSeekableByteChannelRead with CRC64 content validation.
     */
    @ParameterizedTest
    @MethodSource("channelReadDataSupplier")
    public void openSeekableByteChannelReadContentValidation(Integer streamBufferSize, int copyBufferSize,
        int dataLength) throws IOException {
        byte[] data = getRandomByteArray(dataLength);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        // when: "Channel initialized"
        BlobSeekableByteChannelReadOptions options
            = new BlobSeekableByteChannelReadOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64)
                .setReadSizeInBytes(streamBufferSize);
        BlobSeekableByteChannelReadResult result = client.openSeekableByteChannelRead(options, Context.NONE);
        SeekableByteChannel channel = result.getChannel();

        // then: "Channel initialized to position zero"
        assertEquals(0, channel.position());
        assertNotNull(result.getProperties());
        assertEquals(data.length, result.getProperties().getBlobSize());

        // when: "read from channel"
        ByteArrayOutputStream downloadedData = new ByteArrayOutputStream();
        int copied = copy(channel, downloadedData, copyBufferSize);

        // then: "channel position updated accordingly"
        assertEquals(dataLength, copied);
        assertEquals(dataLength, channel.position());

        // and: "expected data downloaded"
        TestUtils.assertArraysEqual(data, downloadedData.toByteArray());
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    // @LiveOnly
    // @Test
    // public void downloadStreamSingleGetVeryLargeRangeContentValidation() {
    //     long requestedSpanBytes = 200L * Constants.GB;

    //     List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
    //     HttpPipelinePolicy sniffPolicy = (context, next) -> {
    //         recorded.add(context.getHttpRequest().getHeaders());
    //         return next.process();
    //     };
    //     BlobServiceClient serviceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
    //         ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), sniffPolicy);
    //     BlobClient client = serviceClient.getBlobContainerClient("largeblobholder").getBlobClient("largeblob");

    //     BlobRange range = new BlobRange(0, requestedSpanBytes);
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setRange(range)
    //         .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
    //     BlobDownloadResponse response = client.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

    //     assertEquals(1, recorded.size(), "Expected one HTTP request (no client-side partitioning of the download).");
    //     String rangeHeader = recorded.get(0).getValue(HttpHeaderName.fromString("x-ms-range"));
    //     assertNotNull(rangeHeader);
    //     assertEquals(String.format(Locale.ROOT, "bytes=0-%d", requestedSpanBytes - 1), rangeHeader,
    //         "x-ms-range should request the full configured span in a single Get.");
    // }

    // @LiveOnly
    // @Test
    // public void streamTwoHundredGibToLargeBlobHolder() throws IOException {
    //     final long twoHundredGibBytes = TWO_HUNDRED_GIB_BYTES;
    //     // Larger writes reduce loop overhead; BlobOutputStream deep-copies each write, so keep this reasonable.
    //     final int writeBufferBytes = 32 * Constants.MB;
    //     byte[] chunk = new byte[writeBufferBytes];
    //     Arrays.fill(chunk, (byte) 0x5A);

    //     BlobContainerClient containerClient = primaryBlobServiceClient.getBlobContainerClient("largeblobholder");
    //     BlockBlobClient blockBlobClient = containerClient.getBlobClient("largeblob").getBlockBlobClient();

    //     // Azure allows at most 50,000 blocks per blob. Use a large staged block size to cut Put Block round-trips.
    //     // Raise maxConcurrency (default is 8) so more blocks upload in parallel; memory is roughly blockSize * concurrency.
    //     final long stagedBlockBytes = 64L * Constants.MB;
    //     com.azure.storage.blob.models.ParallelTransferOptions parallelTransferOptions
    //         = new com.azure.storage.blob.models.ParallelTransferOptions().setBlockSizeLong(stagedBlockBytes)
    //             .setMaxConcurrency(32);
    //     BlockBlobOutputStreamOptions streamOptions
    //         = new BlockBlobOutputStreamOptions().setParallelTransferOptions(parallelTransferOptions);

    //     try (BlobOutputStream blobOutputStream = blockBlobClient.getBlobOutputStream(streamOptions, Context.NONE)) {
    //         long remaining = twoHundredGibBytes;
    //         while (remaining > 0) {
    //             int n = (int) Math.min(chunk.length, remaining);
    //             blobOutputStream.write(chunk, 0, n);
    //             remaining -= n;
    //         }
    //     }

    //     assertEquals(twoHundredGibBytes, blockBlobClient.getProperties().getBlobSize(),
    //         "Blob size should match the number of streamed bytes.");
    // }

    static Stream<Arguments> channelReadDataSupplier() {
        return Stream.of(Arguments.of(50, 40, Constants.KB), Arguments.of(Constants.KB + 50, 40, Constants.KB),
            Arguments.of(null, Constants.MB, TEN_MB));
    }
}
