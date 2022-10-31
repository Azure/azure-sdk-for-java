// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.storage.blob.implementation.accesshelpers.BlobDownloadAsyncResponseConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * This class contains the response information returned from the server when downloading a blob.
 */
public final class BlobDownloadAsyncResponse extends ResponseBase<BlobDownloadHeaders, Flux<ByteBuffer>> implements Closeable {

    static {
        BlobDownloadAsyncResponseConstructorProxy.setAccessor(BlobDownloadAsyncResponse::new);
    }

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    /**
     * Constructs a {@link BlobDownloadAsyncResponse}.
     *
     * @param request Request sent to the service.
     * @param statusCode Response status code returned by the service.
     * @param headers Raw headers returned by the response.
     * @param value Stream of download data being returned by the service.
     * @param deserializedHeaders Headers deserialized into an object.
     */
    public BlobDownloadAsyncResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value,
        BlobDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, value, deserializedHeaders);
    }

    /**
     * Constructs a {@link BlobDownloadAsyncResponse}.
     *
     * @param sourceResponse The initial Stream Response
     * @param onErrorResume Function used to resume.
     * @param retryOptions Retry options.
     */
    BlobDownloadAsyncResponse(ResponseBase<BlobsDownloadHeaders, Flux<ByteBuffer>> sourceResponse,
        BiFunction<Throwable, Long, Mono<ResponseBase<BlobsDownloadHeaders, Flux<ByteBuffer>>>> onErrorResume,
        DownloadRetryOptions retryOptions) {
        super(sourceResponse.getRequest(), sourceResponse.getStatusCode(), sourceResponse.getHeaders(),
            createResponseFlux(sourceResponse,
                Objects.requireNonNull(onErrorResume, "'onErrorResume' must not be null"),
                Objects.requireNonNull(retryOptions, "'retryOptions' must not be null")),
            extractHeaders(sourceResponse));
    }

    private static BlobDownloadHeaders extractHeaders(ResponseBase<BlobsDownloadHeaders, Flux<ByteBuffer>> response) {
        return ModelHelper.populateBlobDownloadHeaders(response.getDeserializedHeaders(),
            ModelHelper.getErrorCode(response.getHeaders()));
    }

    private static Flux<ByteBuffer> createResponseFlux(
        ResponseBase<BlobsDownloadHeaders, Flux<ByteBuffer>> sourceResponse,
        BiFunction<Throwable, Long, Mono<ResponseBase<BlobsDownloadHeaders, Flux<ByteBuffer>>>> onErrorResume,
        DownloadRetryOptions retryOptions) {
        return FluxUtil.createRetriableDownloadFlux(sourceResponse::getValue,
                (throwable, position) -> onErrorResume.apply(throwable, position).flatMapMany(Response::getValue),
                retryOptions.getMaxRetryRequests())
            .defaultIfEmpty(EMPTY_BUFFER);
    }

    /**
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @param progressReporter Optional {@link ProgressReporter}.
     * @return A {@link Mono} that completes when transfer is completed.
     */
    public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel, ProgressReporter progressReporter) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        if (super.getValue() != null) {
            return FluxUtil.writeToAsynchronousByteChannel(
                FluxUtil.addProgressReporting(super.getValue(), progressReporter), channel);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public void close() throws IOException {
        super.getValue().subscribe().dispose();
    }
}
