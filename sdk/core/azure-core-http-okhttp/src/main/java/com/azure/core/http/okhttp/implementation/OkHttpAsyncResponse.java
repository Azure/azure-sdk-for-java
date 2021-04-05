// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import okhttp3.Response;
import okhttp3.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpAsyncResponse extends OkHttpAsyncResponseBase {
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

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
            stream -> FluxUtil.toFluxByteBuffer(stream, BYTE_BUFFER_CHUNK_SIZE),
            bodyStream -> this.close(),
            false);
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
}
