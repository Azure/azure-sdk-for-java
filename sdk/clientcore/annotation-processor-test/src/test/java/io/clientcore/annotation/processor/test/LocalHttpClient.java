// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.io.IOException;

/**
 * A mock implementation of {@link HttpClient} that allows for testing the {@link HttpRequest} sent to it.
 */
public final class LocalHttpClient implements HttpClient {
    private volatile HttpRequest lastHttpRequest;
    volatile boolean closeCalledOnResponse;

    @Override
    public Response<?> send(HttpRequest request) {
        lastHttpRequest = request;
        boolean success = request.getUri().getPath().equals("/my/uri/path");

        if (request.getHttpMethod().equals(HttpMethod.POST)) {
            success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
        } else {
            success &= request.getHttpMethod().equals(HttpMethod.GET)
                || request.getHttpMethod().equals(HttpMethod.HEAD);
        }

        return new MockHttpResponse(request, success ? 200 : 400) {
            @Override
            public void close() throws IOException {
                closeCalledOnResponse = true;

                super.close();
            }
        };
    }

    public HttpRequest getLastHttpRequest() {
        return lastHttpRequest;
    }
}
