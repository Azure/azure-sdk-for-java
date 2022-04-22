// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import reactor.core.publisher.MonoSink;

/**
 * {@link Handler} for Azure HTTP responses.
 */
public final class VertxHttpResponseHandler implements Handler<AsyncResult<HttpResponse<Buffer>>> {

    private final HttpRequest request;
    private final MonoSink<com.azure.core.http.HttpResponse> sink;
    private final boolean eagerlyReadResponse;

    public VertxHttpResponseHandler(HttpRequest request, MonoSink<com.azure.core.http.HttpResponse> sink,
                                    boolean eagerlyReadResponse) {
        this.request = request;
        this.sink = sink;
        this.eagerlyReadResponse = eagerlyReadResponse;
    }

    @Override
    public void handle(AsyncResult<HttpResponse<Buffer>> event) {
        if (event.succeeded()) {
            VertxHttpResponseBase response;
            if (eagerlyReadResponse) {
                io.vertx.ext.web.client.HttpResponse<Buffer> originalResponse = event.result();
                response = new BufferedVertxHttpResponse(request, originalResponse, originalResponse.body());
            } else {
                response = new VertxHttpAsyncResponse(request, event.result());
            }
            sink.success(response);
        } else {
            if (event.cause() != null) {
                sink.error(event.cause());
            }
        }
    }
}
