// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.StreamUtils;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpAsyncResponse extends OkHttpAsyncResponseBase {
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

    private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);

    private final ResponseBody responseBody;

    public OkHttpAsyncResponse(Response response, HttpRequest request) {
        super(response, request);
        // innerResponse.body() getter will not return null for server returned responses.
        // It can be null:
        // [a]. if response is built manually with null body (e.g for mocking)
        // [b]. for the cases described here
        // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
        this.responseBody = response.body();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (this.responseBody == null) {
            return Flux.empty();
        }

        // Use Flux.using to close the stream after complete emission
        return Flux.using(this.responseBody::byteStream, OkHttpAsyncResponse::toFluxByteBuffer,
            bodyStream -> this.close(), false);
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryDataHelper.createBinaryData(new InputStreamContent(this.responseBody.byteStream()));
    }

    private static Flux<ByteBuffer> toFluxByteBuffer(InputStream responseBody) {
        return Flux.just(true)
            .repeat()
            .flatMap(ignored -> {
                byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];
                try {
                    int read = responseBody.read(buffer);
                    if (read > 0) {
                        return Mono.just(Tuples.of(read, ByteBuffer.wrap(buffer, 0, read)));
                    } else {
                        return Mono.just(Tuples.of(read, EMPTY_BYTE_BUFFER));
                    }
                } catch (IOException ex) {
                    return Mono.error(ex);
                }
            })
            .takeUntil(tuple -> tuple.getT1() == -1)
            .filter(tuple -> tuple.getT1() > 0)
            .map(Tuple2::getT2);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.fromCallable(() -> {
            // Reactor: The fromCallable operator treats a null from the Callable
            // as completion signal.
            if (responseBody == null) {
                return null;
            }
            byte[] content = responseBody.bytes();
            // Consistent with GAed behaviour.
            if (content.length == 0) {
                return null;
            }
            // OkHttp: When calling ResponseBody::bytes() the underlying stream automatically closed.
            // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-must-be-closed
            return content;
        });
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        if (responseBody == null) {
            return Mono.empty();
        }

        return Mono.using(responseBody::byteStream, Mono::just, ignored -> this.close());
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        StreamUtils.INSTANCE.transfer(responseBody.byteStream(), outputStream);
    }

    @Override
    public Mono<Void> writeBodyTo(AsynchronousFileChannel asynchronousFileChannel, long position) {
        return Mono.defer(
            () -> {
                AtomicLong currentPosition = new AtomicLong(position);
                byte[] buffer = new byte[8 * 1024];
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                BufferedSource source = responseBody.source();
                try {
                    int read = source.read(buffer);
                    byteBuffer.position(0);
                    byteBuffer.limit(read);

                    // TODO (kasobol-msft) consider using just reactor here.
                    CompletableFuture<Void> completableFuture = new CompletableFuture<>();

                    writeToChannel(
                        asynchronousFileChannel, currentPosition, buffer, byteBuffer, source, completableFuture);

                    return Mono.fromFuture(completableFuture);
                } catch (IOException e) {
                    return Mono.error(e);
                }
            }
        ).doFinally(ignored -> close());
    }

    @Override
    public void writeBodyTo(FileChannel fileChannel, long position) throws IOException {
        long maxTransferSize = responseBody.contentLength();
        if (maxTransferSize < 0) {
            // unknown body length fallback to Long.MAX
            maxTransferSize = Long.MAX_VALUE;
        }
        fileChannel.transferFrom(responseBody.source(), position, maxTransferSize);
    }

    private void writeToChannel(
        AsynchronousFileChannel asynchronousFileChannel, AtomicLong currentPosition,
        byte[] buffer, ByteBuffer byteBuffer, BufferedSource source, CompletableFuture<Void> completableFuture) {
        asynchronousFileChannel.write(byteBuffer, currentPosition.get(), byteBuffer,
            new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    currentPosition.addAndGet(result);

                    if (byteBuffer.hasRemaining()) {
                        // If the entire ByteBuffer hasn't been written send it to be written again until it completes.
                        writeToChannel(
                            asynchronousFileChannel, currentPosition, buffer, byteBuffer, source, completableFuture);
                    } else {
                        try {
                            if (!source.exhausted()) {
                                int read = source.read(buffer);
                                byteBuffer.position(0);
                                byteBuffer.limit(read);
                                writeToChannel(
                                    asynchronousFileChannel, currentPosition, buffer,
                                    byteBuffer, source, completableFuture);
                            } else {
                                completableFuture.complete(null);
                            }
                        } catch (IOException e) {
                            completableFuture.completeExceptionally(e);
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    completableFuture.completeExceptionally(exc);
                }
            });
    }

    @Override
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
