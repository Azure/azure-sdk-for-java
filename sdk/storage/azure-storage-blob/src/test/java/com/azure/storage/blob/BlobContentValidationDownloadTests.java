// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobSeekableByteChannelReadResult;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
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
import java.util.List;
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

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadResponse response = client.downloadStreamWithResponse(outputStream,
            new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null,
            Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        BlobDownloadContentResponse response = client.downloadContentWithResponse(
            new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null,
            Context.NONE);
        byte[] result = response.getValue().toBytes();

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
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

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        int blockSize = 4 * Constants.MB;
        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        Response<BlobProperties> response = client.downloadToFileWithResponse(options, null, Context.NONE);
        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        assertNotNull(response.getValue());
        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
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

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        int blockSize = 4 * Constants.MB;
        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        Response<BlobProperties> response = client.downloadToFileWithResponse(options, null, Context.NONE);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(response.getHeaders()));
        assertNotNull(response.getValue());
        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * Range download without content validation works correctly.
     */
    @Test
    public void downloadStreamWithResponseContentValidationRange() {
        byte[] randomData = getRandomByteArray(4 * Constants.KB);

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(randomData));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setRange(new BlobRange(0, 512L));
        client.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertEquals(512, outputStream.toByteArray().length);
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * Default behavior: when no algorithm is specified, default is NONE (no validation).
     */
    @Test
    public void downloadStreamDefaultAlgorithmIsNone() {
        byte[] data = getRandomByteArray(TEN_MB);

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        client.downloadStreamWithResponse(outputStream, new BlobDownloadStreamOptions(), null, Context.NONE);

        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * AUTO on downloadStream resolves to CRC64 behavior.
     */
    @Test
    public void downloadStreamWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options
            = new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO);
        BlobDownloadResponse response = client.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with NONE: no validation triggered.
     */
    @Test
    public void downloadContentWithNone() {
        byte[] data = getRandomByteArray(TEN_MB);

        HttpHeaders recorded = new HttpHeaders();
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
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with AUTO resolves to CRC64 behavior.
     */
    @Test
    public void downloadContentWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        BlobDownloadContentResponse response = client.downloadContentWithResponse(
            new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO), null,
            Context.NONE);
        byte[] result = response.getValue().toBytes();

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     * Interrupt with proper rewind to segment boundary; verifies retry range headers.
     */
    @Test
    public void interruptAndVerifyProperRewind() {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(2 * segmentSize);
        HttpHeaders recorded = new HttpHeaders();
        List<HttpHeaders> recordedResponseHeaders = new CopyOnWriteArrayList<>();

        BlobClient uploadClient = createBlobClientWithRequestSniffer(recorded);
        uploadClient.upload(BinaryData.fromBytes(randomData));

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(1, interruptPos, uploadClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy
            = getRequestAndResponseHeaderSniffer(uploadClient.getBlobUrl(), recorded, recordedResponseHeaders);

        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            uploadClient.getBlobUrl(), sniffPolicy, mockPolicy);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobDownloadResponse response
            = downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(randomData, outputStream.toByteArray());
        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
        assertTrue(recordedResponseHeaders.size() >= 2,
            "Expected at least the initial response and one retry response");
        assertTrue(
            recordedResponseHeaders.stream().allMatch(BlobTestBase::hasStructuredMessageDownloadResponseHeaders));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
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
        HttpHeaders recorded = new HttpHeaders();
        List<HttpHeaders> recordedResponseHeaders = new CopyOnWriteArrayList<>();

        BlobClient uploadClient = createBlobClientWithRequestSniffer(recorded);
        uploadClient.upload(BinaryData.fromBytes(randomData));

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, uploadClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy
            = getRequestAndResponseHeaderSniffer(uploadClient.getBlobUrl(), recorded, recordedResponseHeaders);

        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            uploadClient.getBlobUrl(), sniffPolicy, mockPolicy);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobDownloadResponse response
            = downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        byte[] result = outputStream.toByteArray();
        assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
        TestUtils.assertArraysEqual(randomData, result);
        assertTrue(!recordedResponseHeaders.isEmpty());
        assertTrue(
            recordedResponseHeaders.stream().allMatch(BlobTestBase::hasStructuredMessageDownloadResponseHeaders));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void openInputStreamContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        BlobInputStreamOptions options
            = new BlobInputStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobInputStream inputStream = client.openInputStream(options, Context.NONE);

        TestUtils.assertArraysEqual(data, convertInputStreamToByteArray(inputStream));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void openInputStreamRangeContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        int start = Constants.MB;
        int count = 3 * Constants.MB + 257;

        HttpHeaders recorded = new HttpHeaders();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);
        client.upload(BinaryData.fromBytes(data));

        BlobInputStreamOptions options = new BlobInputStreamOptions().setRange(new BlobRange(start, (long) count))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64)
            .setBlockSize(Constants.MB);
        BlobInputStream inputStream = client.openInputStream(options, Context.NONE);

        byte[] downloadedRange = convertInputStreamToByteArray(inputStream);
        assertEquals(count, downloadedRange.length);
        TestUtils.assertArraysEqual(data, start, downloadedRange, 0, count);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    /**
     *  openSeekableByteChannelRead with CRC64 content validation.
     */
    @ParameterizedTest
    @MethodSource("channelReadDataSupplier")
    public void openSeekableByteChannelReadContentValidation(Integer streamBufferSize, int copyBufferSize,
        int dataLength) throws IOException {
        byte[] data = getRandomByteArray(dataLength);

        HttpHeaders recorded = new HttpHeaders();
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
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded));
    }

    static Stream<Arguments> channelReadDataSupplier() {
        return Stream.of(Arguments.of(50, 40, Constants.KB), Arguments.of(Constants.KB + 50, 40, Constants.KB),
            Arguments.of(null, Constants.MB, TEN_MB));
    }
}
