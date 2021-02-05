// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Response;
import okhttp3.ResponseBody;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpAsyncResponse extends OkHttpAsyncResponseBase {
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

    private final ClientLogger logger = new ClientLogger(OkHttpAsyncResponse.class);

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
        return Flux.using(this.responseBody::byteStream,
            OkHttpAsyncResponse::toFluxByteBuffer,
            bodyStream -> {
                // OkHttp: The stream from ResponseBody::byteStream() has to be explicitly closed.
                // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-must-be-closed
                try {
                    bodyStream.close();
                } catch (IOException ioe) {
                    throw logger.logExceptionAsError(Exceptions.propagate(ioe));
                }
            }, false);
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
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }

    /**
     * Creates a Flux of ByteBuffer, with each ByteBuffer wrapping bytes read from the given InputStream.
     *
     * @param inputStream InputStream to back the Flux
     * @return Flux of ByteBuffer backed by the InputStream
     */
    private static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
        Pair pair = new Pair();
        return Flux.just(true)
            .repeat()
            .map(ignore -> {
                byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];
                try {
                    int numBytes = inputStream.read(buffer);
                    if (numBytes > 0) {
                        return pair.buffer(ByteBuffer.wrap(buffer, 0, numBytes)).readBytes(numBytes);
                    } else {
                        return pair.buffer(null).readBytes(numBytes);
                    }
                } catch (IOException ioe) {
                    throw Exceptions.propagate(ioe);
                }
            })
            .takeUntil(p -> p.readBytes() == -1)
            .filter(p -> p.readBytes() > 0)
            .map(Pair::buffer);
    }

    private static class Pair {
        private ByteBuffer byteBuffer;
        private int readBytes;

        ByteBuffer buffer() {
            return this.byteBuffer;
        }

        int readBytes() {
            return this.readBytes;
        }

        Pair buffer(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
            return this;
        }

        Pair readBytes(int cnt) {
            this.readBytes = cnt;
            return this;
        }
    }
}
