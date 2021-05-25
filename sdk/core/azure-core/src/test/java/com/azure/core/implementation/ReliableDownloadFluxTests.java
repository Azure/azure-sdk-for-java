// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link ReliableDownloadFlux}.
 */
public class ReliableDownloadFluxTests {
    @Test
    public void initialDownloadIsEmpty() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(Flux::empty,
            (ignoredThrowable, ignoredOffset) -> Flux.empty(), 0, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .assertNext(bytes -> assertEquals(0, bytes.length))
            .verifyComplete();
    }

    @Test
    public void initialDownloadIsAnErrorButRetries() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (ignoredThrowable, ignoredOffset) -> Flux.empty(), 1, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .assertNext(bytes -> assertEquals(0, bytes.length))
            .verifyComplete();
    }

    @Test
    public void initialDownloadAndRetryErrorButRetriesUntilCompletion() {
        AtomicInteger retryCount = new AtomicInteger(0);

        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (throwable, offset) -> {
                if (retryCount.getAndIncrement() == 0) {
                    return Flux.error(new RuntimeException());
                } else {
                    return Flux.empty();
                }
            }, 2, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .assertNext(bytes -> assertEquals(0, bytes.length))
            .verifyComplete();
    }

    @Test
    public void initialDownloadIsAnErrorAndNoRetriesAreAvailable() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (ignoredThrowable, ignoredOffset) -> Flux.empty(), 0, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void initialDownloadIsANonRetriableError() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (throwable, offset) -> {
                if (!(throwable instanceof IOException)) {
                    return Flux.error(throwable);
                }

                return Flux.empty();
            }, 1, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void retryFailsWithNonRetriableError() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new IOException()),
            (throwable, offset) -> {
                if (!(throwable instanceof IOException)) {
                    return Flux.error(throwable);
                }

                return Flux.error(new RuntimeException());
            }, 1, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void allRetriesAreConsumed() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (throwable, offset) -> Flux.error(new RuntimeException()), 100, 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void multipleSubscriptionsWorkAppropriately() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> generateFromOffset(0),
            (throwable, offset) -> generateFromOffset(offset), 1, 0L);

        byte[] expected = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        StepVerifier.create(Flux.range(0, 100)
            .parallel()
            .flatMap(ignored -> FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .map(bytes -> {
                assertArrayEquals(expected, bytes);
                return bytes;
            })
            .then())
            .verifyComplete();
    }

    @Test
    public void downloadFromAnInitialOffset() {
        ReliableDownloadFlux reliableDownloadFlux = new ReliableDownloadFlux(() -> Flux.error(new IOException()),
            ((throwable, offset) -> {
                if (!(throwable instanceof IOException)) {
                    return Flux.error(throwable);
                }

                return generateFromOffset(offset);
            }), 1, 5L);

        byte[] expected = new byte[]{0, 0, 0, 0, 0};

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(reliableDownloadFlux))
            .assertNext(bytes -> assertArrayEquals(expected, bytes))
            .verifyComplete();
    }

    private static Flux<ByteBuffer> generateFromOffset(long offset) {
        return Flux.generate(() -> offset, (count, sink) -> {
            if (count >= 10) {
                sink.complete();
            } else {
                sink.next(ByteBuffer.wrap(new byte[]{0}));
            }

            return count + 1;
        });
    }
}
