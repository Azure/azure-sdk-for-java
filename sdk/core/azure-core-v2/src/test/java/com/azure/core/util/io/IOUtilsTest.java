// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.io;

import com.azure.core.TestByteArrayOutputStream;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.MockFluxHttpResponse;
import io.clientcore.core.http.rest.StreamResponse;
import com.azure.core.v2.util.FaultyAsynchronousByteChannel;
import com.azure.core.v2.util.PartialWriteAsynchronousChannel;
import com.azure.core.v2.util.PartialWriteChannel;
import com.azure.core.v2.util.mocking.MockAsynchronousFileChannel;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IOUtilsTest {
    private static final HttpRequest MOCK_REQUEST = new HttpRequest(HttpMethod.GET, "https://example.com");

    @Test
    public void canTransferFromReadableByteChannelToWriteableByteChannel() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        fillArray(data);

        TestByteArrayOutputStream byteArrayOutputStream = new TestByteArrayOutputStream(data.length);
        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        WritableByteChannel destination = Channels.newChannel(byteArrayOutputStream);

        IOUtils.transfer(source, destination);

        assertArraysEqual(data, byteArrayOutputStream.toByteArrayUnsafe());
    }

    @Test
    public void canTransferFromReadableByteChannelToWriteableByteChannelWithPartialWrites() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        fillArray(data);

        TestByteArrayOutputStream byteArrayOutputStream = new TestByteArrayOutputStream(data.length);
        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        WritableByteChannel destination = new PartialWriteChannel(Channels.newChannel(byteArrayOutputStream));

        IOUtils.transfer(source, destination);

        assertArraysEqual(data, byteArrayOutputStream.toByteArrayUnsafe());
    }

    @Test
    public void canTransferFromReadableByteChannelToAsynchronousByteChannel() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousFileChannel = new MockAsynchronousFileChannel(written);

        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        try (AsynchronousByteChannel destination = IOUtils.toAsynchronousByteChannel(mockAsynchronousFileChannel, 0)) {
            IOUtils.transferAsync(source, destination).block();
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canTransferFromReadableByteChannelToAsynchronousByteChannelWithPartialWrites() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousFileChannel = new MockAsynchronousFileChannel(written);

        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        try (AsynchronousByteChannel destination = IOUtils.toAsynchronousByteChannel(mockAsynchronousFileChannel, 0)) {
            AsynchronousByteChannel paritialWriteDestination = new PartialWriteAsynchronousChannel(destination);
            IOUtils.transferAsync(source, paritialWriteDestination).block();
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canResumeStreamResponseTransfer() throws IOException, InterruptedException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousFileChannel = new MockAsynchronousFileChannel(written);

        Function<Integer, Flux<ByteBuffer>> fluxSupplier
            = offset -> Flux.generate(() -> offset, (currentOffset, sink) -> {
                int size = Math.min(4096, data.length - currentOffset);
                if (size > 0) {
                    sink.next(ByteBuffer.wrap(data, currentOffset, size));
                } else {
                    sink.complete();
                }
                return currentOffset + size;
            });
        AtomicInteger retries = new AtomicInteger();
        ConcurrentLinkedQueue<Long> offsets = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> throwables = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MockFluxResponse<?>  responses = new ConcurrentLinkedQueue<>();
        MockFluxHttpResponse httpResponse = new MockFluxHttpResponse(MOCK_REQUEST, fluxSupplier.apply(0));
        responses.add(httpResponse);
        StreamResponse initialResponse = new StreamResponse(httpResponse);
        BiFunction<Throwable, Long, StreamResponse>> onErrorResume = (throwable, offset) -> {
            retries.incrementAndGet();
            offsets.add(offset);
            throwables.add(throwable);
            MockFluxHttpResponse newHttpResponse
                = new MockFluxHttpResponse(MOCK_REQUEST, fluxSupplier.apply(offset.intValue()));
            responses.add(newHttpResponse);
            return new StreamResponse(newHttpResponse));
        };

        try (FaultyAsynchronousByteChannel channel
            = new FaultyAsynchronousByteChannel(IOUtils.toAsynchronousByteChannel(mockAsynchronousFileChannel, 0),
                () -> new IOException("KABOOM"), 3, 16384)) {

            StepVerifier
                .create(IOUtils.transferStreamResponseToAsynchronousByteChannel(channel, initialResponse, onErrorResume,
                    null, 5))
                .verifyComplete();
        }

        assertEquals(3, retries.get());
        assertEquals(3, offsets.size());
        offsets.forEach(e -> assertEquals(16384, e));
        assertEquals(3, throwables.size());
        throwables.forEach(e -> assertEquals("KABOOM", e.getMessage()));
        assertArraysEqual(data, written);
        // check that all responses are closed
        assertEquals(4, responses.size());
        Thread.sleep(100); // Give all responses a chance to close
        responses.forEach(r -> assertTrue(r.isClosed()));
    }

    @Test
    public void throwsIfRetriesAreExhausted() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        fillArray(data);

        byte[] written = new byte[1024];
        MockAsynchronousFileChannel mockAsynchronousFileChannel = new MockAsynchronousFileChannel(written);

        Function<Integer, Flux<ByteBuffer>> fluxSupplier
            = offset -> Flux.generate(() -> offset, (currentOffset, sink) -> {
                int size = Math.min(256, data.length - currentOffset);
                if (size > 0) {
                    sink.next(ByteBuffer.wrap(data, currentOffset, size));
                } else {
                    sink.complete();
                }
                return currentOffset + size;
            });
        AtomicInteger retries = new AtomicInteger();
        ConcurrentLinkedQueue<Long> offsets = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> throwables = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MockFluxResponse<?>  responses = new ConcurrentLinkedQueue<>();
        MockFluxHttpResponse httpResponse = new MockFluxHttpResponse(MOCK_REQUEST, fluxSupplier.apply(0));
        responses.add(httpResponse);
        StreamResponse initialResponse = new StreamResponse(httpResponse);
        BiFunction<Throwable, Long, StreamResponse>> onErrorResume = (throwable, offset) -> {
            retries.incrementAndGet();
            offsets.add(offset);
            throwables.add(throwable);
            MockFluxHttpResponse newHttpResponse
                = new MockFluxHttpResponse(MOCK_REQUEST, fluxSupplier.apply(offset.intValue()));
            responses.add(newHttpResponse);
            return new StreamResponse(newHttpResponse));
        };

        try (FaultyAsynchronousByteChannel channel
            = new FaultyAsynchronousByteChannel(IOUtils.toAsynchronousByteChannel(mockAsynchronousFileChannel, 0),
                () -> new IOException("KABOOM"), 3, 1024)) {

            StepVerifier
                .create(IOUtils.transferStreamResponseToAsynchronousByteChannel(channel, initialResponse, onErrorResume,
                    null, 2))
                .expectErrorMessage("KABOOM")
                .verify();
        }

        assertEquals(2, retries.get());
        assertEquals(2, offsets.size());
        offsets.forEach(e -> assertEquals(1024L, e));
        assertEquals(2, throwables.size());
        throwables.forEach(e -> assertEquals("KABOOM", e.getMessage()));
        assertArraysEqual(data, 0, 1024, written);
        // check that all responses are closed
        assertEquals(3, responses.size());
        responses.forEach(r -> assertTrue(r.isClosed()));
    }
}
