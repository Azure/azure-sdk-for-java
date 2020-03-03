// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.implementation.models.BlobsDownloadResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A utility to create Response with retry-able content.
 */
final class ContentRetryableResponseUtil {
    private static final String BOOK_KEEP_OBJECT_KEY = "ec6bc96b-ffe8-4b04-93bd-831733b25a59";
    private static final Duration CONTENT_READ_RETRY_DELAY = Duration.ofSeconds(2);

    private ContentRetryableResponseUtil() {
    }
    /**
     * Create a blob download {@link Response} with retry-able content.
     *
     * @param downloadFunction the function when called send a download blob request
     * @param responseMapper the mapper to map first response from downloadFunction to a {@link Response}
     *                       with retry-able content
     * @param contentReadTimeout the read timeout to apply on content
     * @param maxRetry the maximum number of times to call downloadFunction when content read fail
     * @param <R> the type of Response that mapper returns
     * @return a Response with retry-able content
     */
    static <R extends Response<Flux<ByteBuffer>>>
        Mono<R> createResponse(DownloadFunction downloadFunction,
                               BiFunction<BlobsDownloadResponse, Flux<ByteBuffer>, R> responseMapper,
                               Duration contentReadTimeout,
                               int maxRetry) {
        return downloadFunction.call(0, null)
            .map(firstResponse -> {
                Flux<ByteBuffer> retryableContentFlux
                    = Flux.deferWithContext((Function<Context, Flux<ByteBuffer>>) reactorContext -> {
                        final BookKeep bookKeep = reactorContext.get(BOOK_KEEP_OBJECT_KEY);

                        return firstResponse.getValue()
                            .timeout(bookKeep.getContentReadTimeout())
                            .onErrorResume(onErrorResumeFunction(downloadFunction, firstResponse))
                            .doOnNext(chunk -> {
                                // Account for emitted bytes.
                                bookKeep.addToBytesEmitted(chunk.remaining());
                                // The max retry count represents the number of times to retry in a row.
                                // Reset the retry count as soon as a chunk is successfully read.
                                bookKeep.resetRetryCount();
                            });

                    }).subscriberContext(reactorContext -> {
                        return reactorContext
                            .put(BOOK_KEEP_OBJECT_KEY, new BookKeep(contentReadTimeout, maxRetry));
                    });
                return responseMapper.apply(firstResponse, retryableContentFlux);
            });
    }

    /**
     * Get the function that inspect the error and switch to retry-able content if error is retry-able.
     *
     * @param downloadFunction the function when called send a download blob request
     * @param lastResponse the response of the last downloadFunction invocation, content of which failed to read
     * @return the error resume function
     */
    private static Function<Throwable, Flux<ByteBuffer>> onErrorResumeFunction(DownloadFunction downloadFunction,
                                                                               BlobsDownloadResponse lastResponse) {
        return throwable -> {
            if (throwable instanceof TimeoutException || throwable instanceof IOException) {
                return retryContentFlux(downloadFunction, lastResponse, throwable);
            }
            return Flux.error(throwable);
        };
    }

    /**
     * Get a content Flux which does retry on content read error.
     *
     * @param downloadFunction the function when called send a download blob request
     * @param lastResponse the response of the last downloadFunction invocation, content of which failed to read
     * @param retryableError the error due to which the content read was failed
     * @return retry-able Flux
     */
    private static Flux<ByteBuffer> retryContentFlux(DownloadFunction downloadFunction,
                                                     BlobsDownloadResponse lastResponse,
                                                     Throwable retryableError) {
        return Flux.deferWithContext(reactorContext -> {
            // This was subscribed because the blocked Flux<ByteBuffer> was killed via TimeoutException
            // or there is an IOException on reading Flux<ByteBuffer>. We need to swap that with a
            // new Flux<ByteBuffer>, which emit chunks from where the old Flux<ByteBuffer> was left off.
            //
            BookKeep bookKeep = reactorContext.get(BOOK_KEEP_OBJECT_KEY);
            if (bookKeep.isMaxRetried()) {
                throw Exceptions.propagate(retryableError);
            }
            bookKeep.incRetryCount();
            return downloadFunction.call(bookKeep.getBytesEmitted(), lastResponse)
                .delaySubscription(CONTENT_READ_RETRY_DELAY)
                .flatMapMany(response -> {
                    return response.getValue()
                        .timeout(bookKeep.getContentReadTimeout())
                        .onErrorResume(onErrorResumeFunction(downloadFunction, response));
                });
        });
    }

    /**
     * A contract representing a Function to download the blob.
     */
    @FunctionalInterface
    public interface DownloadFunction {
        /**
         * Get a Mono that when subscribed send download blob request.
         *
         * @param bytesEmitted the number of bytes of the blob successfully delivered to downstream before the
         *     last content read failure
         * @param lastResponse the last response from DownloadFunction, the content of which failed to read
         * @return the response Mono that on subscribe send download blob request and emit response
         */
        Mono<BlobsDownloadResponse> call(long bytesEmitted, BlobsDownloadResponse lastResponse);
    }

    /**
     * Internal type for content retry related house keeping.
     */
    private static class BookKeep {
        private final Duration contentReadTimeout;
        private final int maxRetry;
        private int bytesEmitted = 0;
        private int retryCount = 0;

        /**
         * Creates BookKeep.
         *
         * @param contentReadTimeout the content read timeout
         * @param maxRetry the maximum number of times to retry when a single read fails
         */
        BookKeep(Duration contentReadTimeout, int maxRetry) {
            this.contentReadTimeout = contentReadTimeout;
            this.maxRetry = maxRetry;
        }

        /**
         * Update the bytes emitted so far to the downstream.
         *
         * @param count the bytes currently emitted
         */
        void addToBytesEmitted(int count) {
            this.bytesEmitted += count;
        }

        /**
         * Get the total bytes emitted to downstream.
         *
         * @return the bytes emitted
         */
        int getBytesEmitted() {
            return this.bytesEmitted;
        }

        /**
         * Reset the retry counter.
         */
        void resetRetryCount() {
            this.retryCount = 0;
        }

        /**
         * Increment the retry counter by one.
         */
        void incRetryCount() {
            this.retryCount++;
        }

        /**
         * Checks whether the retry exhausted.
         *
         * @return true if maximum times retried
         */
        boolean isMaxRetried() {
            return this.retryCount > this.maxRetry;
        }

        /**
         * Get the read timeout for the response content
         *
         * @return the timeout
         */
        Duration getContentReadTimeout() {
            return this.contentReadTimeout;
        }
    }
}
