// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;

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
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, Constants.KB, StructuredMessageFlags.STORAGE_CRC64);
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
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, Constants.KB, StructuredMessageFlags.STORAGE_CRC64);
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
        // Interrupt after first segment + 3 reads + 10 bytes (mirrors .NET interruptPos)
        int interruptPos = segmentSize + (3 * 128) + 10; // readLen in .NET test = 128 bytes
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = new BlobClientBuilder().endpoint(bc.getBlobUrl())
            .addPolicy(decoderPolicy)
            // Ensure the fault policy runs before decoding and on the initial call.
            .addPolicy(mockPolicy)
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .buildAsyncClient();

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation - should succeed despite the interruption
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify the decoded data matches the original exactly
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();
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
        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3, interruptPos, bc.getBlobUrl());

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        // Download with validation - should succeed despite multiple interruptions
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify the decoded data matches the original exactly
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();
    }

    @Test
    public void interruptAndVerifyProperRewind() throws IOException {
        // Test: Verify that interruption causes proper rewind to last complete segment boundary
        // This mirrors the .NET test: Interrupt_AppropriateRewind
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(2 * segmentSize);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy that will simulate 1 interruption at segment boundary + 2 reads + offset (per .NET)
        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10; // readLen = segmentSize/4
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        // Download with validation
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            // Ensure the fault policy was invoked even if the assertion below fails.
            .doFinally(signalType -> {
                System.out.println("[MockPartialResponsePolicy] hits=" + mockPolicy.getHits() + ", triesRemaining="
                    + mockPolicy.getTriesRemaining() + ", ranges=" + mockPolicy.getRangeHeaders());
                assertTrue(mockPolicy.getHits() > 0, "Mock interruption policy was not invoked");
            })
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify the decoded data matches the original
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();

        // Quick sanity: mock interruption should have been hit and range headers recorded.
        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) throws IOException {
        // Test: Verify that after interruption and retry, decoding continues correctly
        // Mirrors .NET Interrupt_ProperDecode (multipleInterrupts toggles number of injected faults)
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        byte[] randomData = getRandomByteArray(dataSize);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, segmentSize, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        // Create a policy with interruptions to test multi-step decode after retries
        // Interrupt after first segment + 3 reads + 10 bytes (per .NET)
        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10; // readLen = 8KB in .NET
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, bc.getBlobUrl());

        // Upload the encoded data
        bc.upload(input, null, true).block();

        // Create download client with mock interruption and decoder policies
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        // Download with validation - decoder must properly handle state across retries
        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                // Verify every byte is correctly decoded despite interruptions
                assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();
    }
}
