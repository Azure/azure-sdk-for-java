// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.TransferUtil;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * This class contains the response information returned from the server when downloading a blob.
 */
public final class BlobDownloadAsyncResponse extends ResponseBase<BlobDownloadHeaders, Flux<ByteBuffer>> implements Closeable {

    private static final Duration TIMEOUT_VALUE = Duration.ofSeconds(60);

    private static final Mono<ByteBuffer> EMPTY_BUFFER_MONO = Mono.just(ByteBuffer.allocate(0));
    private final StreamResponse initialResponse;
    private final BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume;
    private final long offset;
    private final int maxRetries;


    /**
     * Constructs a {@link BlobDownloadAsyncResponse}.
     *
     * @param request Request sent to the service.
     * @param statusCode Response status code returned by the service.
     * @param headers Raw headers returned in the response.
     * @param value Stream of download data being returned by the service.
     * @param deserializedHeaders Headers deserialized into an object.
     */
    public BlobDownloadAsyncResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value,
                                     BlobDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, value, deserializedHeaders);
        this.initialResponse = null;
        this.onErrorResume = null;
        this.offset = 0;
        this.maxRetries = 0;
    }

    /**
     * Constructs a {@link BlobDownloadAsyncResponse}.
     *
     * @param initialResponse The initial Stream Response
     * @param onErrorResume Function used to resume.
     * @param offset Initial offset.
     * @param maxRetries Max retries.
     */
    public BlobDownloadAsyncResponse(
        StreamResponse initialResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
        long offset, int maxRetries) {
        super(initialResponse.getRequest(), initialResponse.getStatusCode(),
            initialResponse.getHeaders(),
            createResponseFlux(initialResponse, onErrorResume, offset, maxRetries),
            extractHeaders(initialResponse));
        this.initialResponse = initialResponse;
        this.onErrorResume = onErrorResume;
        this.offset = offset;
        this.maxRetries = maxRetries;
    }

    private static BlobDownloadHeaders extractHeaders(StreamResponse response) {
        BlobsDownloadHeaders blobsDownloadHeaders =
            ModelHelper.transformBlobDownloadHeaders(response.getHeaders());
        return ModelHelper.populateBlobDownloadHeaders(
            blobsDownloadHeaders, ModelHelper.getErrorCode(response.getHeaders()));
    }

    private static Flux<ByteBuffer> createResponseFlux(
        StreamResponse initialResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
        long offset, int maxRetries) {
        return FluxUtil.createRetriableDownloadFlux(
                initialResponse::getValue,
                (throwable, position) -> onErrorResume.apply(throwable, position)
                    .flatMapMany(StreamResponse::getValue),
                maxRetries, offset)
            .switchIfEmpty(EMPTY_BUFFER_MONO).timeout(TIMEOUT_VALUE);
    }

    /**
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @param progressReporter The progress reporter.
     * @return A {@link Mono} that completes when transfer is completed.
     */
    public Mono<Void> transferContentToAsync(AsynchronousByteChannel channel, ProgressReporter progressReporter) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        if (initialResponse == null) {
            return FluxUtil.writeToAsynchronousByteChannel(super.getValue(), channel);
        } else {
            return TransferUtil.downloadToAsynchronousByteChannel(channel, Mono.just(initialResponse),
                onErrorResume, progressReporter, offset, maxRetries);
        }
    }

    @Override
    public void close() throws IOException {
        if (initialResponse != null) {
            initialResponse.close();
        } else {
            super.getValue().subscribe().dispose();
        }
    }
}
