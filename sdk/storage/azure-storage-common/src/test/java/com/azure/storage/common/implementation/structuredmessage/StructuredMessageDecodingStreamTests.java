// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.storage.common.DownloadContentValidationOptions;
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
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        Flux<ByteBuffer> encodedStream = Flux.just(encodedData);

        Flux<ByteBuffer> decodedStream = StructuredMessageDecodingStream.wrapStreamIfNeeded(encodedStream,
            (long) encodedData.remaining(), validationOptions);

        StepVerifier.create(decodedStream).assertNext(buffer -> {
            byte[] decodedData = new byte[buffer.remaining()];
            buffer.get(decodedData);
            assertArrayEquals(originalData, decodedData);
        }).verifyComplete();
    }

    @Test
    public void testWrapStreamWithValidationDisabled() {
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);
        ByteBuffer dataBuffer = ByteBuffer.wrap(originalData);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(false);

        Flux<ByteBuffer> originalStream = Flux.just(dataBuffer);

        Flux<ByteBuffer> resultStream = StructuredMessageDecodingStream.wrapStreamIfNeeded(originalStream,
            (long) originalData.length, validationOptions);

        StepVerifier.create(resultStream).assertNext(buffer -> {
            byte[] resultData = new byte[buffer.remaining()];
            buffer.get(resultData);
            assertArrayEquals(originalData, resultData);
        }).verifyComplete();
    }

    @Test
    public void testWrapStreamWithNullValidationOptions() {
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);
        ByteBuffer dataBuffer = ByteBuffer.wrap(originalData);

        Flux<ByteBuffer> originalStream = Flux.just(dataBuffer);

        Flux<ByteBuffer> resultStream
            = StructuredMessageDecodingStream.wrapStreamIfNeeded(originalStream, (long) originalData.length, null);

        StepVerifier.create(resultStream).assertNext(buffer -> {
            byte[] resultData = new byte[buffer.remaining()];
            buffer.get(resultData);
            assertArrayEquals(originalData, resultData);
        }).verifyComplete();
    }
}
