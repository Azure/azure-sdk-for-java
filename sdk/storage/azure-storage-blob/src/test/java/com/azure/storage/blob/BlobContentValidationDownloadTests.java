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
    private static final int BLOCK_SIZE = 4 * Constants.MB;

    private final List<File> createdFiles = new ArrayList<>();

    private byte[] data;
    private HttpHeaders recordedRequestHeaders;
    private HttpHeaders recordedResponseHeaders;
    private BlobClient blobClient;
    private BlobClient downloadClient;
    private File file;
    private File outFile;
    private ByteArrayOutputStream outputStream;

    @Override
    public void beforeTest() {
        super.beforeTest();
        data = null;
        recordedRequestHeaders = new HttpHeaders();
        recordedResponseHeaders = new HttpHeaders();
        blobClient = null;
        downloadClient = null;
        outputStream = null;
    }

    @Override
    protected void afterTest() {
        createdFiles.forEach(File::delete);
        createdFiles.clear();
        data = null;
        recordedRequestHeaders = new HttpHeaders();
        recordedResponseHeaders = new HttpHeaders();
        blobClient = null;
        downloadClient = null;
        file = null;
        outFile = null;
        outputStream = null;
        super.afterTest();
    }

    private void initializeBlobClient() {
        blobClient = createBlobClientWithRequestSniffer(recordedRequestHeaders);
        downloadClient = blobClient;
    }

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidation() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        outputStream = new ByteArrayOutputStream();
        BlobDownloadResponse response = downloadClient.downloadStreamWithResponse(outputStream,
            new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null,
            Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidation() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        BlobDownloadContentResponse response = downloadClient.downloadContentWithResponse(
            new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null,
            Context.NONE);
        byte[] result = response.getValue().toBytes();

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
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
        file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        initializeBlobClient();
        blobClient.uploadFromFile(file.toPath().toString(), true);

        outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        Response<BlobProperties> response = downloadClient.downloadToFileWithResponse(options, null, Context.NONE);
        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        assertNotNull(response.getValue());
        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
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
        file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        initializeBlobClient();
        blobClient.uploadFromFile(file.toPath().toString(), true);

        outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        Response<BlobProperties> response = downloadClient.downloadToFileWithResponse(options, null, Context.NONE);
        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        assertNotNull(response.getValue());
        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * Range download without content validation works correctly.
     */
    @Test
    public void downloadStreamWithResponseContentValidationRange() {
        data = getRandomByteArray(4 * Constants.KB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setRange(new BlobRange(0, 512L));
        downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertEquals(512, outputStream.toByteArray().length);
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * Default behavior: when no algorithm is specified, default is NONE (no validation).
     */
    @Test
    public void downloadStreamDefaultAlgorithmIsNone() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        outputStream = new ByteArrayOutputStream();
        downloadClient.downloadStreamWithResponse(outputStream, new BlobDownloadStreamOptions(), null, Context.NONE);

        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * AUTO on downloadStream resolves to CRC64 behavior.
     */
    @Test
    public void downloadStreamWithAuto() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options
            = new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO);
        BlobDownloadResponse response
            = downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * downloadContentWithResponse with NONE: no validation triggered.
     */
    @Test
    public void downloadContentWithNone() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        byte[] result
            = downloadClient
                .downloadContentWithResponse(
                    new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.NONE),
                    null, Context.NONE)
                .getValue()
                .toBytes();

        TestUtils.assertArraysEqual(data, result);
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * downloadContentWithResponse with AUTO resolves to CRC64 behavior.
     */
    @Test
    public void downloadContentWithAuto() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        BlobDownloadContentResponse response = downloadClient.downloadContentWithResponse(
            new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO), null,
            Context.NONE);
        byte[] result = response.getValue().toBytes();

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * Interrupt with proper rewind to segment boundary; verifies retry range headers.
     */
    @Test
    public void interruptAndVerifyProperRewind() {
        final int segmentSize = Constants.KB;
        data = getRandomByteArray(2 * segmentSize);
        initializeBlobClient();

        blobClient.upload(BinaryData.fromBytes(data));

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = getRequestAndResponseHeaderSniffer(blobClient.getBlobUrl(),
            recordedRequestHeaders, recordedResponseHeaders);

        downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), blobClient.getBlobUrl(),
            sniffPolicy, mockPolicy);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobDownloadResponse response
            = downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        TestUtils.assertArraysEqual(data, outputStream.toByteArray());
        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
        assertTrue(hasStructuredMessageDownloadResponseHeaders(recordedResponseHeaders));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     * Proper decode across retries (single and multiple interrupts).
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) {
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        data = getRandomByteArray(dataSize);
        initializeBlobClient();

        blobClient.upload(BinaryData.fromBytes(data));

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = getRequestAndResponseHeaderSniffer(blobClient.getBlobUrl(),
            recordedRequestHeaders, recordedResponseHeaders);

        downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), blobClient.getBlobUrl(),
            sniffPolicy, mockPolicy);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        outputStream = new ByteArrayOutputStream();
        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobDownloadResponse response
            = downloadClient.downloadStreamWithResponse(outputStream, options, null, Context.NONE);

        assertTrue(hasStructuredMessageDownloadResponseHeaders(response.getHeaders()));
        byte[] result = outputStream.toByteArray();
        assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
        TestUtils.assertArraysEqual(data, result);
        assertTrue(hasStructuredMessageDownloadResponseHeaders(recordedResponseHeaders));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void openInputStreamContentValidation() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();
        blobClient.upload(BinaryData.fromBytes(data));

        BlobInputStreamOptions options
            = new BlobInputStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobInputStream inputStream = downloadClient.openInputStream(options, Context.NONE);

        TestUtils.assertArraysEqual(data, convertInputStreamToByteArray(inputStream));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    @Test
    public void openInputStreamRangeContentValidation() {
        data = getRandomByteArray(TEN_MB);
        initializeBlobClient();

        int start = Constants.MB;
        int count = 3 * Constants.MB + 257;

        blobClient.upload(BinaryData.fromBytes(data));

        BlobInputStreamOptions options = new BlobInputStreamOptions().setRange(new BlobRange(start, (long) count))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64)
            .setBlockSize(Constants.MB);
        BlobInputStream inputStream = downloadClient.openInputStream(options, Context.NONE);

        byte[] downloadedRange = convertInputStreamToByteArray(inputStream);
        assertEquals(count, downloadedRange.length);
        TestUtils.assertArraysEqual(data, start, downloadedRange, 0, count);
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    /**
     *  openSeekableByteChannelRead with CRC64 content validation.
     */
    @ParameterizedTest
    @MethodSource("channelReadDataSupplier")
    public void openSeekableByteChannelReadContentValidation(Integer streamBufferSize, int copyBufferSize,
        int dataLength) throws IOException {
        data = getRandomByteArray(dataLength);
        initializeBlobClient();

        blobClient.upload(BinaryData.fromBytes(data));

        // when: "Channel initialized"
        BlobSeekableByteChannelReadOptions options
            = new BlobSeekableByteChannelReadOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64)
                .setReadSizeInBytes(streamBufferSize);
        BlobSeekableByteChannelReadResult result = downloadClient.openSeekableByteChannelRead(options, Context.NONE);
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
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders));
    }

    static Stream<Arguments> channelReadDataSupplier() {
        return Stream.of(Arguments.of(50, 40, Constants.KB), Arguments.of(Constants.KB + 50, 40, Constants.KB),
            Arguments.of(null, Constants.MB, TEN_MB));
    }
}
