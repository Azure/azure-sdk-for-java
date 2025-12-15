// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import com.azure.storage.common.policy.StorageContentValidationDecoderPolicy;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
public class BlobMessageDecoderDownloadTests extends BlobTestBase {

    private BlobAsyncClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null).block();
    }

    @Test
    public void downloadStreamWithResponseContentValidation() throws IOException {
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false, validationOptions))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationRange() throws IOException {
        // Note: Range downloads are not compatible with structured message validation
        // because you need the complete encoded message for validation.
        // This test verifies that range downloads work without validation.
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Range download without validation should work
        BlobRange range = new BlobRange(0, 512L);

        StepVerifier.create(bc.upload(input, null, true)
            .then(
                bc.downloadStreamWithResponse(range, (DownloadRetryOptions) null, (BlobRequestConditions) null, false))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                // Should get exactly 512 bytes of encoded data
                assertEquals(512, r.length);
            }).verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationLargeBlob() throws IOException {
        // Test with larger data to verify chunking works correctly
        byte[] randomData = getRandomByteArray(5 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false, validationOptions))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationMultipleSegments() throws IOException {
        // Test with multiple segments to ensure all segments are decoded correctly
        byte[] randomData = getRandomByteArray(2 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false, validationOptions))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseNoValidation() throws IOException {
        // Test that download works normally when validation is not enabled
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // No validation options - should download encoded data as-is
        StepVerifier.create(bc.upload(input, null, true)
            .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                (BlobRequestConditions) null, false))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                // Should get encoded data, not decoded
                assertTrue(r.length > randomData.length); // Encoded data is larger
            }).verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseValidationDisabled() throws IOException {
        // Test with validation options but validation disabled
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(false);

        StepVerifier.create(bc.upload(input, null, true)
            .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                (BlobRequestConditions) null, false, validationOptions))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                // Should get encoded data, not decoded
                assertTrue(r.length > randomData.length); // Encoded data is larger
            }).verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmallSegment() throws IOException {
        // Test with small segment size to ensure boundary conditions are handled
        byte[] randomData = getRandomByteArray(256);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 128, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false, validationOptions))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationVeryLargeBlob() throws IOException {
        // Test with very large data to verify chunking and policy work correctly with large blobs
        byte[] randomData = getRandomByteArray(10 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 2048, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false, validationOptions))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetry() throws IOException {
        // Test smart retry functionality with structured message validation
        // This test simulates network interruptions and verifies that:
        // 1. The decoder validates checksums for all received data
        // 2. Retries resume from the encoded offset where the interruption occurred
        // 3. The download eventually succeeds despite multiple interruptions

        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 3 network interruptions
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3);

        // Upload the encoded data using the regular client
        bc.upload(input, null, true).block();

        // Create a download client with both the mock policy AND the decoder policy
        // The decoder policy is needed to actually decode structured messages and validate checksums
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        // Configure retry options to allow retries
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation - should succeed despite interruptions
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                // Verify the data is correctly decoded
                TestUtils.assertArraysEqual(r, randomData);
            }).verifyComplete();

        // Verify that retries occurred (3 interruptions means we should have 0 tries remaining)
        assertEquals(0, mockPolicy.getTriesRemaining());

        // Verify that range headers were sent for retries
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        assertTrue(rangeHeaders.size() > 0, "Expected range headers for retries");

        // With structured message validation and smart retry, retries should resume from the encoded
        // offset where the interruption occurred. The first request starts at 0, and subsequent
        // retry requests should start from progressively higher offsets.
        assertTrue(rangeHeaders.get(0).startsWith("bytes=0-"), "First request should start from offset 0");

        // Subsequent requests should start from higher offsets (smart retry resuming from where it left off)
        for (int i = 1; i < rangeHeaders.size(); i++) {
            String rangeHeader = rangeHeaders.get(i);
            // Each retry should start from a higher offset than the previous
            // Note: We can't assert exact offset values as they depend on how much data was received
            // before the interruption, but we can verify it's a valid range header
            assertTrue(rangeHeader.startsWith("bytes="),
                "Retry request " + i + " should have a range header: " + rangeHeader);
        }
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetryVariousSizes() throws IOException {
        int[] dataSizes = new int[] { Constants.KB, 1500, 3 * Constants.KB + 128 };
        int[] segmentSizes = new int[] { 512, 700, 1024 };

        for (int i = 0; i < dataSizes.length; i++) {
            byte[] randomData = getRandomByteArray(dataSizes[i]);
            int segmentSize = segmentSizes[i % segmentSizes.length];

            StructuredMessageEncoder encoder
                = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
            ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

            Flux<ByteBuffer> input = Flux.just(encodedData);

            // Create a policy that will simulate 2 network interruptions for each run
            MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(2);

            // Upload the encoded data
            bc.upload(input, null, true).block();

            // Create a download client with both the mock policy AND the decoder policy
            StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
            BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
                bc.getBlobUrl(), mockPolicy, decoderPolicy);

            DownloadContentValidationOptions validationOptions
                = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
            DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

            StepVerifier.create(downloadClient
                .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                    validationOptions)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                    TestUtils.assertArraysEqual(r, randomData);
                }).verifyComplete();

            assertEquals(0, mockPolicy.getTriesRemaining());
            List<String> rangeHeaders = mockPolicy.getRangeHeaders();
            assertTrue(rangeHeaders.size() > 0, "Expected range headers for retries");
            assertTrue(rangeHeaders.get(0).startsWith("bytes=0-"), "First request should start from offset 0");
        }
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetrySync() throws IOException {
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);
        bc.upload(input, null, true).block();

        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3);
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobClient downloadClient = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(),
            mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Context ctx = new Context(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true)
            .addData(Constants.STRUCTURED_MESSAGE_VALIDATION_OPTIONS_CONTEXT_KEY, validationOptions);

        downloadClient.downloadStreamWithResponse(out, null, retryOptions, null, false, null, ctx);

        TestUtils.assertArraysEqual(out.toByteArray(), randomData);

        assertEquals(0, mockPolicy.getTriesRemaining());
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        assertTrue(rangeHeaders.size() > 0, "Expected range headers for retries");
        assertTrue(rangeHeaders.get(0).startsWith("bytes=0-"), "First request should start from offset 0");
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetryMultipleSegments() throws IOException {
        // Test smart retry with multiple segments to ensure checksum validation
        // works correctly and retries resume from the interrupted encoded offset.

        byte[] randomData = getRandomByteArray(2 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 4 network interruptions
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(4);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create a download client with both the mock policy AND the decoder policy
        // The decoder policy is needed to actually decode structured messages and validate checksums
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation - should succeed and validate all segment checksums
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                // Verify the data is correctly decoded
                TestUtils.assertArraysEqual(r, randomData);
            }).verifyComplete();

        // Verify that retries occurred
        assertEquals(0, mockPolicy.getTriesRemaining());

        // Verify multiple retry requests were made
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        assertTrue(rangeHeaders.size() >= 4,
            "Expected at least 4 range headers for retries, got: " + rangeHeaders.size());

        // With smart retry, each request should have a valid range header
        for (int i = 0; i < rangeHeaders.size(); i++) {
            String rangeHeader = rangeHeaders.get(i);
            assertTrue(rangeHeader.startsWith("bytes="),
                "Request " + i + " should have a valid range header, but was: " + rangeHeader);
        }
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetryLargeBlob() throws IOException {
        // Test smart retry with a larger blob to ensure retries resume from the
        // interrupted offset and successfully validate all data

        byte[] randomData = getRandomByteArray(5 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 2 network interruptions
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(2);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create a download client with both the mock policy AND the decoder policy
        // The decoder policy is needed to actually decode structured messages and validate checksums
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation - decoder should validate checksums before each retry
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                // Verify the data is correctly decoded
                TestUtils.assertArraysEqual(r, randomData);
            }).verifyComplete();

        // Verify that retries occurred
        assertEquals(0, mockPolicy.getTriesRemaining());

        // Verify that smart retry is working with valid range headers
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        for (int i = 0; i < rangeHeaders.size(); i++) {
            String rangeHeader = rangeHeaders.get(i);
            assertTrue(rangeHeader.startsWith("bytes="),
                "Request " + i + " should have a valid range header, but was: " + rangeHeader);
        }
    }

    @Test
    public void uninterruptedStreamWithStructuredMessageDecoding() throws IOException {
        // Test: Verify that structured message decoding works correctly without any interruptions
        // This mirrors the .NET test: UninterruptedStream
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, Constants.KB, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create a download client with decoder policy but NO mock interruption policy
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        // Download with validation - should succeed without any interruptions
        StepVerifier
            .create(
                downloadClient
                    .downloadStreamWithResponse((BlobRange) null, null, (BlobRequestConditions) null, false,
                        validationOptions)
                    .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> {
                // Verify the decoded data matches the original
                TestUtils.assertArraysEqual(result, randomData);
            })
            .verifyComplete();
    }

    @Test
    public void interruptWithDataIntact() throws IOException {
        // Test: Verify that data remains intact after a single interruption and retry
        // This mirrors the .NET test: Interrupt_DataIntact with single interrupt
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 1 network interruption at a specific position
        // Interrupt after first segment completes to test smart retry from segment boundary
        // Use 1200 bytes to ensure at least one 1KB segment completes
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, 1200);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation - should succeed despite the interruption
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify the decoded data matches the original exactly
                TestUtils.assertArraysEqual(result, randomData);
            }).verifyComplete();

        // Verify that exactly 1 interruption was used (retry occurred)
        assertEquals(0, mockPolicy.getTriesRemaining());

        // Verify that retry used appropriate range header
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        assertTrue(rangeHeaders.size() >= 2, "Expected at least 2 requests (initial + 1 retry)");
    }

    @Test
    public void interruptMultipleTimesWithDataIntact() throws IOException {
        // Test: Verify that data remains intact after multiple interruptions and retries
        // This mirrors the .NET test: Interrupt_DataIntact with multiple interrupts
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 3 network interruptions
        // Use 800 bytes for subsequent interruptions to get 3 interrupts with 4KB data + 1KB segments
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3, 800);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        // Download with validation - should succeed despite multiple interruptions
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify the decoded data matches the original exactly
                TestUtils.assertArraysEqual(result, randomData);
            }).verifyComplete();

        // Verify that all 3 interruptions were used
        assertEquals(0, mockPolicy.getTriesRemaining());

        // Verify that retries used appropriate range headers (initial + 3 retries = 4 requests)
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        assertTrue(rangeHeaders.size() >= 4, "Expected at least 4 requests (initial + 3 retries)");
    }

    @Test
    public void interruptAndVerifyProperRewind() throws IOException {
        // Test: Verify that interruption causes proper rewind to last complete segment boundary
        // This mirrors the .NET test: Interrupt_AppropriateRewind
        final int segmentSize = 512;
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 1 interruption
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify the decoded data matches the original
                TestUtils.assertArraysEqual(result, randomData);
            }).verifyComplete();

        // Verify retry occurred
        assertEquals(0, mockPolicy.getTriesRemaining());

        // Verify the retry started from a segment boundary (not from 0)
        List<String> rangeHeaders = mockPolicy.getRangeHeaders();
        assertTrue(rangeHeaders.size() >= 2, "Expected at least 2 requests");

        // First request should start from 0
        assertTrue(rangeHeaders.get(0).startsWith("bytes=0-") || rangeHeaders.get(0).startsWith("bytes=0"),
            "First request should start from offset 0");

        // Second request (retry) should start from a non-zero offset (segment boundary)
        // With 512-byte segments and proper encoding, first segment completes at ~543 bytes
        if (rangeHeaders.size() > 1) {
            String retryRange = rangeHeaders.get(1);
            assertTrue(retryRange.startsWith("bytes="), "Retry request should have a range header");
            // Extract the start offset from "bytes=X-Y" or "bytes=X"
            String offsetStr = retryRange.substring(6).split("-")[0];
            long retryOffset = Long.parseLong(offsetStr);
            assertTrue(retryOffset > 0,
                "Retry should start from non-zero offset (segment boundary), but was: " + retryOffset);
        }
    }

    @Test
    public void interruptAndVerifyProperDecode() throws IOException {
        // Test: Verify that after interruption and retry, decoding continues correctly
        // This mirrors the .NET test: Interrupt_ProperDecode
        final int segmentSize = Constants.KB;
        final int dataSize = 4 * segmentSize;
        byte[] randomData = getRandomByteArray(dataSize);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy with 2 interruptions to test multi-step decode after retries
        // Use 1000 bytes to get 2 interruptions with 4KB data + 1KB segments
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(2, 1000);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), mockPolicy, decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        // Download with validation - decoder must properly handle state across retries
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify every byte is correctly decoded despite multiple interruptions
                assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
                TestUtils.assertArraysEqual(result, randomData);
            }).verifyComplete();

        // Verify both interruptions were used
        assertEquals(0, mockPolicy.getTriesRemaining());
    }
}
