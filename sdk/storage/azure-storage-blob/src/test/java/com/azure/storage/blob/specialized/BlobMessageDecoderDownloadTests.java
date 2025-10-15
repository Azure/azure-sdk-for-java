// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;

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
        byte[] randomData = getRandomByteArray(Constants.KB);
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(randomData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(randomData));

        Flux<ByteBuffer> input = Flux.just(encodedData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        BlobRange range = new BlobRange(0, 512L);

        StepVerifier.create(bc.upload(input, null, true)
            .then(bc.downloadStreamWithResponse(range, (DownloadRetryOptions) null,
                (BlobRequestConditions) null, false, validationOptions))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                assertTrue(r.length > 0);
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
        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> {
                assertNotNull(r);
                // Should get encoded data, not decoded
                assertTrue(r.length > randomData.length); // Encoded data is larger
            })
            .verifyComplete();
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

        StepVerifier
            .create(bc.upload(input, null, true)
                .then(bc.downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null,
                    (BlobRequestConditions) null, false, validationOptions))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> {
                assertNotNull(r);
                // Should get encoded data, not decoded
                assertTrue(r.length > randomData.length); // Encoded data is larger
            })
            .verifyComplete();
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
}
