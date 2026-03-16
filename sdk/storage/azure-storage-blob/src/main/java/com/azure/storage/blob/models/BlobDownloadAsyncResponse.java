// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.io.IOUtils;
import com.azure.storage.blob.implementation.accesshelpers.BlobDownloadAsyncResponseConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.policy.StorageContentValidationDecoderPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousByteChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * This class contains the response information returned from the server when downloading a blob.
 */
public final class BlobDownloadAsyncResponse extends ResponseBase<BlobDownloadHeaders, Flux<ByteBuffer>>
    implements Closeable {

    private static final ClientLogger LOGGER = new ClientLogger(BlobDownloadAsyncResponse.class);

    static {
        BlobDownloadAsyncResponseConstructorProxy
            .setAccessor(new BlobDownloadAsyncResponseConstructorProxy.BlobDownloadAsyncResponseConstructorAccessor() {
                @Override
                public BlobDownloadAsyncResponse create(StreamResponse sourceResponse,
                    BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions,
                    AtomicReference<StorageContentValidationDecoderPolicy.DecoderState> decoderStateRef) {
                    return new BlobDownloadAsyncResponse(sourceResponse, onErrorResume, retryOptions, decoderStateRef);
                }

                @Override
                public StreamResponse getSourceResponse(BlobDownloadAsyncResponse response) {
                    return response.sourceResponse;
                }

                @Override
                public StorageContentValidationDecoderPolicy.DecoderState
                    getDecoderState(BlobDownloadAsyncResponse response) {
                    AtomicReference<StorageContentValidationDecoderPolicy.DecoderState> ref = response.decoderStateRef;
                    return ref == null ? null : ref.get();
                }
            });
    }

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final StreamResponse sourceResponse;
    private final BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume;
    private final DownloadRetryOptions retryOptions;
    private final AtomicReference<StorageContentValidationDecoderPolicy.DecoderState> decoderStateRef;

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
        this.sourceResponse = null;
        this.onErrorResume = null;
        this.retryOptions = null;
        this.decoderStateRef = null;
    }

    /**
     * Constructs a {@link BlobDownloadAsyncResponse}.
     *
     * @param sourceResponse The initial Stream Response
     * @param onErrorResume Function used to resume.
     * @param retryOptions Retry options.
     */
    BlobDownloadAsyncResponse(StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions) {
        this(sourceResponse, onErrorResume, retryOptions, null);
    }

    BlobDownloadAsyncResponse(StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions,
        AtomicReference<StorageContentValidationDecoderPolicy.DecoderState> decoderStateRef) {
        this(sourceResponse, onErrorResume, retryOptions, decoderStateRef, extractHeaders(sourceResponse));
    }

    private BlobDownloadAsyncResponse(StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions,
        AtomicReference<StorageContentValidationDecoderPolicy.DecoderState> decoderStateRef,
        BlobDownloadHeaders deserializedHeaders) {
        super(sourceResponse.getRequest(), sourceResponse.getStatusCode(), sourceResponse.getHeaders(),
            createResponseFluxWithContentCrc(sourceResponse, onErrorResume, retryOptions, decoderStateRef,
                deserializedHeaders),
            deserializedHeaders);
        this.sourceResponse = Objects.requireNonNull(sourceResponse, "'sourceResponse' must not be null");
        this.onErrorResume = Objects.requireNonNull(onErrorResume, "'onErrorResume' must not be null");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' must not be null");
        this.decoderStateRef = decoderStateRef;
    }

    private static BlobDownloadHeaders extractHeaders(StreamResponse response) {
        HttpHeaders headers = response.getHeaders();
        return ModelHelper.populateBlobDownloadHeaders(new BlobsDownloadHeaders(headers),
            ModelHelper.getErrorCode(headers));
    }

    private static Flux<ByteBuffer> createResponseFlux(StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions) {
        return FluxUtil
            .createRetriableDownloadFlux(sourceResponse::getValue,
                (throwable, position) -> onErrorResume.apply(throwable, position).flatMapMany(StreamResponse::getValue),
                retryOptions.getMaxRetryRequests())
            .defaultIfEmpty(EMPTY_BUFFER);
    }

    /**
     * Builds the response flux and populates ContentCrc64 on the deserialized headers when structured message
     * decoding completes (mirrors .NET Details.ContentCrc populated after stream consumption).
     */
    private static Flux<ByteBuffer> createResponseFluxWithContentCrc(StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions,
        AtomicReference<StorageContentValidationDecoderPolicy.DecoderState> decoderStateRef,
        BlobDownloadHeaders deserializedHeaders) {
        Flux<ByteBuffer> flux = createResponseFlux(sourceResponse, onErrorResume, retryOptions);
        if (decoderStateRef != null && deserializedHeaders != null) {
            flux = flux.doOnComplete(() -> {
                StorageContentValidationDecoderPolicy.DecoderState state = decoderStateRef.get();
                if (state != null && state.isFinalized()) {
                    long crc = state.getComposedCrc64();
                    byte[] crcBytes = new byte[8];
                    ByteBuffer.wrap(crcBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(crc);
                    deserializedHeaders.setContentCrc64(crcBytes);
                }
            });
        }
        return flux;
    }

    /**
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @param progressReporter Optional {@link ProgressReporter}.
     * @return A {@link Mono} that completes when transfer is completed.
     */
    public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel, ProgressReporter progressReporter) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        LOGGER.atVerbose()
            .addKeyValue("thread", Thread.currentThread().getName())
            .log("BlobDownloadAsyncResponse.writeValueToAsync entry");
        if (sourceResponse != null) {
            LOGGER.atVerbose()
                .log("BlobDownloadAsyncResponse.writeValueToAsync using sourceResponse (IOUtils.transfer)");
            return IOUtils.transferStreamResponseToAsynchronousByteChannel(channel, sourceResponse, onErrorResume,
                progressReporter, retryOptions.getMaxRetryRequests());
        } else if (super.getValue() != null) {
            return FluxUtil.writeToAsynchronousByteChannel(
                FluxUtil.addProgressReporting(super.getValue(), progressReporter), channel);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public void close() throws IOException {
        if (sourceResponse != null) {
            sourceResponse.close();
        } else {
            super.getValue().subscribe().dispose();
        }
    }
}
