// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.io.IOUtils;
import okhttp3.Response;
import okhttp3.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpAsyncResponse extends OkHttpAsyncResponseBase {
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

    private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);

    private final ResponseBody responseBody;

    public OkHttpAsyncResponse(Response response, HttpRequest request, boolean eagerlyConvertHeaders) {
        super(response, request, eagerlyConvertHeaders);
        // innerResponse.body() getter will not return null for server returned responses.
        // It can be null:
        // [a]. if response is built manually with null body (e.g for mocking)
        // [b]. for the cases described here
        // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
        this.responseBody = response.body();
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromStream(this.responseBody.byteStream());
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

        return Mono.using(responseBody::byteStream, Mono::just, ignored -> this.close(), false);
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        if (responseBody != null) {
            IOUtils.transfer(responseBody.source(), channel);
        }
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        if (responseBody != null) {
            return IOUtils.transferAsync(responseBody.source(), channel);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
