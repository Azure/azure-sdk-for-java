// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * REST response with a streaming content.
 */
public final class BinaryDataResponse extends SimpleResponse<BinaryData> implements Closeable {
    private final HttpResponse response;

    /**
     * Creates a {@link BinaryDataResponse}.
     *
     * @param request The request which resulted in this response.
     * @param response The HTTP response.
     */
    public BinaryDataResponse(HttpRequest request, HttpResponse response) {
        super(request, response.getStatusCode(), response.getHeaders(), response.getBodyAsBinaryData());
        this.response = response;
    }

    /**
     * Writes body content to {@link OutputStream}.
     * @param outputStream {@link OutputStream}.
     * @throws IOException if an I/O error occurs when reading or writing.
     */
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        response.writeBodyTo(outputStream);
    }

    /**
     * Disposes the connection associated with this {@link BinaryDataResponse}.
     */
    @Override
    public void close() {
        response.close();
    }
}
