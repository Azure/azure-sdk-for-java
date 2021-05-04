// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A {@code Flux<ByteBuffer>} implementation which is capable of performing a reliable download by applying a resume
 * operation if an error occurs during the download.
 */
public final class ReliableDownloadFlux extends Flux<ByteBuffer> {
    private final Supplier<Flux<ByteBuffer>> initialDownloadSupplier;
    private final Predicate<Throwable> resumePredicate;
    private final int maxRetries;
    private final Function<Long, Flux<ByteBuffer>> resumeDownload;

    private long position = 0L;
    private int retryCount = 0;

    /**
     * Creates a ReliableDownloadFlux.
     *
     * @param initialDownloadSupplier Supplier of the initial download.
     * @param resumePredicate A predicate to determine if the download should be resumed when an error occurs.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     * @param resumeDownload A function which takes the current download offset and resumes from that position.
     */
    public ReliableDownloadFlux(Supplier<Flux<ByteBuffer>> initialDownloadSupplier,
        Predicate<Throwable> resumePredicate, int maxRetries, Function<Long, Flux<ByteBuffer>> resumeDownload) {
        this.initialDownloadSupplier = initialDownloadSupplier;
        this.resumePredicate = resumePredicate;
        this.maxRetries = maxRetries;
        this.resumeDownload = resumeDownload;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
        initialDownloadSupplier.get()
            .doOnNext(buffer -> position += buffer.remaining())
            .onErrorResume(resumePredicate, throwable -> {
                retryCount++;

                if (retryCount > maxRetries) {
                    return Flux.error(throwable);
                }

                return resumeDownload.apply(position);
            })
            .subscribe(actual);
    }
}
