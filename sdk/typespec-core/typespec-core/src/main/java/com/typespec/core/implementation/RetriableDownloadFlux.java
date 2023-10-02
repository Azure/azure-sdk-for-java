// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.http.policy.RetryOptions;
import com.typespec.core.http.policy.RetryStrategy;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A {@code Flux<ByteBuffer>} implementation which is capable of performing a retriable download by applying a resume
 * operation if an error occurs during the download.
 */
public final class RetriableDownloadFlux extends Flux<ByteBuffer> {
    private static final ClientLogger LOGGER = new ClientLogger(RetriableDownloadFlux.class);

    private final Supplier<Flux<ByteBuffer>> downloadSupplier;
    private final BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume;
    private final RetryStrategy retryStrategy;
    private final int maxRetries;
    private final long position;
    private final int retryCount;

    /**
     * Creates a RetriableDownloadFlux.
     *
     * @param downloadSupplier Supplier of the initial download.
     * @param onDownloadErrorResume {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     * downloading when an error occurs.
     * @param retryOptions The configuration for retrying the failed download.
     * @param position The initial offset for the download.
     */
    public RetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, RetryOptions retryOptions,
        long position) {
        this(downloadSupplier, onDownloadErrorResume, ImplUtils.getRetryStrategyFromOptions(retryOptions), position,
            0);
    }

    private RetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, RetryStrategy retryStrategy, long position,
        int retryCount) {
        this.downloadSupplier = downloadSupplier;
        this.onDownloadErrorResume = onDownloadErrorResume;
        this.retryStrategy = retryStrategy;
        this.maxRetries = retryStrategy.getMaxRetries();
        this.position = position;
        this.retryCount = retryCount;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
        final long[] currentPosition = new long[]{position};

        downloadSupplier.get()
            .map(buffer -> {
                currentPosition[0] += buffer.remaining();
                return buffer;
            })
            .onErrorResume(Exception.class, exception -> {
                int updatedRetryCount = retryCount + 1;

                if (updatedRetryCount > maxRetries) {
                    LOGGER.log(LogLevel.ERROR, () -> "Exhausted all retry attempts while downloading, "
                        + maxRetries + " of " + maxRetries + ".", exception);
                    return Flux.error(exception);
                }

                LOGGER.log(LogLevel.INFORMATIONAL,
                    () -> "Using retry attempt " + updatedRetryCount + " of " + maxRetries + " while downloading.",
                    exception);
                Duration backoff = retryStrategy.calculateRetryDelay(updatedRetryCount);

                Flux<ByteBuffer> retryDownload = new RetriableDownloadFlux(
                    () -> onDownloadErrorResume.apply(exception, currentPosition[0]), onDownloadErrorResume,
                    retryStrategy, currentPosition[0], updatedRetryCount);

                if (backoff != null && !backoff.isNegative() && !backoff.isZero()) {
                    return retryDownload.delaySubscription(backoff);
                } else {
                    return retryDownload;
                }
            })
            .subscribe(actual);
    }
}
