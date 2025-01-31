// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.Foo;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.io.IOException;

import static io.clientcore.annotation.processor.test.TestInterfaceGenerationTests.SYNC_TOKEN_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock implementation of {@link HttpClient} that allows for testing the {@link HttpRequest} sent to it.
 */
public final class LocalHttpClient implements HttpClient {
    private volatile HttpRequest lastHttpRequest;
    volatile boolean closeCalledOnResponse;

    @Override
    public Response<?> send(HttpRequest request) {
        lastHttpRequest = request;
        String path = request.getUri().getPath();
        HttpMethod method = request.getHttpMethod();
        boolean success = false;

        if (path.matches("^/kv/[^/]+$")) {
            if (method.equals(HttpMethod.GET)) {
                assertEquals(SYNC_TOKEN_VALUE, request.getHeaders().getValue(HttpHeaderName.fromString("Sync-Token")));
                Foo responseBody = new Foo().setKey("bar").setLabel("baz");;
                return new MockHttpResponse(request, 200, responseBody);
            }
        } else if (path.equals("/my/uri/path")) {
            if (method.equals(HttpMethod.POST)) {
                success = "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success = method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD);
            }
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
