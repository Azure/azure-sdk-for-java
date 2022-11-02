// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.accesshelpers.BlobDownloadAsyncResponseConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.azure.core.util.FluxUtil.addProgressReporting;

/**
 * This class contains the response information returned from the server when downloading a blob.
 */
public final class BlobDownloadAsyncResponse extends ResponseBase<BlobDownloadHeaders, Flux<ByteBuffer>>
    implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(BlobDownloadAsyncResponse.class);

    static {
        BlobDownloadAsyncResponseConstructorProxy.setAccessor(
            new BlobDownloadAsyncResponseConstructorProxy.BlobDownloadAsyncResponseConstructorAccessor() {
                @Override
                public BlobDownloadAsyncResponse create(StreamResponse sourceResponse,
                    BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
                    DownloadRetryOptions retryOptions) {
                    return new BlobDownloadAsyncResponse(sourceResponse, onErrorResume, retryOptions);
                }

                @Override
                public Mono<Void> writeToFile(BlobDownloadAsyncResponse response, FileChannel fileChannel,
                    long position, ProgressReporter progressReporter) {
                    return response.writeValueToFile(fileChannel, position, progressReporter);
                }
            });
    }

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private final StreamResponse sourceResponse;
    private final BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume;
    private final DownloadRetryOptions retryOptions;

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
        super(sourceResponse.getRequest(), sourceResponse.getStatusCode(), sourceResponse.getHeaders(),
            createResponseFlux(sourceResponse, onErrorResume, retryOptions), extractHeaders(sourceResponse));
        this.sourceResponse = Objects.requireNonNull(sourceResponse, "'sourceResponse' must not be null");
        this.onErrorResume = Objects.requireNonNull(onErrorResume, "'onErrorResume' must not be null");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' must not be null");
    }

    private static BlobDownloadHeaders extractHeaders(StreamResponse response) {
        return ModelHelper.populateBlobDownloadHeaders(new BlobsDownloadHeaders(response.getHeaders()),
            ModelHelper.getErrorCode(response.getHeaders()));
    }

    private static Flux<ByteBuffer> createResponseFlux(StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, DownloadRetryOptions retryOptions) {
        return FluxUtil.createRetriableDownloadFlux(sourceResponse::getValue,
                (throwable, position) -> onErrorResume.apply(throwable, position).flatMapMany(Response::getValue),
                retryOptions.getMaxRetryRequests())
            .defaultIfEmpty(EMPTY_BUFFER);
    }

    Mono<Void> writeValueToFile(FileChannel fileChannel, long position, ProgressReporter progressReporter) {
        if (sourceResponse != null) {
            return transferStreamResponseToWritableByteChannelHelper(
                new BufferedFileChannel(fileChannel, position, progressReporter), sourceResponse, onErrorResume,
                retryOptions.getMaxRetryRequests(), 0);
        } else if (super.getValue() != null) {
            OutputStream stream = Channels.newOutputStream(new BufferedFileChannel(fileChannel, position,
                progressReporter));
            return FluxUtil.writeToOutputStream(addProgressReporting(super.getValue(), progressReporter), stream)
                .then(Mono.fromCallable(() -> {
                    stream.flush();
                    return null;
                }));
        } else {
            return Mono.empty();
        }
    }

    private static Mono<Void> transferStreamResponseToWritableByteChannelHelper(
        BufferedFileChannel bufferedFileChannel, StreamResponse response,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, int maxRetries, int retryCount) {
        return Mono.fromRunnable(() -> response.writeValueTo(bufferedFileChannel))
            .onErrorResume(Exception.class, exception -> {
                response.close();

                int updatedRetryCount = retryCount + 1;

                if (updatedRetryCount > maxRetries) {
                    LOGGER.atError().addKeyValue("tryCount", retryCount)
                        .log(() -> "Retry attempts have been exhausted.", exception);
                    return Mono.error(exception);
                }

                return onErrorResume.apply(exception, bufferedFileChannel.getBytesWritten())
                    .flatMap(newResponse -> transferStreamResponseToWritableByteChannelHelper(bufferedFileChannel,
                        newResponse, onErrorResume, maxRetries, updatedRetryCount));
            })
            .then(Mono.fromCallable(() -> {
                bufferedFileChannel.flush();
                response.close();
                return null;
            }));
    }

    private static final class BufferedFileChannel implements WritableByteChannel {
        private final FileChannel innerChannel;
        private final long position;
        private final ProgressReporter progressReporter;

        private ByteBuffer buffer;
        private long bytesWritten = 0L;

        private BufferedFileChannel(FileChannel innerChannel, long position, ProgressReporter progressReporter) {
            this.innerChannel = innerChannel;
            this.position = position;
            this.progressReporter = progressReporter;
        }

        long getBytesWritten() {
            return bytesWritten;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (buffer == null) {
                buffer = ByteBuffer.allocate(64 * 1024);
            }

            if (buffer.remaining() >= src.remaining()) {
                buffer.put(src);
                return 0;
            }

            ByteBuffer send = buffer;
            send.flip();
            buffer = ByteBuffer.allocate(64 * 1024);
            buffer.put(src);

            return writeInternal(send);
        }

        void flush() throws IOException {
            if (buffer.position() > 0) {
                buffer.flip();

                writeInternal(buffer);
            }
        }

        private int writeInternal(ByteBuffer send) throws IOException {
            int totalWritten = 0;

            while (send.hasRemaining()) {
                int written = innerChannel.write(send, position + totalWritten);
                if (progressReporter != null) {
                    progressReporter.reportProgress(written);
                }
                bytesWritten += written;
                totalWritten += written;
            }

            return totalWritten;
        }

        @Override
        public boolean isOpen() {
            return innerChannel.isOpen();
        }

        @Override
        public void close() throws IOException {
            innerChannel.close();
        }
    }

    /**
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     *
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @param progressReporter Optional {@link ProgressReporter}.
     * @return A {@link Mono} that completes when transfer is completed.
     */
    public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel, ProgressReporter progressReporter) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        if (sourceResponse != null) {
            return IOUtils.transferStreamResponseToAsynchronousByteChannel(channel,
                sourceResponse, onErrorResume, progressReporter, retryOptions.getMaxRetryRequests());
        } else if (super.getValue() != null) {
            return FluxUtil.writeToAsynchronousByteChannel(
                addProgressReporting(super.getValue(), progressReporter), channel);
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
