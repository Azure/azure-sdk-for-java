// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class StructuredMessageDecodingStreamTests {

    @Test
    public void testWrapStreamWithValidationEnabled() throws IOException {
        // Create test data
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        // Encode the data using StructuredMessageEncoder
        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));

        // Create validation options
        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        // Create a stream with the encoded data
        Flux<ByteBuffer> encodedStream = Flux.just(encodedData);

        // Apply structured message decoding
        Flux<ByteBuffer> decodedStream = StructuredMessageDecodingStream.wrapStreamIfNeeded(encodedStream,
            (long) encodedData.remaining(), validationOptions);

        // Verify the decoded data matches original data
        StepVerifier.create(decodedStream).assertNext(buffer -> {
            byte[] decodedData = new byte[buffer.remaining()];
            buffer.get(decodedData);
            assertArrayEquals(originalData, decodedData);
        }).verifyComplete();
    }

    @Test
    public void testWrapStreamWithValidationDisabled() {
        // Create test data
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);
        ByteBuffer dataBuffer = ByteBuffer.wrap(originalData);

        // Create validation options with validation disabled
        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(false);

        // Create a stream with the data
        Flux<ByteBuffer> originalStream = Flux.just(dataBuffer);

        // Apply structured message decoding (should return original stream)
        Flux<ByteBuffer> resultStream = StructuredMessageDecodingStream.wrapStreamIfNeeded(originalStream,
            (long) originalData.length, validationOptions);

        // Verify the stream is returned unchanged
        StepVerifier.create(resultStream).assertNext(buffer -> {
            byte[] resultData = new byte[buffer.remaining()];
            buffer.get(resultData);
            assertArrayEquals(originalData, resultData);
        }).verifyComplete();
    }

    @Test
    public void testWrapStreamWithNullValidationOptions() {
        // Create test data
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);
        ByteBuffer dataBuffer = ByteBuffer.wrap(originalData);

        // Create a stream with the data
        Flux<ByteBuffer> originalStream = Flux.just(dataBuffer);

        // Apply structured message decoding with null options (should return original stream)
        Flux<ByteBuffer> resultStream
            = StructuredMessageDecodingStream.wrapStreamIfNeeded(originalStream, (long) originalData.length, null);

        // Verify the stream is returned unchanged
        StepVerifier.create(resultStream).assertNext(buffer -> {
            byte[] resultData = new byte[buffer.remaining()];
            buffer.get(resultData);
            assertArrayEquals(originalData, resultData);
        }).verifyComplete();
    }
}
