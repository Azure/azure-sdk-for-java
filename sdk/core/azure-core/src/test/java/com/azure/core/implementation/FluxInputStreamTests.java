// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FluxInputStreamTests {
    private static final int KB = 1024;
    private static final int MB = KB * KB;

    /* Generates deterministic test data for FluxInputStream unit tests. */
    private Flux<ByteBuffer> generateData(int num) {
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(new byte[] { (byte) i }));
        }
        return Flux.fromIterable(buffers);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 100, KB, MB })
    public void fluxInputStreamMin(int byteCount) throws IOException {
        final int expected = byteCount;

        try (InputStream is = new FluxInputStream(generateData(byteCount))) {
            byte[] bytes = new byte[expected];
            int totalRead = 0;
            int bytesRead = 0;
            int remaining = expected;

            while (bytesRead != -1 && totalRead < expected) {
                bytesRead = is.read(bytes, totalRead, remaining);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    remaining -= bytesRead;
                }
            }

            assertEquals(expected, totalRead);
            for (int i = 0; i < expected; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }

    @Test
    public void fluxInputStreamWithEmptyByteBuffers() throws IOException {
        final int expected = KB;
        List<ByteBuffer> buffers = new ArrayList<>(expected * 2);
        for (int i = 0; i < expected; i++) {
            buffers.add(ByteBuffer.wrap(new byte[] { (byte) i }));
            buffers.add(ByteBuffer.wrap(new byte[0]));
        }

        try (InputStream is = new FluxInputStream(Flux.fromIterable(buffers))) {
            byte[] bytes = new byte[expected];
            int totalRead = 0;
            int bytesRead = 0;
            int remaining = expected;

            while (bytesRead != -1 && totalRead < expected) {
                bytesRead = is.read(bytes, totalRead, remaining);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    remaining -= bytesRead;
                }
            }

            assertEquals(expected, totalRead);
            for (int i = 0; i < expected; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }

    @Test
    public void closeBeforeFirstReadSubscribesAndCancelsWithoutDemand() throws IOException {
        AtomicInteger subscribeCalls = new AtomicInteger();
        AtomicInteger cancelCalls = new AtomicInteger();
        AtomicInteger disposeCalls = new AtomicInteger();
        AtomicLong requested = new AtomicLong();
        Flux<ByteBuffer> data = Flux.using(Object::new,
            ignored -> Flux.<ByteBuffer>never()
                .doOnSubscribe(subscription -> subscribeCalls.incrementAndGet())
                .doOnRequest(requested::addAndGet)
                .doOnCancel(cancelCalls::incrementAndGet),
            ignored -> disposeCalls.incrementAndGet());

        FluxInputStream stream = new FluxInputStream(data);
        stream.close();
        stream.close();

        assertEquals(1, subscribeCalls.get());
        assertEquals(0, requested.get());
        assertEquals(1, cancelCalls.get());
        assertEquals(1, disposeCalls.get());
    }

    @Test
    public void closeDuringFirstSubscriptionCancelsPublishedSubscription() throws Exception {
        CountDownLatch subscribeEntered = new CountDownLatch(1);
        AtomicInteger cancelCalls = new AtomicInteger();
        AtomicInteger disposeCalls = new AtomicInteger();
        AtomicLong requested = new AtomicLong();
        AtomicReference<Thread> closeThreadReference = new AtomicReference<>();
        Flux<ByteBuffer> data = Flux.using(Object::new, ignored -> Flux.from(subscriber -> {
            subscribeEntered.countDown();
            awaitThreadWaiting(closeThreadReference);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long count) {
                    requested.addAndGet(count);
                }

                @Override
                public void cancel() {
                    cancelCalls.incrementAndGet();
                }
            });
        }), ignored -> disposeCalls.incrementAndGet());
        FluxInputStream stream = new FluxInputStream(data);
        AtomicReference<Throwable> readError = new AtomicReference<>();
        AtomicReference<Throwable> closeError = new AtomicReference<>();
        Thread readThread = new Thread(() -> {
            try {
                stream.read();
            } catch (Throwable throwable) {
                readError.set(throwable);
            }
        });
        Thread closeThread = new Thread(() -> {
            try {
                stream.close();
            } catch (Throwable throwable) {
                closeError.set(throwable);
            }
        });
        closeThreadReference.set(closeThread);

        readThread.start();
        assertTrue(subscribeEntered.await(5, TimeUnit.SECONDS));
        closeThread.start();
        readThread.join(TimeUnit.SECONDS.toMillis(5));
        closeThread.join(TimeUnit.SECONDS.toMillis(5));

        assertFalse(readThread.isAlive());
        assertFalse(closeThread.isAlive());
        assertEquals(1, cancelCalls.get());
        assertEquals(0, requested.get());
        assertEquals(1, disposeCalls.get());
        assertNull(closeError.get());
        assertTrue(readError.get() instanceof IllegalStateException);
    }

    @ParameterizedTest
    @MethodSource("fluxInputStreamErrorSupplier")
    public void fluxInputStreamError(RuntimeException exception) {
        assertThrows(IOException.class, () -> {
            try (InputStream is = new FluxInputStream(Flux.error(exception))) {
                is.read();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static Stream<RuntimeException> fluxInputStreamErrorSupplier() {
        HttpResponse httpResponse = new HttpResponse(null) {
            @Override
            public int getStatusCode() {
                return 404;
            }

            @Override
            public String getHeaderValue(String name) {
                return "";
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return null;
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return null;
            }
        };
        return Stream.of(new IllegalArgumentException("Mock illegal argument exception."),
            new HttpResponseException("Mock exception", httpResponse, null),
            new UncheckedIOException(new IOException("Mock IO Exception.")));
    }

    private static void awaitThreadWaiting(AtomicReference<Thread> threadReference) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Thread thread = threadReference.get();
            if (thread != null && thread.getState() == Thread.State.WAITING) {
                return;
            }
            Thread.yield();
        }
        throw new AssertionError("Close thread didn't wait for the stream lock.");
    }
}
