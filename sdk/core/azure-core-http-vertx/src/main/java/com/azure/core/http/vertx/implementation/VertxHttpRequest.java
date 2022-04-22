// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

/**
 * Holds a Vert.x {@link HttpRequest} together with a body payload.
 */
public final class VertxHttpRequest {
    private final Buffer body;
    private final HttpRequest<Buffer> delegate;

    public VertxHttpRequest(HttpRequest<Buffer> delegate, Buffer body) {
        this.delegate = delegate;
        this.body = body;
    }

    public void send(VertxHttpResponseHandler handler) {
        delegate.sendBuffer(body, handler);
    }
}
