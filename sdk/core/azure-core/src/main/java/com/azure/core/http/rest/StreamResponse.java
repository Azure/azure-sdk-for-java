// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * REST response with a streaming content.
 */
public final class StreamResponse extends SimpleResponse<Flux<ByteBuffer>> implements Closeable {
    private volatile boolean consumed;

    private final HttpResponse response;

    /**
     * Creates a {@link StreamResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param value The content of the HTTP response.
     */
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value) {
        super(request, statusCode, headers, value);
        this.response = null;
    }

    /**
     * Creates a {@link StreamResponse}.
     *
     * @param request The request which resulted in this response.
     * @param response The source http response.
     */
    public StreamResponse(HttpRequest request, HttpResponse response) {
        super(request, response.getStatusCode(), response.getHeaders(), null);
        this.response = response;
    }

    /**
     * The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     *
     * @return The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     */
    @Override
    public Flux<ByteBuffer> getValue() {
        if (response == null) {
            return super.getValue().doFinally(t -> this.consumed = true);
        } else  {
            return response.getBody().doFinally(t -> this.consumed = true);
        }
    }

    /**
     * The content of the HTTP response as a {@link BinaryData}.
     *
     * @return The content of the HTTP response as a {@link BinaryData}.
     */
    public BinaryData getValueAsBinaryData() {
        if (response == null) {
            return BinaryDataHelper.createBinaryData(new FluxByteBufferContent(getValue()));
        } else {
            return response.getBodyAsBinaryData();
        }
    }

    /**
     * Writes body content to {@link OutputStream}.
     * @param outputStream {@link OutputStream}.
     * @throws IOException if an I/O error occurs when reading or writing.
     */
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        if (response != null) {
            response.writeBodyTo(outputStream);
        } else {
            FluxUtil.writeToOutputStream(getValue(), outputStream).block();
        }
    }

    /**
     * Disposes the connection associated with this {@link StreamResponse}.
     */
    @Override
    public void close() {
        if (this.consumed) {
            return;
        }
        if (response != null) {
            response.close();
        } else {
            final Flux<ByteBuffer> value = getValue();
            value.subscribe().dispose();
        }
        this.consumed = true;
    }
}
