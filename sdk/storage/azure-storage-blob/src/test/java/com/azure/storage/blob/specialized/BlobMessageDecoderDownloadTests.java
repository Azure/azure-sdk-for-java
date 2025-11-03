// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
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
        // 1. The decoder validates checksums for all received data before retry
        // 2. The decoder state is preserved across retries
        // 3. The SDK continues from the correct offset after interruption

        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 3 network interruptions
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3);

        // Upload the encoded data using the regular client
        bc.upload(input, null, true).block();

        // Create a download client with the mock policy to simulate interruptions
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

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

        // The first request should be for the entire blob
        assertTrue(rangeHeaders.get(0).startsWith("bytes=0-"), "First request should start from offset 0");

        // Subsequent requests should continue from where we left off (after the first byte)
        for (int i = 1; i < rangeHeaders.size(); i++) {
            String rangeHeader = rangeHeaders.get(i);
            assertTrue(rangeHeader.startsWith("bytes=" + i + "-"),
                "Retry request " + i + " should start from offset " + i + " but was: " + rangeHeader);
        }
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetryMultipleSegments() throws IOException {
        // Test smart retry with multiple segments to ensure checksum validation
        // works correctly across segment boundaries during network interruptions

        byte[] randomData = getRandomByteArray(2 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 4 network interruptions
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(4);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create a download client with the mock policy
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

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
    }

    @Test
    public void downloadStreamWithResponseContentValidationSmartRetryLargeBlob() throws IOException {
        // Test smart retry with a larger blob to ensure decoder state
        // is correctly maintained across retries with more data

        byte[] randomData = getRandomByteArray(5 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 2 network interruptions
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(2);

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create a download client with the mock policy
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

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
    }
}
