// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A {@code Flux<ByteBuffer>} implementation which is capable of performing a reliable download by applying a resume
 * operation if an error occurs during the download.
 */
public final class ReliableDownloadFlux extends Flux<ByteBuffer> {
    private final Supplier<Flux<ByteBuffer>> downloadSupplier;
    private final BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume;
    private final int maxRetries;

    private long position;
    private int retryCount;

    /**
     * Creates a ReliableDownloadFlux.
     *
     * @param downloadSupplier Supplier of the initial download.
     * @param onDownloadErrorResume {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     * downloading when an error occurs.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     * @param position The initial offset for the download.
     */
    public ReliableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, int maxRetries, long position) {
        this(downloadSupplier, onDownloadErrorResume, maxRetries, position, 0);
    }

    private ReliableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, int maxRetries, long position,
        int retryCount) {
        this.downloadSupplier = downloadSupplier;
        this.onDownloadErrorResume = onDownloadErrorResume;
        this.maxRetries = maxRetries;
        this.position = position;
        this.retryCount = retryCount;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
        downloadSupplier.get()
            .doOnNext(buffer -> position += buffer.remaining())
            .onErrorResume(throwable -> {
                retryCount++;

                if (retryCount > maxRetries) {
                    return Flux.error(throwable);
                }

                return new ReliableDownloadFlux(() -> onDownloadErrorResume.apply(throwable, position),
                    onDownloadErrorResume, maxRetries, position, retryCount);
            })
            .subscribe(actual);
    }
}
