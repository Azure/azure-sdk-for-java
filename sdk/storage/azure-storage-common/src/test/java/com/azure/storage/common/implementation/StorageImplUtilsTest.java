// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link StorageImplUtils}.
 */
public class StorageImplUtilsTest {
    private static final byte[] HELLO_WORLD_BYTES = "Hello world!".getBytes(StandardCharsets.UTF_8);

    /**
     * Tests that using {@link StorageImplUtils#deepCloneStreamBuffers(Flux)} will ensure that if the underlying buffers
     * in the stream are mutated after the 'onNext' operation chains completes the stream we return will remain
     * consistent.
     */
    @Test
    public void deepCloneStream() {
        byte[] mutatingBuffer = Arrays.copyOf(HELLO_WORLD_BYTES, HELLO_WORLD_BYTES.length);

        DelayedWriteStream delayedWriteStream = new DelayedWriteStream();

        Disposable disposable = StorageImplUtils.deepCloneStreamBuffers(Flux.just(ByteBuffer.wrap(mutatingBuffer)))
            .subscribe(buffer -> delayedWriteStream.write(buffer.array()));

        while (!disposable.isDisposed()) {
            // NO-OP waiting until complete.
        }

        mutatingBuffer[0] = (byte) 0;

        while (!delayedWriteStream.writeFuture.isDone()) {
            // Wait until future is done.
        }

        assertArrayEquals(HELLO_WORLD_BYTES, delayedWriteStream.internalBuffer);
    }

    /**
     * Tests that when {@link StorageImplUtils#deepCloneStreamBuffers(Flux)} isn't used and the underlying buffers in
     * the stream are mutated after the 'onNext' operation chain completes the stream returned won't remain consistent.
     */
    @Test
    public void mutatingStreamFails() {
        byte[] mutatingBuffer = Arrays.copyOf(HELLO_WORLD_BYTES, HELLO_WORLD_BYTES.length);

        DelayedWriteStream delayedWriteStream = new DelayedWriteStream();

        Disposable disposable = Flux.just(ByteBuffer.wrap(mutatingBuffer))
            .subscribe(buffer -> delayedWriteStream.write(buffer.array()));

        while (!disposable.isDisposed()) {
            // NO-OP waiting until complete.
        }

        mutatingBuffer[0] = (byte) 0;

        while (!delayedWriteStream.writeFuture.isDone()) {
            // Wait until future is done.
        }

        assertFalse(Arrays.equals(HELLO_WORLD_BYTES, delayedWriteStream.internalBuffer));
    }

    /**
     * Tests that passing {@link StorageImplUtils#deepCloneStreamBuffers(Flux)} {@code null} will return {@code null}.
     */
    @Test
    public void passingDeepStreamCloneNullReturnsNull() {
        assertNull(StorageImplUtils.deepCloneStreamBuffers(null));
    }

    /*
     * OutputStream that delays writing to its internal buffer for 5 seconds.
     */
    private static final class DelayedWriteStream extends OutputStream {
        private byte[] internalBuffer;
        private ScheduledFuture<?> writeFuture;

        @Override
        public void write(int b) {
            write(new byte[] { (byte) b });
        }

        @Override
        public void write(byte[] b) {
            writeFuture = Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> internalBuffer = b, 5, TimeUnit.SECONDS);
        }
    }
}
