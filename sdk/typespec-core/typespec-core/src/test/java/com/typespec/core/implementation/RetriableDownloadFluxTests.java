// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.http.policy.ExponentialBackoffOptions;
import com.typespec.core.http.policy.FixedDelayOptions;
import com.typespec.core.http.policy.RetryOptions;
import com.typespec.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.typespec.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link RetriableDownloadFlux}.
 */
public class RetriableDownloadFluxTests {
    @Test
    public void initialDownloadIsEmpty() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(Flux::empty,
            (ignoredThrowable, ignoredOffset) -> Flux.empty(), createDefaultRetryOptions(0), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .assertNext(bytes -> assertEquals(0, bytes.length))
            .verifyComplete();
    }

    @Test
    public void initialDownloadIsAnErrorButRetries() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (ignoredThrowable, ignoredOffset) -> Flux.empty(), createDefaultRetryOptions(1), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .assertNext(bytes -> assertEquals(0, bytes.length))
            .verifyComplete();
    }

    @Test
    public void initialDownloadAndRetryErrorButRetriesUntilCompletion() {
        AtomicInteger retryCount = new AtomicInteger(0);

        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (throwable, offset) -> {
                if (retryCount.getAndIncrement() == 0) {
                    return Flux.error(new RuntimeException());
                } else {
                    return Flux.empty();
                }
            }, createDefaultRetryOptions(2), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .assertNext(bytes -> assertEquals(0, bytes.length))
            .verifyComplete();
    }

    @Test
    public void initialDownloadIsAnErrorAndNoRetriesAreAvailable() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (ignoredThrowable, ignoredOffset) -> Flux.empty(), createDefaultRetryOptions(0), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void initialDownloadIsANonRetriableError() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (throwable, offset) -> {
                if (!(throwable instanceof IOException)) {
                    return Flux.error(throwable);
                }

                return Flux.empty();
            }, createDefaultRetryOptions(1), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void retryFailsWithNonRetriableError() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new IOException()),
            (throwable, offset) -> {
                if (!(throwable instanceof IOException)) {
                    return Flux.error(throwable);
                }

                return Flux.error(new RuntimeException());
            }, createDefaultRetryOptions(1), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void allRetriesAreConsumed() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new RuntimeException()),
            (throwable, offset) -> Flux.error(new RuntimeException()),
            new RetryOptions(new FixedDelayOptions(100, Duration.ofMillis(1))), 0L);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void multipleSubscriptionsWorkAppropriately() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> generateFromOffset(0),
            (throwable, offset) -> generateFromOffset(offset), createDefaultRetryOptions(1), 0L);

        byte[] expected = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        StepVerifier.create(Flux.range(0, 100)
            .parallel()
            .flatMap(ignored -> FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .map(bytes -> {
                assertArraysEqual(expected, bytes);
                return bytes;
            })
            .then())
            .verifyComplete();
    }

    @Test
    public void downloadFromAnInitialOffset() {
        RetriableDownloadFlux retriableDownloadFlux = new RetriableDownloadFlux(() -> Flux.error(new IOException()),
            ((throwable, offset) -> {
                if (!(throwable instanceof IOException)) {
                    return Flux.error(throwable);
                }

                return generateFromOffset(offset);
            }), createDefaultRetryOptions(1), 5L);

        byte[] expected = new byte[]{0, 0, 0, 0, 0};

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(retriableDownloadFlux))
            .assertNext(bytes -> assertArraysEqual(expected, bytes))
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

    private static RetryOptions createDefaultRetryOptions(int maxRetries) {
        return new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(maxRetries));
    }
}
