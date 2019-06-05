// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.interceptor;

import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * An HTTP response that is created to simulate a HTTP request.
 */
public class MockHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final String body;

    /**
     * Creates a buffered HTTP response.
     * @param statusCode The HTTP response status code.
     * @param headers The HTTP response headers.
     * @param body The HTTP response body as a string.
     */
    public MockHttpResponse(int statusCode, HttpHeaders headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String headerValue(String headerName) {
        return headers.value(headerName);
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public Single<byte[]> bodyAsByteArray() {
        return Single.just(body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Single<String> bodyAsString() {
        return Single.just(body);
    }

    @Override
    public Flowable<ByteBuffer> body() {
        return Flowable.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
    }

}
