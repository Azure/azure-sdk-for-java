// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests {@link BufferedFluxByteBuffer}.
 */
public class BufferedFluxByteBufferTests {
    @Test
    public void coldBufferIsBuffered() {
        byte[] randomBytes = new byte[1024 * 1024];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        BufferedFluxByteBuffer bufferedFluxByteBuffer = new BufferedFluxByteBuffer(
            Flux.fromArray(splitBytesIntoBuffers(randomBytes)));

        // Run once to verify that the results are expected.
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bufferedFluxByteBuffer))
            .assertNext(bytes -> assertArrayEquals(randomBytes, bytes))
            .verifyComplete();

        // Run again to verify that the results are consistent.
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bufferedFluxByteBuffer))
            .assertNext(bytes -> assertArrayEquals(randomBytes, bytes))
            .verifyComplete();
    }

    @Test
    public void hotBufferIsBuffered() {
        byte[] randomBytes = new byte[1024 * 1024];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        BufferedFluxByteBuffer bufferedFluxByteBuffer = new BufferedFluxByteBuffer(
            Flux.fromArray(splitBytesIntoBuffers(randomBytes)).share());

        // Run once to verify that the results are expected.
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bufferedFluxByteBuffer))
            .assertNext(bytes -> assertArrayEquals(randomBytes, bytes))
            .verifyComplete();

        // Run again to verify that the results are consistent.
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(bufferedFluxByteBuffer))
            .assertNext(bytes -> assertArrayEquals(randomBytes, bytes))
            .verifyComplete();
    }

    private static ByteBuffer[] splitBytesIntoBuffers(byte[] bytes) {
        int expectedBuffers = (int) Math.ceil(bytes.length / (double) 1024);
        ByteBuffer[] buffers = new ByteBuffer[expectedBuffers];

        for (int i = 0; i < expectedBuffers; i++) {
            int bufferLength = Math.min(1024, bytes.length - (1024 * i));
            buffers[i] = ByteBuffer.wrap(bytes, i * 1024, bufferLength);
        }

        return buffers;
    }
}
