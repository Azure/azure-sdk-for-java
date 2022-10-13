// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.io;


import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockFluxHttpResponse;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.FaultyAsynchronousByteChannel;
import com.azure.core.util.PartialWriteAsynchronousChannel;
import com.azure.core.util.PartialWriteChannel;
import com.azure.core.util.ValidationUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOUtilsTest {
    private static final byte[] DATA;

    static {
        DATA = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        new Random().nextBytes(DATA);
    }

    @Test
    public void canTransferFromReadableByteChannelToWriteableByteChannel() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(DATA));
        WritableByteChannel destination = Channels.newChannel(byteArrayOutputStream);

        IOUtils.transfer(source, destination);

        ValidationUtils.assertArraysEqual(DATA, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void canTransferFromReadableByteChannelToWriteableByteChannelWithPartialWrites() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(DATA));
        WritableByteChannel destination = new PartialWriteChannel(Channels.newChannel(byteArrayOutputStream));

        IOUtils.transfer(source, destination);

        ValidationUtils.assertArraysEqual(DATA, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void canTransferFromReadableByteChannelToAsynchronousByteChannel() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", null);
        tempFile.toFile().deleteOnExit();

        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(DATA));
        try (AsynchronousByteChannel destination = IOUtils.toAsynchronousByteChannel(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            IOUtils.transferAsync(source, destination).block();
        }

        ValidationUtils.assertArraysEqual(DATA, Files.readAllBytes(tempFile));
    }

    @Test
    public void canTransferFromReadableByteChannelToAsynchronousByteChannelWithPartialWrites() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", null);
        tempFile.toFile().deleteOnExit();

        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(DATA));
        try (AsynchronousByteChannel destination = IOUtils.toAsynchronousByteChannel(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            AsynchronousByteChannel paritialWriteDestination = new PartialWriteAsynchronousChannel(destination);
            IOUtils.transferAsync(source, paritialWriteDestination).block();
        }

        ValidationUtils.assertArraysEqual(DATA, Files.readAllBytes(tempFile));
    }

    @Test
    public void canResumeStreamResponseTransfer() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", null);
        tempFile.toFile().deleteOnExit();

        Function<Integer, Flux<ByteBuffer>> fluxSupplier = offset -> Flux.generate(() -> offset, (currentOffset, sink) -> {
            int size = Math.min(64, DATA.length - currentOffset);
            if (size > 0) {
                sink.next(ByteBuffer.wrap(DATA, currentOffset, size));
            } else {
                sink.complete();
            }
            return currentOffset + size;
        });
        AtomicInteger retries = new AtomicInteger();
        ConcurrentLinkedQueue<Long> offsets = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> throwables = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<HttpResponse> responses = new ConcurrentLinkedQueue<>();
        HttpResponse httpResponse = Mockito.spy(new MockFluxHttpResponse(
            Mockito.mock(HttpRequest.class), fluxSupplier.apply(0)));
        responses.add(httpResponse);
        StreamResponse initialResponse = new StreamResponse(httpResponse);
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume = (throwable, offset) -> {
            retries.incrementAndGet();
            offsets.add(offset);
            throwables.add(throwable);
            HttpResponse newHttpResponse = Mockito.spy(new MockFluxHttpResponse(
                Mockito.mock(HttpRequest.class), fluxSupplier.apply(offset.intValue())));
            responses.add(newHttpResponse);
            return Mono.just(new StreamResponse(newHttpResponse));
        };

        try (FaultyAsynchronousByteChannel channel = new FaultyAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
            () -> new IOException("KABOOM"),
            3,
            1024)) {

            StepVerifier.create(IOUtils.transferStreamResponseToAsynchronousByteChannel(
                    channel, initialResponse, onErrorResume, null, 5))
                .verifyComplete();
        }

        assertEquals(3, retries.get());
        assertEquals(3, offsets.size());
        offsets.forEach(e -> assertEquals(1024L, e));
        assertEquals(3, throwables.size());
        throwables.forEach(e -> assertEquals("KABOOM", e.getMessage()));
        ValidationUtils.assertArraysEqual(DATA, Files.readAllBytes(tempFile));
        // check that all responses are closed
        assertEquals(4, responses.size());
        responses.forEach(r -> Mockito.verify(r).close());
    }

    @Test
    public void throwsIfRetriesAreExhausted() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", null);
        tempFile.toFile().deleteOnExit();

        Function<Integer, Flux<ByteBuffer>> fluxSupplier = offset -> Flux.generate(() -> offset, (currentOffset, sink) -> {
            int size = Math.min(64, DATA.length - currentOffset);
            if (size > 0) {
                sink.next(ByteBuffer.wrap(DATA, currentOffset, size));
            } else {
                sink.complete();
            }
            return currentOffset + size;
        });
        AtomicInteger retries = new AtomicInteger();
        ConcurrentLinkedQueue<Long> offsets = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> throwables = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<HttpResponse> responses = new ConcurrentLinkedQueue<>();
        HttpResponse httpResponse = Mockito.spy(new MockFluxHttpResponse(
            Mockito.mock(HttpRequest.class), fluxSupplier.apply(0)));
        responses.add(httpResponse);
        StreamResponse initialResponse = new StreamResponse(httpResponse);
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume = (throwable, offset) -> {
            retries.incrementAndGet();
            offsets.add(offset);
            throwables.add(throwable);
            HttpResponse newHttpResponse = Mockito.spy(new MockFluxHttpResponse(
                Mockito.mock(HttpRequest.class), fluxSupplier.apply(offset.intValue())));
            responses.add(newHttpResponse);
            return Mono.just(new StreamResponse(newHttpResponse));
        };

        try (FaultyAsynchronousByteChannel channel = new FaultyAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
            () -> new IOException("KABOOM"),
            3,
            1024)) {

            StepVerifier.create(IOUtils.transferStreamResponseToAsynchronousByteChannel(
                    channel, initialResponse, onErrorResume, null, 2))
                .expectErrorMessage("KABOOM")
                .verify();
        }

        assertEquals(2, retries.get());
        assertEquals(2, offsets.size());
        offsets.forEach(e -> assertEquals(1024L, e));
        assertEquals(2, throwables.size());
        throwables.forEach(e -> assertEquals("KABOOM", e.getMessage()));
        ValidationUtils.assertArraysEqual(DATA, 0, Files.readAllBytes(tempFile), 0, 1024);
        // check that all responses are closed
        assertEquals(3, responses.size());
        responses.forEach(r -> Mockito.verify(r).close());
    }
}
