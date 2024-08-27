// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.io.IOUtils;
import okhttp3.Response;
import okhttp3.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpAsyncResponse extends OkHttpAsyncResponseBase {
    // Previously, this was 4096, but it is being changed to 8192 as that more closely aligns to what Netty uses as a
    // default and will reduce the number of small allocations we'll need to make.
    private static final int BYTE_BUFFER_CHUNK_SIZE = 8192;

    private final ResponseBody responseBody;

    /**
     * Creates an OkHttpAsyncResponse.
     *
     * @param response The OkHttp response.
     * @param request The request which generated the response.
     * @param eagerlyConvertHeaders Whether to eagerly convert the response headers.
     */
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
        return Flux.using(this.responseBody::byteStream,
            bodyStream -> FluxUtil.toFluxByteBuffer(bodyStream, BYTE_BUFFER_CHUNK_SIZE), bodyStream -> this.close(),
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
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        if (responseBody != null) {
            try {
                IOUtils.transfer(responseBody.source(), channel, responseBody.contentLength());
            } finally {
                close();
            }
        }
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        if (responseBody != null) {
            return Mono.using(() -> this,
                ignored -> IOUtils.transferAsync(responseBody.source(), channel, responseBody.contentLength()),
                OkHttpAsyncResponse::close);
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
